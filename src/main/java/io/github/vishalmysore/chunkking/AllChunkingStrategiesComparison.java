package io.github.vishalmysore.chunkking;

import io.github.vishalmysore.rag.ChunkingStrategy;
import io.github.vishalmysore.rag.OpenAIEmbeddingProvider;
import io.github.vishalmysore.rag.RAGService;
import io.github.vishalmysore.rag.SearchResult;
import io.github.vishalmysore.rag.SimpleContextGenerator;
import io.github.vishalmysore.rag.chunking.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Comprehensive Chunking Strategies Comparison
 * 
 * Compares ALL chunking strategies available in the agenticmemory library:
 * 1. SlidingWindowChunking - Traditional fixed-size window approach
 * 2. ContextualChunking - Adds LLM-generated context to chunks
 * 3. AdaptiveChunking - Adapts chunk size based on content
 * 4. EntityBasedChunking - Chunks based on named entities
 * 5. NERBasedChunking - Uses Named Entity Recognition
 * 6. TopicBasedChunking - Groups content by topic
 * 7. CodeSpecificChunking - Optimized for source code
 * 8. HTMLTagBasedChunking - Chunks HTML by tags
 * 9. RegexChunking - Custom regex-based chunking
 * 10. TaskAwareChunking - Optimizes for specific task types
 * 11. HybridChunking - Combines multiple strategies
 * 12. ZettelkastenChunking - Knowledge management approach
 * 
 * Uses a large paragraph with various linguistic features to test each strategy.
 */
public class AllChunkingStrategiesComparison {
    
    /**
     * Large test document with diverse content:
     * - Technical terms
     * - Anaphoric references
     * - Numeric data
     * - Multiple topics
     * - Complex sentence structures
     */
    private static final String LARGE_TEST_DOCUMENT = 
        "Berlin is the capital and largest city of Germany, both by area and by population. " +
        "Its more than 3.85 million inhabitants make it the European Union's most populous city, " +
        "as measured by population within city limits. The city is also one of the states of Germany, " +
        "and is the third smallest state in the country in terms of area. " +
        
        "Berlin is surrounded by the state of Brandenburg and contiguous with Potsdam, Brandenburg's capital. " +
        "The city has a temperate oceanic climate with warm summers and cold winters. " +
        "Average temperatures range from -1°C in winter to 24°C in summer. " +
        "Annual precipitation is approximately 570mm, distributed fairly evenly throughout the year. " +
        
        "Its economy is based on high-tech firms and the service sector, encompassing a diverse range of " +
        "creative industries, research facilities, media corporations and convention venues. " +
        "The city is a major technology hub and startup ecosystem in Europe. " +
        "Notable companies headquartered in Berlin include Zalando, HelloFresh, and N26. " +
        "The unemployment rate stood at 8.6% in 2022, slightly above the German average. " +
        
        "It is a world city of culture, politics, media and science. " +
        "The city has a thriving arts scene with over 175 museums, including the Pergamon Museum, " +
        "the Bode Museum, and the Neues Museum on Museum Island. " +
        "Berlin hosts three UNESCO World Heritage Sites: Museum Island, Palaces and Parks of Potsdam and Berlin, " +
        "and the Berlin Modernism Housing Estates. " +
        
        "The city's universities and research institutions are renowned internationally. " +
        "The Humboldt University of Berlin, founded in 1810, has educated 29 Nobel Prize winners. " +
        "Other major institutions include the Free University of Berlin, Technical University of Berlin, " +
        "and the Berlin University of the Arts. " +
        "Approximately 200,000 students are enrolled in Berlin's higher education institutions. " +
        
        "Berlin's transportation infrastructure is highly developed. The Berlin U-Bahn and S-Bahn " +
        "comprise 473 stations serving over 1.5 billion passengers annually. " +
        "The city is also a major rail hub with connections to all major European cities. " +
        "Berlin Brandenburg Airport, opened in 2020, handles approximately 24 million passengers per year. " +
        
