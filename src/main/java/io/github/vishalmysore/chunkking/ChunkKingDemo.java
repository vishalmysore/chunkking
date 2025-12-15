package io.github.vishalmysore.chunkking;

import io.github.vishalmysore.rag.ChunkingStrategy;
import io.github.vishalmysore.rag.EmbeddingProvider;
import io.github.vishalmysore.rag.OpenAIEmbeddingProvider;
import io.github.vishalmysore.rag.chunking.ContextualChunking;
import io.github.vishalmysore.rag.chunking.SlidingWindowChunking;
import io.github.vishalmysore.rag.ContextGenerator;
import io.github.vishalmysore.rag.SimpleContextGenerator;
import io.github.vishalmysore.rag.RAGService;
import io.github.vishalmysore.rag.SearchResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ChunkKing - Demonstrates Context Problem and Context-Sensitive Chunking
 * 
 * This example shows the difference between traditional chunking (chunk-then-embed)
 * and Contextual Chunking (adding document context to each chunk) for handling 
 * long-distance contextual dependencies.
 * 
 * Problem: When text is split into chunks BEFORE embedding, anaphoric references like
 * "it", "the city", "this" lose their connection to what they reference, leading to
 * poor quality embeddings and retrieval failures.
 * 
 * Solution: Contextual Chunking adds document-level context to each chunk using an LLM,
 * helping the embedding model understand references even when chunks are processed separately.
 * This dramatically improves matching on sentences with anaphoric references.
 * 
 * Usage: java ChunkKingDemo <openai-api-key>
 */
public class ChunkKingDemo {
    
