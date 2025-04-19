package co.moderniscope.analyzer.api;

/**
 * Entry point for creating language-specific analyzers.
 */
public interface AnalyzerFactory {
    /**
     * Creates a generic multi-language code analyzer.
     *
     * @return A new CodeAnalyzer instance
     */
    CodeAnalyzer createAnalyzer();

    /**
     * Creates a language-specific analyzer.
     *
     * @param language The programming language to analyze
     * @return A language-specific analyzer
     * @throws UnsupportedOperationException if the language is not supported
     */
    LanguageAnalyzer createLanguageAnalyzer(ProgrammingLanguage language);
}