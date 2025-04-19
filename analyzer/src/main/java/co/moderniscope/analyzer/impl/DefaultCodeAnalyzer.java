package co.moderniscope.analyzer.impl;

import co.moderniscope.analyzer.api.CodeAnalyzer;
import co.moderniscope.analyzer.api.DependencyGraph;
import co.moderniscope.analyzer.api.LanguageAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Default implementation of CodeAnalyzer that supports multiple programming languages.
 */
public class DefaultCodeAnalyzer implements CodeAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCodeAnalyzer.class);

    private final DependencyGraph graph;
    private final Map<String, LanguageAnalyzer> languageAnalyzers;

    public DefaultCodeAnalyzer(DependencyGraph graph) {
        this.graph = graph;
        this.languageAnalyzers = new HashMap<>();
    }

    /**
     * Registers a language analyzer for specific file extensions.
     *
     * @param analyzer The language analyzer to register
     */
    public void registerLanguageAnalyzer(LanguageAnalyzer analyzer) {
        for (String extension : analyzer.getSupportedFileExtensions()) {
            languageAnalyzers.put(extension, analyzer);
        }
    }

    @Override
    public void addSourceDirectory(Path sourceDir) {
        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("Not a directory: " + sourceDir);
        }

        for (LanguageAnalyzer analyzer : new HashSet<>(languageAnalyzers.values())) {
            analyzer.addSourceDirectory(sourceDir);
        }
    }

    @Override
    public void analyzeProject(Path projectRoot) {
        if (!Files.isDirectory(projectRoot)) {
            throw new IllegalArgumentException("Not a directory: " + projectRoot);
        }

        logger.info("Analyzing project at: {}", projectRoot.toAbsolutePath());

        // Find source directories and add them to analyzers
        findSourceDirectories(projectRoot).forEach(this::addSourceDirectory);

        // Find and analyze all source files
        try (Stream<Path> files = Files.walk(projectRoot)) {
            List<Path> sourceFiles = files
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .toList();

            logger.info("Found {} source files to analyze.", sourceFiles.size());

            int successCount = 0;
            for (Path file : sourceFiles) {
                String extension = getFileExtension(file);
                LanguageAnalyzer analyzer = languageAnalyzers.get(extension);

                if (analyzer != null) {
                    try {
                        boolean success = analyzer.analyzeFile(file, graph);
                        if (success) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        logger.error("Error analyzing file {}: {}", file, e.getMessage(), e);
                    }
                }
            }

            logger.info("Successfully analyzed {} out of {} files.", successCount, sourceFiles.size());

        } catch (IOException e) {
            logger.error("Error walking project directory", e);
        }
    }

    @Override
    public DependencyGraph getDependencyGraph() {
        return graph;
    }

    private boolean isSupportedFile(Path file) {
        String extension = getFileExtension(file);
        return languageAnalyzers.containsKey(extension);
    }

    private String getFileExtension(Path file) {
        String filename = file.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex) : "";
    }

    private List<Path> findSourceDirectories(Path projectRoot) {
        List<Path> sourceDirs = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(projectRoot, 5)) { // Limit depth to 5
            paths.filter(Files::isDirectory)
                    .filter(this::isPotentialSourceDir)
                    .forEach(sourceDirs::add);
        } catch (IOException e) {
            logger.error("Error finding source directories", e);
        }
        return sourceDirs;
    }

    private boolean isPotentialSourceDir(Path dir) {
        String name = dir.getFileName().toString().toLowerCase();
        return name.equals("src") || name.equals("java") || name.equals("kotlin") ||
                name.equals("js") || name.equals("python") || name.equals("source") ||
                name.equals("main") || name.equals("app");
    }
}