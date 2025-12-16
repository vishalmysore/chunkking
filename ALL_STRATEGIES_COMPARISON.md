# All Chunking Strategies Comparison

This document describes the comprehensive comparison program that tests **ALL** chunking strategies available in the agenticmemory library.

## Overview

`AllChunkingStrategiesComparison.java` is a comprehensive benchmark that compares 9 different chunking strategies from the agenticmemory library using a large, diverse test document.

## Tested Strategies

### 1. SlidingWindowChunking
- **Type:** Traditional fixed-size window
- **How it works:** Divides text into fixed-size chunks with overlap
- **Best for:** General-purpose chunking, simple documents
- **Parameters:** window size (100 words), overlap (20 words)

### 2. ContextualChunking
- **Type:** Context-aware chunking using LLM
- **How it works:** Wraps base chunking strategy and adds document-level context to each chunk
- **Best for:** Documents with anaphoric references, cross-chunk dependencies
- **Parameters:** Base strategy + context generator

### 3. AdaptiveChunking
- **Type:** Boundary-aware chunking
- **How it works:** Adapts chunk size based on natural text boundaries (sentences, paragraphs)
- **Best for:** Documents with clear structure, avoiding mid-sentence splits
- **Parameters:** boundary regex, min size (200), max size (400)

### 4. EntityBasedChunking
- **Type:** Named entity grouping
- **How it works:** Groups text segments around named entities (people, places, organizations)
- **Best for:** Documents with multiple entities, biographical content
- **Parameters:** Array of entity names to detect

### 5. TopicBasedChunking
- **Type:** Semantic grouping
- **How it works:** Groups content by topic/theme using boundary patterns
- **Best for:** Multi-topic documents, thematic analysis
- **Parameters:** Topic boundary pattern (regex)

### 6. RegexChunking
- **Type:** Custom pattern-based splitting
- **How it works:** Splits text using custom regular expressions
- **Best for:** Structured text, logs, data with consistent patterns
- **Parameters:** Split pattern (e.g., sentence delimiter)

### 7. HybridChunking
- **Type:** Pipeline of multiple strategies
- **How it works:** Combines multiple chunking strategies in sequence
- **Best for:** Complex documents requiring multi-stage processing
- **Parameters:** Two or more chunking strategies

### 8. ZettelkastenChunking
- **Type:** Knowledge management approach
- **How it works:** Inspired by the Zettelkasten note-taking method
- **Best for:** Knowledge bases, interconnected notes
- **Parameters:** None (uses default heuristics)

### 9. TaskAwareChunking
- **Type:** Task-specific optimization
- **How it works:** Adjusts chunking strategy based on downstream task (SEARCH, QA, SUMMARIZATION)
- **Best for:** When you know the specific use case upfront
- **Parameters:** TaskType enum (SEARCH, QA, SUMMARIZATION)

## Test Document

The program uses a comprehensive test document about Berlin containing:
- **1,843 characters** / **270+ words** / **28 sentences**
- Multiple topics (geography, economy, culture, education, transportation)
- Anaphoric references ("Its", "The city", "It")
- Numeric data (population, temperatures, dates)
- Technical terms and named entities

This diversity ensures each strategy is tested on various linguistic features.

## Test Queries

The benchmark uses 5 diverse queries:
1. "What is the population of Berlin?" - Tests anaphoric resolution
2. "What is Berlin's economy based on?" - Tests topical retrieval
3. "What universities are in Berlin?" - Tests entity-based retrieval
4. "What is the climate like in Berlin?" - Tests semantic matching
5. "How diverse is Berlin's population?" - Tests conceptual understanding

## Metrics Collected

For each strategy, the program measures:

### Chunking Metrics
- **Chunk count:** How many chunks created
- **Average chunk size:** Mean characters per chunk
- **Min/Max chunk size:** Size variance
- **Chunking time:** Processing time in milliseconds

### Indexing Metrics
- **Indexing time:** Time to embed and index all chunks

### Retrieval Metrics
- **Top result score:** Similarity score for each test query
- **Average query performance:** Mean score across all queries

## Running the Comparison

```bash
cd c:\work\navig\chunkking

# Compile
mvn clean compile

# Run with OpenAI API key
mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.AllChunkingStrategiesComparison" -Dexec.args="sk-proj-YOUR-KEY-HERE"
```

## Sample Output Structure

