package co.moderniscope.analyzer.impl;

import co.moderniscope.analyzer.api.AnalyzerFactory;
import co.moderniscope.analyzer.api.CodeAnalyzer;
import co.moderniscope.analyzer.api.LanguageAnalyzer;
import co.moderniscope.analyzer.api.ProgrammingLanguage;

/**
 * Default implementation of AnalyzerFactory.
 */
public class DefaultAnalyzerFactory implements AnalyzerFactory {

    @Override
    public CodeAnalyzer createAnalyzer() {
        DefaultDependencyGraph graph = new DefaultDependencyGraph();
        DefaultCodeAnalyzer analyzer = new DefaultCodeAnalyzer(graph);

        analyzer.registerLanguageAnalyzer(new JavaAnalyzer());

        return analyzer;
    }

    @Override
    public LanguageAnalyzer createLanguageAnalyzer(ProgrammingLanguage language) {
        return switch (language) {
            case JAVA -> new JavaAnalyzer();
            case KOTLIN, JAVASCRIPT, TYPESCRIPT, PYTHON, CSHARP, GO, RUST, CPP -> null;
            default -> throw new UnsupportedOperationException("Language not supported: " + language);
        };
    }
}