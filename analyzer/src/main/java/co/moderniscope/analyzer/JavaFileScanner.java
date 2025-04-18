package co.moderniscope.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JavaFileScanner {

    public void scanAndBuildGraph(String rootPath) {
        Path path = Paths.get(rootPath);
        System.out.println("Scanning Java files in: " + path.toAbsolutePath());

        try (Stream<Path> pathStream = Files.walk(path)) {
            List<Path> javaFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();

            if (javaFiles.isEmpty()) {
                System.out.println("No Java files found in the specified directory.");
                return;
            }

            System.out.println("Found " + javaFiles.size() + " Java files.");

            DependencyGraphBuilder graphBuilder = new DependencyGraphBuilder();

            for (Path file : javaFiles) {
                try {
                    CompilationUnit cu = StaticJavaParser.parse(file);
                    Optional<PackageDeclaration> pkg = cu.getPackageDeclaration();

                    cu.findAll(ImportDeclaration.class).forEach(importDecl -> {
                        graphBuilder.addDependency(pkg.map(PackageDeclaration::getNameAsString).orElse(""),
                                importDecl.getNameAsString());
                    });
                } catch (Exception e) {
                    System.err.println("Error parsing file: " + file);
                }
            }

            graphBuilder.printGraph();
        } catch (IOException e) {
            System.err.println("Error walking the file tree: " + e.getMessage());
        }
    }
}