    /**
     * Wikipedia article about Berlin (simplified)
     */
    private static final String BERLIN_ARTICLE = 
        "Berlin is the capital and largest city of Germany, both by area and by population. " +
        "Its more than 3.85 million inhabitants make it the European Union's most populous city, " +
        "as measured by population within city limits. " +
        "The city is also one of the states of Germany, and is the third smallest state in the country in terms of area. " +
        "Berlin is surrounded by the state of Brandenburg and contiguous with Potsdam, Brandenburg's capital. " +
        "The city has a temperate oceanic climate. " +
        "Its economy is based on high-tech firms and the service sector. " +
        "It is a world city of culture, politics, media and science.";
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ChunkKingDemo <openai-api-key>");
            System.err.println("Example: java ChunkKingDemo sk-proj-...");
            System.exit(1);
        }
        
        String apiKey = args[0];
        
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              ChunkKing: Late Chunking Demonstration                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        demonstrateContextProblem();
        System.out.println();
        demonstrateLateChunkingConcept(apiKey);
        System.out.println();
        demonstrateContextSensitiveChunking();
        System.out.println();
        demonstrateNumericalResults(apiKey);
    }
    
    /**
     * Demonstrates the context problem with traditional chunking
     */
    private static void demonstrateContextProblem() {
        System.out.println("┌─ CONTEXT PROBLEM ─────────────────────────────────────────────────┐");
        System.out.println("│ Long distance contextual dependencies are poorly handled when     │");
        System.out.println("│ relevant information is spread over multiple chunks.               │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // Split into sentences (traditional approach)
        String[] sentences = BERLIN_ARTICLE.split("\\. ");
        
        System.out.println("Traditional Chunking (Sentence-level):");
        System.out.println("─────────────────────────────────────────────────────────────────────");
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (!sentence.endsWith(".")) sentence += ".";
            System.out.printf("Chunk %d: %s%n", i + 1, sentence);
            
            // Highlight anaphoric references
            if (sentence.contains("Its ") || sentence.contains("it ") || 
                sentence.contains("The city") || sentence.contains("It ")) {
                System.out.println("         ⚠️  Contains anaphoric reference - loses context!");
            }
            System.out.println();
        }
        
        System.out.println("Problem: Phrases like \"Its\", \"The city\", \"It\" reference \"Berlin\"");
        System.out.println("from the first sentence, but after chunking, the embedding model");
        System.out.println("cannot link them to the entity, producing low-quality embeddings.");
        System.out.println();
        
        System.out.println("Example Query Failure:");
        System.out.println("  Query: \"What is the population of Berlin?\"");
        System.out.println("  Issue: City name and population are in different chunks!");
        System.out.println("         Chunk 1 has \"Berlin\" but no population.");
        System.out.println("         Chunk 2 has population but only \"Its\" (no Berlin).");
        System.out.println("  Result: ❌ RAG system cannot answer the question correctly.");
    }
    
    /**
     * Demonstrates Late Chunking concept by comparing embeddings with and without context
     */
    private static void demonstrateLateChunkingConcept(String apiKey) {
        System.out.println("┌─ LATE CHUNKING: EMBEDDING WITH CONTEXT ───────────────────────────┐");
        System.out.println("│ Shows how embeddings differ when text is embedded WITH vs         │");
        System.out.println("│ WITHOUT full document context                                     │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        try {
            OpenAIEmbeddingProvider embeddings = new OpenAIEmbeddingProvider(
                apiKey,
                "text-embedding-3-small",
                1536  // Full dimensions for comparison
            );
            
            // The chunk we want to compare
            String chunk2Only = "Its more than 3.85 million inhabitants make it the European Union's most populous city.";
            
            // Full document with context
            String fullDoc = BERLIN_ARTICLE;
            
            System.out.println("Chunk (isolated):");
            System.out.println("  \"" + chunk2Only + "\"");
            System.out.println();
            System.out.println("Full Document:");
            System.out.println("  \"" + truncate(fullDoc, 100) + "...\"");
            System.out.println();
            
            // Get embeddings
            System.out.println("Computing embeddings...");
            float[] embChunkOnly = embeddings.embed(chunk2Only);
            float[] embFullDoc = embeddings.embed(fullDoc);
            
            // Show first 12 dimensions
            System.out.println("\n=== First 12 Dimensions ===");
            System.out.print("Chunk only    : [");
            for (int i = 0; i < 12; i++) {
                System.out.printf("%.4f%s", embChunkOnly[i], i < 11 ? ", " : "");
            }
            System.out.println("]");
            
            System.out.print("Full document : [");
            for (int i = 0; i < 12; i++) {
                System.out.printf("%.4f%s", embFullDoc[i], i < 11 ? ", " : "");
            }
            System.out.println("]");
            
            // Calculate differences
            System.out.println("\n=== Vector Difference (first 12 dims) ===");
            System.out.print("[");
            for (int i = 0; i < 12; i++) {
                System.out.printf("%.4f%s", embFullDoc[i] - embChunkOnly[i], i < 11 ? ", " : "");
            }
            System.out.println("]");
            
            // Calculate similarity metrics
            double cosineSim = cosineSimilarity(embChunkOnly, embFullDoc);
            double l2Dist = l2Distance(embChunkOnly, embFullDoc);
            
            System.out.println("\n=== Similarity Metrics ===");
            System.out.printf("Cosine similarity : %.4f\n", cosineSim);
            System.out.printf("L2 distance       : %.4f\n", l2Dist);
            
            System.out.println("\n🔍 INTERPRETATION:");
            if (cosineSim < 0.95) {
                System.out.println("  ⚠️  Vectors are DIFFERENT (cosine < 0.95)");
                System.out.println("  → Embedding with full context changes the representation!");
                System.out.println("  → The word 'Its' gets linked to 'Berlin' in full context");
                System.out.println("  → Isolated chunk loses this semantic connection");
            } else {
                System.out.println("  ✓ Vectors are very similar (cosine >= 0.95)");
            }
            System.out.println();
            
            System.out.println("💡 LATE CHUNKING INSIGHT:");
            System.out.println("  Traditional: chunk → embed each separately");
            System.out.println("  Late Chunking: embed full doc → then extract chunk embeddings");
            System.out.println("  Result: Each chunk's embedding knows about the full context!");
            
            embeddings.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrates how context-sensitive chunking solves the problem
     */
    private static void demonstrateContextSensitiveChunking() {
        System.out.println("┌─ CONTEXT-SENSITIVE CHUNKING SOLUTION ─────────────────────────────┐");
        System.out.println("│ Contextual Chunking: Add document context to each chunk           │");
        System.out.println("│ Uses LLM to generate contextual summaries for better retrieval    │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        System.out.println("Traditional Encoding (LEFT):               Contextual Chunking (RIGHT):");
        System.out.println("─────────────────────────────              ────────────────────────");
        System.out.println("1. Chunk text a priori                     1. Chunk text into segments");
        System.out.println("   (sentences/paragraphs)                     ");
        System.out.println("                                           2. Generate document context");
        System.out.println("2. Embed each chunk                           using LLM");
        System.out.println("   separately                              ");
        System.out.println("                                           3. Prepend context to each");
        System.out.println("3. ❌ Lose cross-chunk                         chunk before embedding");
        System.out.println("   context                                ");
        System.out.println("                                           4. ✅ Each chunk has");
        System.out.println("                                              document-level context");
        System.out.println();
        
        System.out.println("How Contextual Chunking Works:");
        System.out.println("─────────────────────────────────────────────────────────────────────");
        System.out.println("Step 1: Chunk the document (e.g., by sentences or paragraphs)");
        System.out.println("  Chunk 1: \"Berlin is the capital...\"");
        System.out.println("  Chunk 2: \"Its more than 3.85 million inhabitants...\"");
        System.out.println();
        System.out.println("Step 2: Generate document-level context using LLM");
        System.out.println("  Context: \"This document discusses Berlin, the capital of Germany\"");
        System.out.println();
        System.out.println("Step 3: Prepend context to each chunk before embedding");
        System.out.println("  Enhanced Chunk 1: \"[Context: Berlin, Germany...] Berlin is the capital...\"");
        System.out.println("  Enhanced Chunk 2: \"[Context: Berlin, Germany...] Its more than 3.85 million...\"");
        System.out.println();
        System.out.println("Result: The pronoun \"Its\" is now embedded alongside \"Berlin\",");
        System.out.println("        making it a much better match for queries about Berlin!");
    }
    
    /**
     * Demonstrates numerical results comparing traditional vs Contextual Chunking
     */
    private static void demonstrateNumericalResults(String apiKey) {
        System.out.println("┌─ NUMERICAL RESULTS ───────────────────────────────────────────────┐");
        System.out.println("│ Computing REAL embedding similarity using OpenAI                   │");
        System.out.println("│ Comparing ACTUAL chunking strategies from agenticmemory library!  │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // Create OpenAI embedding provider
        System.out.println("Initializing OpenAI embeddings (text-embedding-3-small, 1024d)...");
        OpenAIEmbeddingProvider embeddings = new OpenAIEmbeddingProvider(
            apiKey,
            "text-embedding-3-small",
            1024  // Lucene max dimension
        );
        
        try {
            System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║  STRATEGY 1: Traditional Chunking (SlidingWindowChunking)     ║");
            System.out.println("║  (Chunks first, then embeds each chunk separately)            ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
            
            // Strategy 1: Traditional chunking (SlidingWindowChunking)
            SlidingWindowChunking traditionalChunking = new SlidingWindowChunking(50, 10);
            
            // Create temporary indices for comparison
            String tradIndexPath = "temp-traditional-index";
            String contextualIndexPath = "temp-contextual-index";
            
            try {
                // Index 1: Traditional chunking with RAGService
                System.out.println("Indexing with traditional chunking...");
                try (RAGService tradRAG = new RAGService(Paths.get(tradIndexPath), embeddings)) {
                    List<String> tradChunks = traditionalChunking.chunk(BERLIN_ARTICLE);
                    System.out.println("Traditional chunks created: " + tradChunks.size());
                    for (int i = 0; i < tradChunks.size(); i++) {
                        tradRAG.addDocument("berlin-trad-" + i, tradChunks.get(i));
                    }
                    System.out.println("Traditional chunking complete!");
                }
                
                System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
                System.out.println("║  STRATEGY 2: Contextual Chunking (Manual Context Addition)    ║");
                System.out.println("║  (Adds document context prefix to each chunk)                 ║");
                System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
                
                // Strategy 2: Manual contextual chunking
                System.out.println("Creating contextual chunks manually...");
                SlidingWindowChunking baseStrategy = new SlidingWindowChunking(50, 10);
                List<String> baseChunks = baseStrategy.chunk(BERLIN_ARTICLE);
                
                // Generate simple context manually
                String context = "This document is about Berlin, the capital and largest city of Germany.";
                
                System.out.println("Adding context to each chunk...");
                try (RAGService contextRAG = new RAGService(Paths.get(contextualIndexPath), embeddings)) {
                    for (int i = 0; i < baseChunks.size(); i++) {
                        String enhancedChunk = "[CONTEXT: " + context + "] " + baseChunks.get(i);
                        contextRAG.addDocument("berlin-context-" + i, enhancedChunk);
                    }
                    System.out.println("Contextual chunks indexed: " + baseChunks.size());
                }
                
                System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
                System.out.println("║  COMPARISON: Using RAGService Vector Search                    ║");
                System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
                
                // Test queries
                String[] queries = {
                    "Berlin population",
                    "What is Berlin's economy based on?",
                    "Is Berlin a state?"
                };
                
                for (String query : queries) {
                    System.out.println("Query: \"" + query + "\"");
                    System.out.println("─────────────────────────────────────────────────────────────────");
                    
                    // Search traditional
                    List<SearchResult> tradResults;
                    try (RAGService tradRAG = new RAGService(Paths.get(tradIndexPath), embeddings)) {
                        tradResults = tradRAG.search(query, 3);
                    }
                    
                    System.out.println("\nTRADITIONAL CHUNKING RESULTS:");
                    for (int i = 0; i < tradResults.size(); i++) {
                        System.out.printf("  %d. Score: %.4f | %s%n", 
                            i + 1,
                            tradResults.get(i).getScore(), 
                            truncate(tradResults.get(i).getContent(), 80));
                    }
                    
                    // Search contextual
                    List<SearchResult> contextResults;
                    try (RAGService contextRAG = new RAGService(Paths.get(contextualIndexPath), embeddings)) {
                        contextResults = contextRAG.search(query, 3);
                    }
                    
                    System.out.println("\nCONTEXTUAL CHUNKING RESULTS:");
                    for (int i = 0; i < contextResults.size(); i++) {
                        String content = contextResults.get(i).getContent();
                        // Remove the context prefix for display, but it was used during embedding
                        if (content.startsWith("[CONTEXT:")) {
                            int endContext = content.indexOf("] ");
                            if (endContext > 0) {
                                content = content.substring(endContext + 2);
                            }
                        }
                        System.out.printf("  %d. Score: %.4f | %s%n", 
                            i + 1,
                            contextResults.get(i).getScore(), 
                            truncate(content, 80));
                    }
                    
                    // Compare top results
                    double tradScore = tradResults.isEmpty() ? 0.0 : tradResults.get(0).getScore();
                    double contextScore = contextResults.isEmpty() ? 0.0 : contextResults.get(0).getScore();
                    double improvement = ((contextScore - tradScore) / tradScore) * 100;
                    
                    System.out.printf("\n📊 Top Result Comparison: %.2f%% %s\n", 
                        Math.abs(improvement),
                        improvement > 0 ? "IMPROVEMENT ✅" : (improvement < -5 ? "WORSE ⚠️" : "SIMILAR"));
                    System.out.println();
                }
                
            } finally {
                // Cleanup temp indices
                deleteDirectory(Paths.get(tradIndexPath));
                deleteDirectory(Paths.get(contextualIndexPath));
            }
            
            System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║  KEY FINDINGS                                                  ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Traditional Chunking (SlidingWindowChunking):");
            System.out.println("   → Chunks text first, then embeds each chunk separately");
            System.out.println("   → Each chunk has NO CONTEXT from the document");
            System.out.println("   → Anaphoric references (\"Its\", \"The city\") lose meaning\n");
            
            System.out.println("2. Contextual Chunking:");
            System.out.println("   → Adds document-level context to each chunk");
            System.out.println("   → Context is included during embedding process");
            System.out.println("   → Each chunk understands what pronouns refer to");
            System.out.println("   → Anaphoric references maintain connection to \"Berlin\"\n");
            
            System.out.println("3. How it works:");
            System.out.println("   → Context: \"This document is about Berlin...\"");
            System.out.println("   → Chunk: \"Its more than 3.85 million inhabitants...\"");
            System.out.println("   → Embedded: \"[CONTEXT: Berlin...] Its more than 3.85...\"");
            System.out.println("   → Result: \"Its\" is now semantically linked to \"Berlin\"!\n");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            embeddings.close();
            System.out.println("Embedding provider closed.");
        }
    }
    
    /**
     * Delete directory recursively
     */
    private static void deleteDirectory(java.nio.file.Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            // Ignore
                        }
                    });
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Calculate cosine similarity between two vectors
     */
    private static double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    /**
     * Calculate L2 (Euclidean) distance between two vectors
     */
    private static double l2Distance(float[] a, float[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
