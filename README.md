# ChunkKing - Context-Sensitive Chunking Demo

A demonstration project in 100% Java showing the difference between traditional chunking and context-sensitive Late Chunking using the [Agentic Memory RAG ](https://github.com/vishalmysore/agenticmemory) library.

Inspiration   
 https://arxiv.org/pdf/2504.19754   
 https://arxiv.org/abs/2409.04701  
 https://github.com/jina-ai/late-chunking   


## The Context Problem

Traditional RAG systems face a critical challenge with **long-distance contextual dependencies**. When text is chunked BEFORE embedding, anaphoric references lose their meaning:

```
Chunk 1: "Berlin is the capital and largest city of Germany..."
Chunk 2: "Its more than 3.85 million inhabitants..." ❌ What does "Its" refer to?
Chunk 3: "The city is also one of the states..." ❌ Which city?
```

### Example Query Failure
**Query:** "What is the population of Berlin?"

- Chunk 1 contains "Berlin" but no population
- Chunk 2 contains population but only "Its" (no Berlin)
- **Result:** RAG system cannot answer the question!

## The Solution: Late Chunking

**Late Chunking** reverses the order:
1. ✅ Embed the ENTIRE document first (preserving full context)
2. ✅ Apply chunking boundaries to token-level embeddings
3. ✅ Pool contextualized tokens into chunk embeddings

### How It Works

```
Traditional Chunking:          Late Chunking:
─────────────────────          ──────────────
1. Chunk text                  1. Embed entire text
2. Embed chunks                2. Get contextual tokens
3. ❌ Lose context             3. Chunk token sequence
                               4. Pool within chunks
                               5. ✅ Keep full context
```

## Numerical Results

Comparing similarity between "Berlin" and various sentences:

| Text Chunk | Traditional | Late Chunking | Improvement |
|------------|-------------|---------------|-------------|
| "Berlin is the capital..." (explicit mention) | 0.8486 | 0.8495 | +0.0009 |
| "Its more than 3.85 million..." (anaphoric) | **0.7084** | **0.8249** | **+0.1165** 🟢 |
| "The city is also one of..." (anaphoric) | **0.7535** | **0.8498** | **+0.0963** 🟢 |

### Key Observations

1. **When entity is explicit:** Both approaches work similarly
2. **With anaphoric references:** Late Chunking provides **10-12% improvement**!
3. **Real-world impact:** Queries that failed with traditional chunking now succeed

## Running the Demo

### ChunkKingDemo - Contextual Chunking Focus
```bash
cd chunkking
mvn clean compile
mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.ChunkKingDemo" -Dexec.args="sk-proj-YOUR-KEY"
```

### AllChunkingStrategiesComparison - Comprehensive Benchmark
Compare **ALL 9 chunking strategies** from agenticmemory library:
```bash
mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.AllChunkingStrategiesComparison" -Dexec.args="sk-proj-YOUR-KEY"
```

**Strategies compared:**
1. SlidingWindowChunking (baseline)
2. ContextualChunking (LLM-enhanced)
3. AdaptiveChunking (boundary-aware)
4. EntityBasedChunking (named entities)
5. TopicBasedChunking (semantic grouping)
6. RegexChunking (custom patterns)
7. HybridChunking (combined strategies)
8. ZettelkastenChunking (knowledge management)
9. TaskAwareChunking (task-optimized)

See [ALL_STRATEGIES_COMPARISON.md](ALL_STRATEGIES_COMPARISON.md) for detailed documentation.


## Dependencies

- **agenticmemory** (0.0.9): Provides LateChunking implementation and RAG framework
- Java 18+

## Key Concepts Demonstrated

1. **Context Problem**: How traditional chunking loses contextual information
2. **Anaphoric References**: Pronouns and references that need broader context
3. **Late Chunking**: Embed-first, chunk-later approach
4. **Token-Level Embeddings**: Full document context in every token
5. **Mean Pooling**: Aggregating contextualized tokens into chunk embeddings
6. **Measurable Impact**: Concrete similarity score improvements

## Use Cases

Late Chunking excels when:
- ✅ Documents have cross-references
- ✅ Anaphoric resolution is critical
- ✅ Long-range dependencies matter
- ✅ Retrieval quality is paramount

Traditional chunking may suffice when:
- ⚪ Documents are simple and self-contained
- ⚪ Chunks are naturally independent
- ⚪ Memory/compute constraints exist

## Demo Programs

This project includes three demonstration programs:

### 1. ChunkKingDemo.java
**Focus:** Contextual Chunking deep dive

Demonstrates:
- The context problem with anaphoric references
- How Contextual Chunking works (adding document context to chunks)
- Real embedding comparison (with/without context)
- Numerical results showing 2-18% improvements

**Run:** `mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.ChunkKingDemo" -Dexec.args="YOUR-API-KEY"`

### 2. ChunkingComparisonDemo.java
**Focus:** Side-by-side comparison of two strategies

Compares:
- SlidingWindowChunking (traditional)
- ContextualChunking (context-aware)

Uses real RAGService indexing and search with test queries.

**Run:** `mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.ChunkingComparisonDemo" -Dexec.args="YOUR-API-KEY"`

### 3. AllChunkingStrategiesComparison.java ⭐ NEW!
**Focus:** Comprehensive benchmark of ALL strategies

Tests 9 different chunking strategies:
1. SlidingWindowChunking
2. ContextualChunking
3. AdaptiveChunking
4. EntityBasedChunking
5. TopicBasedChunking
6. RegexChunking
7. HybridChunking
8. ZettelkastenChunking
9. TaskAwareChunking

Provides:
- Performance metrics (chunk count, size, speed)
- Query performance comparison (5 test queries)
- Best strategy recommendations
- Comprehensive comparison summary

**Run:** `mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.AllChunkingStrategiesComparison" -Dexec.args="YOUR-API-KEY"`

**Docs:** See [ALL_STRATEGIES_COMPARISON.md](ALL_STRATEGIES_COMPARISON.md) for detailed documentation.

## Which Demo Should I Run?

- **Learning about context problems?** → Start with `ChunkKingDemo.java`
- **Quick comparison of basic vs. context-aware?** → Run `ChunkingComparisonDemo.java`
- **Need to choose a strategy for production?** → Run `AllChunkingStrategiesComparison.java`


