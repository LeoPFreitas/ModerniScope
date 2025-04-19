package co.moderniscope.analyzer.impl;

import co.moderniscope.analyzer.api.DependencyGraph;
import co.moderniscope.analyzer.api.LanguageAnalyzer;
import co.moderniscope.analyzer.api.RelationshipTypes;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Java language analyzer implementation.
 */
public class JavaAnalyzer implements LanguageAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(JavaAnalyzer.class);
    private final CombinedTypeSolver typeSolver;

    public JavaAnalyzer() {
        // Set up the type solver for resolving references
        this.typeSolver = new CombinedTypeSolver();
        this.typeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser with the symbol solver
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = new ParserConfiguration();
        config.setSymbolResolver(symbolSolver);
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        StaticJavaParser.setConfiguration(config);
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return List.of(".java");
    }

    @Override
    public void addSourceDirectory(Path sourceDir) {
        try {
            typeSolver.add(new JavaParserTypeSolver(sourceDir));
        } catch (Exception e) {
            logger.error("Error adding source directory to type solver: {}", e.getMessage());
        }
    }

    @Override
    public boolean analyzeFile(Path file, DependencyGraph graph) {
        try {
            logger.info("Analyzing Java file: {}", file);

            CompilationUnit cu = StaticJavaParser.parse(file);
            String packageName = cu.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("");

            // Import dependencies
            cu.findAll(com.github.javaparser.ast.ImportDeclaration.class).forEach(importDecl -> {
                String importName = importDecl.getNameAsString();
                graph.addDependency(packageName, importName, RelationshipTypes.IMPORTS);
            });

            // Class/Interface analysis
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                String className = packageName.isEmpty() ?
                        classDecl.getNameAsString() :
                        packageName + "." + classDecl.getNameAsString();

                // Track inheritance relationships
                if (classDecl.getExtendedTypes().isNonEmpty()) {
                    for (var extendedType : classDecl.getExtendedTypes()) {
                        try {
                            ResolvedReferenceTypeDeclaration resolved = extendedType.resolve()
                                    .asReferenceType().getTypeDeclaration().orElseThrow();
                            String superClassName = resolved.getQualifiedName();
                            graph.addDependency(className, superClassName, RelationshipTypes.EXTENDS);
                        } catch (Exception e) {
                            // Resolution might fail for external types
                            String rawName = extendedType.getNameAsString();
                            graph.addDependency(className, rawName, RelationshipTypes.EXTENDS);
                            logger.debug("Could not resolve extended type {}: {}", rawName, e.getMessage());
                        }
                    }
                }

                // Track interface implementations
                if (classDecl.getImplementedTypes().isNonEmpty()) {
                    for (var implementedType : classDecl.getImplementedTypes()) {
                        try {
                            ResolvedReferenceTypeDeclaration resolved = implementedType.resolve()
                                    .asReferenceType().getTypeDeclaration().orElseThrow();
                            String interfaceName = resolved.getQualifiedName();
                            graph.addDependency(className, interfaceName, RelationshipTypes.IMPLEMENTS);
                        } catch (Exception e) {
                            String rawName = implementedType.getNameAsString();
                            graph.addDependency(className, rawName, RelationshipTypes.IMPLEMENTS);
                            logger.debug("Could not resolve implemented type {}: {}", rawName, e.getMessage());
                        }
                    }
                }

                // Analyze fields
                classDecl.findAll(FieldDeclaration.class).forEach(field -> {
                    for (VariableDeclarator variable : field.getVariables()) {
                        try {
                            ResolvedReferenceType type = variable.getType().resolve().asReferenceType();
                            String fieldTypeName = type.getQualifiedName();
                            graph.addDependency(className, fieldTypeName, RelationshipTypes.HAS_FIELD);
                        } catch (Exception e) {
                            // If resolution fails, use the raw type name
                            String rawTypeName = variable.getType().asString();
                            if (!rawTypeName.equals("var") && !isPrimitive(rawTypeName)) {
                                graph.addDependency(className, rawTypeName, RelationshipTypes.HAS_FIELD);
                                logger.debug("Could not resolve field type {}: {}", rawTypeName, e.getMessage());
                            }
                        }
                    }
                });

                // Analyze method calls
                classDecl.findAll(MethodCallExpr.class).forEach(methodCall -> {
                    try {
                        ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
                        String targetClass = resolvedMethod.declaringType().getQualifiedName();
                        graph.addDependency(className, targetClass, RelationshipTypes.CALLS_METHOD);
                    } catch (Exception e) {
                        logger.debug("Could not resolve method call {}: {}",
                                methodCall.getNameAsString(), e.getMessage());
                    }
                });

                // Track object instantiations
                classDecl.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
                    try {
                        ResolvedReferenceTypeDeclaration type = newExpr.getType().resolve()
                                .asReferenceType().getTypeDeclaration().orElseThrow();
                        String instantiatedType = type.getQualifiedName();
                        graph.addDependency(className, instantiatedType, RelationshipTypes.INSTANTIATES);
                    } catch (Exception e) {
                        String rawTypeName = newExpr.getType().getNameAsString();
                        graph.addDependency(className, rawTypeName, RelationshipTypes.INSTANTIATES);
                        logger.debug("Could not resolve instantiated type {}: {}",
                                rawTypeName, e.getMessage());
                    }
                });
            });

            return true;
        } catch (IOException e) {
            logger.error("Error analyzing file {}: {}", file, e.getMessage());
            return false;
        }
    }

    private boolean isPrimitive(String typeName) {
        return typeName.equals("byte") || typeName.equals("short") || typeName.equals("int") ||
                typeName.equals("long") || typeName.equals("float") || typeName.equals("double") ||
                typeName.equals("boolean") || typeName.equals("char");
    }
}