        "The city's cultural diversity is reflected in its demographics. " +
        "Approximately 35% of Berlin's residents have an immigrant background, representing over 190 nations. " +
        "The largest immigrant communities are from Turkey, Poland, Russia, and Syria. " +
        "This diversity has created a vibrant multicultural atmosphere with diverse cuisine, festivals, and neighborhoods.";
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java AllChunkingStrategiesComparison <openai-api-key>");
            System.err.println("Example: java AllChunkingStrategiesComparison sk-proj-...");
            System.exit(1);
        }
        
        String apiKey = args[0];
        
        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║       ALL CHUNKING STRATEGIES COMPARISON - Agenticmemory Library      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝\n");
        
        System.out.println("Test Document Statistics:");
        System.out.println("  Total length: " + LARGE_TEST_DOCUMENT.length() + " characters");
        System.out.println("  Word count: " + LARGE_TEST_DOCUMENT.split("\\s+").length + " words");
        System.out.println("  Sentence count: " + LARGE_TEST_DOCUMENT.split("[.!?]\\s+").length + " sentences");
        System.out.println();
        
        // Create OpenAI embedding provider
        OpenAIEmbeddingProvider embeddings = new OpenAIEmbeddingProvider(
            apiKey,
            "text-embedding-3-small",
            1024
        );
        
        try {
            // Test queries covering different aspects
            String[] testQueries = {
                "What is the population of Berlin?",
                "What is Berlin's economy based on?",
                "What universities are in Berlin?",
                "What is the climate like in Berlin?",
                "How diverse is Berlin's population?"
            };
            
            // Map to store results for comparison
            Map<String, StrategyResults> allResults = new LinkedHashMap<>();
            
            // Test each strategy
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println("Testing chunking strategies...\n");
            
            testStrategy("1. SlidingWindowChunking", 
                new SlidingWindowChunking(100, 20), 
                embeddings, testQueries, allResults);
            
            testStrategy("2. ContextualChunking", 
                new ContextualChunking(new SlidingWindowChunking(100, 20), new SimpleContextGenerator()), 
                embeddings, testQueries, allResults);
            
            testStrategy("3. AdaptiveChunking", 
                new AdaptiveChunking("\\. ", 200, 400), // boundary regex, min, max chunk size
                embeddings, testQueries, allResults);
            
            testStrategy("4. EntityBasedChunking", 
                new EntityBasedChunking(new String[]{"Berlin", "Germany", "Brandenburg", "Europe"}), 
                embeddings, testQueries, allResults);
            
            // Skip NERBasedChunking as it requires model files
            // testStrategy("5. NERBasedChunking", 
            //     new NERBasedChunking(modelPath, tokenizerPath, entityTypes), 
            //     embeddings, testQueries, allResults);
            
            testStrategy("5. TopicBasedChunking", 
                new TopicBasedChunking("\\. "), // boundary pattern
                embeddings, testQueries, allResults);
            
            testStrategy("6. RegexChunking", 
                new RegexChunking("\\. "), // Split by sentence
                embeddings, testQueries, allResults);
            
            testStrategy("7. HybridChunking", 
                new HybridChunking(
                    new SlidingWindowChunking(100, 20),
                    new EntityBasedChunking(new String[]{"Berlin", "Germany"})
                ), 
                embeddings, testQueries, allResults);
            
            testStrategy("8. ZettelkastenChunking", 
                new ZettelkastenChunking(), 
                embeddings, testQueries, allResults);
            
            // TaskAwareChunking requires TaskType parameter
            testStrategy("9. TaskAwareChunking (SEARCH)", 
                new TaskAwareChunking(TaskAwareChunking.TaskType.SEARCH), 
                embeddings, testQueries, allResults);
            
            // CodeSpecificChunking requires Language parameter
            // Skipping as our test doc is not code
            // testStrategy("10. CodeSpecificChunking", 
            //     new CodeSpecificChunking(CodeSpecificChunking.Language.JAVA), 
            //     embeddings, testQueries, allResults);
            
            // HTMLTagBasedChunking requires tag and inclusive parameters
            // Skipping as our test doc is not HTML
            // testStrategy("11. HTMLTagBasedChunking", 
            //     new HTMLTagBasedChunking("p", true), 
            //     embeddings, testQueries, allResults);
            
            // Print comparison summary
            printComparisonSummary(allResults, testQueries);
            
        } catch (Exception e) {
            System.err.println("Error during comparison: " + e.getMessage());
            e.printStackTrace();
        } finally {
            embeddings.close();
            System.out.println("\n✅ Comparison complete!");
        }
    }
    
    private static void testStrategy(String name, ChunkingStrategy strategy, 
                                     OpenAIEmbeddingProvider embeddings,
                                     String[] queries, 
                                     Map<String, StrategyResults> resultsMap) throws Exception {
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.println(name);
        System.out.println("───────────────────────────────────────────────────────────────────────");
        
        Path indexPath = Paths.get("index-" + name.replaceAll("[^a-zA-Z0-9]", "-"));
        
        try {
            // Measure chunking time
            long startChunk = System.currentTimeMillis();
            List<String> chunks = strategy.chunk(LARGE_TEST_DOCUMENT);
            long chunkTime = System.currentTimeMillis() - startChunk;
            
            System.out.println("Chunking Results:");
            System.out.println("  Chunks created: " + chunks.size());
            System.out.println("  Avg chunk size: " + (chunks.isEmpty() ? 0 : 
                chunks.stream().mapToInt(String::length).average().orElse(0)) + " chars");
            System.out.println("  Min chunk size: " + (chunks.isEmpty() ? 0 :
                chunks.stream().mapToInt(String::length).min().orElse(0)) + " chars");
            System.out.println("  Max chunk size: " + (chunks.isEmpty() ? 0 :
                chunks.stream().mapToInt(String::length).max().orElse(0)) + " chars");
            System.out.println("  Chunking time: " + chunkTime + "ms");
            
            // Show first 2 chunks as examples
            System.out.println("\nSample Chunks:");
            for (int i = 0; i < Math.min(2, chunks.size()); i++) {
                System.out.println("  Chunk " + (i+1) + ": " + truncate(chunks.get(i), 80));
            }
            
            // Index and search
            deleteDirectory(indexPath);
            
            long startIndex = System.currentTimeMillis();
            try (RAGService rag = new RAGService(indexPath, embeddings)) {
                for (int i = 0; i < chunks.size(); i++) {
                    rag.addDocument("chunk-" + i, chunks.get(i));
                }
                long indexTime = System.currentTimeMillis() - startIndex;
                
                System.out.println("\nIndexing time: " + indexTime + "ms");
                
                // Test queries
                Map<String, Double> queryScores = new LinkedHashMap<>();
                System.out.println("\nSearch Results:");
                
                for (String query : queries) {
                    List<SearchResult> results = rag.search(query, 1);
                    double topScore = results.isEmpty() ? 0.0 : results.get(0).getScore();
                    queryScores.put(query, topScore);
                    System.out.printf("  %-45s → Score: %.4f%n", 
                        truncate(query, 40), topScore);
                }
                
                // Store results
                resultsMap.put(name, new StrategyResults(
                    chunks.size(),
                    chunks.stream().mapToInt(String::length).average().orElse(0),
                    chunkTime,
                    indexTime,
                    queryScores
                ));
            }
            
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("  ⚠️  Error testing " + name + ": " + e.getMessage());
            // Store empty results to maintain order
            resultsMap.put(name, new StrategyResults(0, 0, 0, 0, new LinkedHashMap<>()));
        } finally {
            deleteDirectory(indexPath);
        }
    }
    
    private static void printComparisonSummary(Map<String, StrategyResults> results, String[] queries) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        COMPARISON SUMMARY                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝\n");
        
        // Overall metrics table
        System.out.println("═══ Overall Metrics ═══════════════════════════════════════════════════");
        System.out.printf("%-35s  %6s  %8s  %9s  %9s%n", 
            "Strategy", "Chunks", "Avg Size", "Chunk(ms)", "Index(ms)");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        
        for (Map.Entry<String, StrategyResults> entry : results.entrySet()) {
            StrategyResults r = entry.getValue();
            System.out.printf("%-35s  %6d  %8.0f  %9d  %9d%n",
                truncate(entry.getKey(), 35),
                r.chunkCount,
                r.avgChunkSize,
                r.chunkingTime,
                r.indexingTime);
        }
        
        // Query performance comparison
        System.out.println("\n═══ Query Performance (Top Result Scores) ═════════════════════════════");
        
        for (String query : queries) {
            System.out.println("\nQuery: \"" + query + "\"");
            System.out.println("───────────────────────────────────────────────────────────────────────");
            
            // Collect scores for this query
            List<Map.Entry<String, Double>> queryResults = new ArrayList<>();
            for (Map.Entry<String, StrategyResults> entry : results.entrySet()) {
                Double score = entry.getValue().queryScores.get(query);
                if (score != null) {
                    queryResults.add(new AbstractMap.SimpleEntry<>(entry.getKey(), score));
                }
            }
            
            // Sort by score descending
            queryResults.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            
            // Print top 5
            for (int i = 0; i < Math.min(5, queryResults.size()); i++) {
                Map.Entry<String, Double> result = queryResults.get(i);
                String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  ";
                System.out.printf("  %s %-45s → %.4f%n",
                    medal,
                    truncate(result.getKey(), 40),
                    result.getValue());
            }
        }
        
        // Best strategy per metric
        System.out.println("\n═══ Best Strategies ═══════════════════════════════════════════════════");
        
        // Fewest chunks
        String fewestChunks = results.entrySet().stream()
            .min(Comparator.comparingInt(e -> e.getValue().chunkCount))
            .map(Map.Entry::getKey)
            .orElse("N/A");
        System.out.println("  🏆 Fewest chunks: " + fewestChunks);
        
        // Fastest chunking
        String fastestChunking = results.entrySet().stream()
            .filter(e -> e.getValue().chunkingTime > 0)
            .min(Comparator.comparingLong(e -> e.getValue().chunkingTime))
            .map(Map.Entry::getKey)
            .orElse("N/A");
        System.out.println("  ⚡ Fastest chunking: " + fastestChunking);
        
        // Best average query performance
        String bestAvgQuery = results.entrySet().stream()
            .max(Comparator.comparingDouble(e -> 
                e.getValue().queryScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0)))
            .map(Map.Entry::getKey)
            .orElse("N/A");
        System.out.println("  🎯 Best avg retrieval: " + bestAvgQuery);
        
        System.out.println("\n═══ Key Insights ══════════════════════════════════════════════════════");
        System.out.println("  • Different strategies excel at different tasks");
        System.out.println("  • Context-aware strategies (Contextual, Entity-based) often score higher");
        System.out.println("  • Trade-off between chunk count and retrieval quality");
        System.out.println("  • Choose strategy based on your specific use case:");
        System.out.println("    - Speed: SlidingWindow, Regex");
        System.out.println("    - Quality: Contextual, Entity-based, NER");
        System.out.println("    - Balance: Hybrid, Adaptive");
    }
    
    private static void deleteDirectory(Path path) throws Exception {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception e) {
                        // Ignore
                    }
                });
        }
    }
    
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Data class to store strategy test results
     */
    private static class StrategyResults {
        final int chunkCount;
        final double avgChunkSize;
        final long chunkingTime;
        final long indexingTime;
        final Map<String, Double> queryScores;
        
        StrategyResults(int chunkCount, double avgChunkSize, long chunkingTime, 
                       long indexingTime, Map<String, Double> queryScores) {
            this.chunkCount = chunkCount;
            this.avgChunkSize = avgChunkSize;
            this.chunkingTime = chunkingTime;
            this.indexingTime = indexingTime;
            this.queryScores = queryScores;
        }
    }
}
