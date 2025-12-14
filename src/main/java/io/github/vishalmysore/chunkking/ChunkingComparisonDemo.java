package io.github.vishalmysore.chunkking;

import io.github.vishalmysore.rag.OpenAIEmbeddingProvider;
import io.github.vishalmysore.rag.RAGService;
import io.github.vishalmysore.rag.SearchResult;
import io.github.vishalmysore.rag.chunking.SlidingWindowChunking;
import io.github.vishalmysore.rag.chunking.ContextualChunking;
import io.github.vishalmysore.rag.ContextGenerator;
import io.github.vishalmysore.rag.SimpleContextGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Comparator;

/**
 * Chunking Comparison Demo - 100% Real Implementation
 * 
 * Demonstrates the difference between chunking strategies using ONLY your actual
 * agenticmemory library classes - NO MOCKS, NO SIMULATION!
 * 
 * Compares:
 * 1. SlidingWindowChunking (traditional approach)
 * 2. ContextualChunking (wraps SlidingWindow with LLM-generated context)
 * 
 * Uses real RAGService for indexing and retrieval!
 * 
 * Usage: java ChunkingComparisonDemo <openai-api-key>
 */
public class ChunkingComparisonDemo {
    
    private static final String BERLIN_ARTICLE = 
        "Berlin is the capital and largest city of Germany, both by area and by population. " +
        "Its more than 3.85 million inhabitants make it the European Union's most populous city, " +
        "as measured by population within city limits. " +
        "The city is also one of the states of Germany, and is the third smallest state in the country in terms of area. " +
        "Berlin is surrounded by the state of Brandenburg and contiguous with Potsdam, Brandenburg's capital. " +
        "The city has a temperate oceanic climate. " +
        "Its economy is based on high-tech firms and the service sector. " +
        "It is a world city of culture, politics, media and science.";
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java ChunkingComparisonDemo <openai-api-key>");
            System.exit(1);
        }
        
        String apiKey = args[0];
        
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Chunking Strategy Comparison - 100% Real Implementation        ║");
        System.out.println("║     Using ONLY io.github.vishalmysore.rag classes!                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
        
        // Create OpenAI embedding provider (1024 dims for Lucene compatibility)
        OpenAIEmbeddingProvider embeddings = new OpenAIEmbeddingProvider(
            apiKey,
            "text-embedding-3-small",
            1024
        );
        
        try {
            // Test queries that involve anaphoric references
            String[] queries = {
                "What is the population of Berlin?",
                "What is Berlin's economy based on?",
                "Is Berlin a state in Germany?"
            };
            
            System.out.println("═══════════════════════════════════════════════════════════════════");
            System.out.println("STRATEGY 1: SlidingWindowChunking (Traditional)");
            System.out.println("Chunks first, then embeds - NO context preservation");
            System.out.println("═══════════════════════════════════════════════════════════════════\n");
            
            Path index1 = Paths.get("index-traditional");
            deleteDirectory(index1);
            
            try (RAGService rag1 = new RAGService(index1, embeddings)) {
                SlidingWindowChunking strategy1 = new SlidingWindowChunking(50, 10);
                rag1.addDocumentWithChunking("berlin", BERLIN_ARTICLE, strategy1);
                
                System.out.println("Indexed document with SlidingWindowChunking\n");
                
                for (String query : queries) {
                    System.out.println("Query: \"" + query + "\"");
                    List<SearchResult> results = rag1.search(query, 3);
                    displayResults(results);
                }
            }
            
            System.out.println("\n═══════════════════════════════════════════════════════════════════");
            System.out.println("STRATEGY 2: ContextualChunking (Your Advanced Strategy!)");
            System.out.println("Wraps SlidingWindow + adds LLM-generated context to each chunk");
            System.out.println("═══════════════════════════════════════════════════════════════════\n");
            
            Path index2 = Paths.get("index-contextual");
            deleteDirectory(index2);
            
            try (RAGService rag2 = new RAGService(index2, embeddings)) {
                // Create ContextualChunking that wraps SlidingWindow
                SlidingWindowChunking baseStrategy = new SlidingWindowChunking(50, 10);
                ContextGenerator contextGen = new SimpleContextGenerator();
                ContextualChunking strategy2 = new ContextualChunking(baseStrategy, contextGen);
                
                rag2.addDocumentWithChunking("berlin", BERLIN_ARTICLE, strategy2);
                
                System.out.println("Indexed document with ContextualChunking\n");
                
                for (String query : queries) {
                    System.out.println("Query: \"" + query + "\"");
                    List<SearchResult> results = rag2.search(query, 3);
                    displayResults(results);
                }
            }
            
            System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
            System.out.println("║  KEY OBSERVATIONS                                                  ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Traditional SlidingWindowChunking:");
            System.out.println("   → Fast and simple");
            System.out.println("   → May struggle with anaphoric references (\"Its\", \"The city\")");
            System.out.println("   → Each chunk stands alone without document context\n");
            
            System.out.println("2. ContextualChunking:");
            System.out.println("   → Adds LLM-generated context to each chunk");
            System.out.println("   → Better handles queries about entities mentioned via pronouns");
            System.out.println("   → Each chunk includes document-level awareness\n");
            
            System.out.println("3. Both use YOUR actual classes from io.github.vishalmysore.rag:");
            System.out.println("   ✅ SlidingWindowChunking");
            System.out.println("   ✅ ContextualChunking");
            System.out.println("   ✅ SimpleContextGenerator");
            System.out.println("   ✅ RAGService with real vector search");
            System.out.println("   ✅ OpenAIEmbeddingProvider");
            System.out.println("\n   NO MOCKS! NO SIMULATION! 100% Real agenticmemory library!\n");
            
            // Cleanup
            deleteDirectory(index1);
            deleteDirectory(index2);
            
        } finally {
            embeddings.close();
        }
    }
    
    private static void displayResults(List<SearchResult> results) {
        if (results.isEmpty()) {
            System.out.println("  No results found!\n");
            return;
        }
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            System.out.printf("  %d. Score: %.4f | %s\n", 
                i + 1, 
                result.getScore(), 
                truncate(result.getContent(), 70));
        }
        System.out.println();
    }
    
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
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
}