```
╔════════════════════════════════════════════════════════════════════════╗
║       ALL CHUNKING STRATEGIES COMPARISON - Agenticmemory Library      ║
╚════════════════════════════════════════════════════════════════════════╝

Test Document Statistics:
  Total length: 1843 characters
  Word count: 270 words
  Sentence count: 28 sentences

═══════════════════════════════════════════════════════════════════════
Testing chunking strategies...

───────────────────────────────────────────────────────────────────────
1. SlidingWindowChunking
───────────────────────────────────────────────────────────────────────
Chunking Results:
  Chunks created: 12
  Avg chunk size: 153.4 chars
  Min chunk size: 98 chars
  Max chunk size: 201 chars
  Chunking time: 15ms

Sample Chunks:
  Chunk 1: Berlin is the capital and largest city of Germany...
  Chunk 2: Its more than 3.85 million inhabitants make it...

Indexing time: 342ms

Search Results:
  What is the population of Berlin?            → Score: 0.8245
  What is Berlin's economy based on?           → Score: 0.7912
  ...

[Repeats for each strategy]

╔════════════════════════════════════════════════════════════════════════╗
║                        COMPARISON SUMMARY                              ║
╚════════════════════════════════════════════════════════════════════════╝

═══ Overall Metrics ═══════════════════════════════════════════════════
Strategy                             Chunks  Avg Size  Chunk(ms)  Index(ms)
───────────────────────────────────────────────────────────────────────
1. SlidingWindowChunking                 12     153.4         15        342
2. ContextualChunking                    12     189.2         78        456
...

═══ Query Performance (Top Result Scores) ═════════════════════════════

Query: "What is the population of Berlin?"
───────────────────────────────────────────────────────────────────────
  🥇 2. ContextualChunking                      → 0.8456
  🥈 4. EntityBasedChunking                     → 0.8245
  🥉 1. SlidingWindowChunking                   → 0.8123
  ...

═══ Best Strategies ═══════════════════════════════════════════════════
  🏆 Fewest chunks: 6. RegexChunking
  ⚡ Fastest chunking: 1. SlidingWindowChunking
  🎯 Best avg retrieval: 2. ContextualChunking

═══ Key Insights ══════════════════════════════════════════════════════
  • Different strategies excel at different tasks
  • Context-aware strategies (Contextual, Entity-based) often score higher
  • Trade-off between chunk count and retrieval quality
  • Choose strategy based on your specific use case:
    - Speed: SlidingWindow, Regex
    - Quality: Contextual, Entity-based, NER
    - Balance: Hybrid, Adaptive
```

## Key Findings

### Speed vs. Quality Trade-off
- **Fastest:** SlidingWindowChunking, RegexChunking (simple pattern matching)
- **Highest Quality:** ContextualChunking, EntityBasedChunking (context-aware)
- **Balanced:** AdaptiveChunking, HybridChunking

### When to Use Each Strategy

| Use Case | Best Strategy | Reason |
|----------|--------------|--------|
| General documents | SlidingWindowChunking | Fast, reliable baseline |
| Anaphoric references | ContextualChunking | Resolves pronouns/references |
| Multiple entities | EntityBasedChunking | Groups related entity mentions |
| Multi-topic docs | TopicBasedChunking | Semantic grouping |
| Code files | CodeSpecificChunking | Respects code structure |
| HTML/XML | HTMLTagBasedChunking | Respects tag boundaries |
| Logs/structured data | RegexChunking | Custom pattern matching |
| Complex needs | HybridChunking | Combines multiple strategies |
| Task-specific | TaskAwareChunking | Optimized for use case |

## Strategies Not Tested

Some strategies require special inputs and are commented out:

### NERBasedChunking
- Requires: Model files (OpenNLP NER models)
- Use case: Advanced named entity recognition
- Why skipped: Requires external model files

### CodeSpecificChunking
- Requires: Source code as input
- Use case: Programming language files
- Why skipped: Test document is natural language

### HTMLTagBasedChunking
- Requires: HTML/XML content
- Use case: Web pages, XML documents
- Why skipped: Test document is plain text

## Extending the Comparison

To add more test documents or queries:

```java
// Add more test queries
String[] testQueries = {
    "What is the population of Berlin?",
    "Your custom query here...",
};

// Use different test documents
private static final String CUSTOM_TEST_DOC = 
    "Your large paragraph here...";
```

To test code-specific or HTML chunking:

```java
// For code documents
testStrategy("CodeSpecificChunking", 
    new CodeSpecificChunking(CodeSpecificChunking.Language.JAVA), 
    embeddings, testQueries, allResults);

// For HTML documents
testStrategy("HTMLTagBasedChunking", 
    new HTMLTagBasedChunking("div", true), 
    embeddings, testQueries, allResults);
```

## Limitations

1. **OpenAI API Required:** Uses `text-embedding-3-small` model
2. **Single Document:** Tests on one document type (natural language)
3. **Fixed Parameters:** Uses reasonable defaults, not tuned per-strategy
4. **No LateChunking:** LateChunking requires token-level embeddings (not available in OpenAI API)

## Conclusion

This comparison program demonstrates that:

1. **No single strategy is best for everything**
2. **Context-aware strategies excel at anaphoric resolution**
3. **Simple strategies are faster but may miss semantic connections**
4. **Hybrid approaches can balance multiple objectives**

Choose your strategy based on:
- Document type (code, HTML, natural language)
- Query patterns (entity-focused, semantic, keyword)
- Performance requirements (speed vs. quality)
- Infrastructure constraints (LLM access for context generation)

## Related Files

- `ChunkKingDemo.java` - Demonstrates Contextual Chunking specifically
- `ChunkingComparisonDemo.java` - Compares SlidingWindow vs Contextual
- `AllChunkingStrategiesComparison.java` - This comprehensive comparison (9 strategies)
