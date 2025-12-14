# ChunkKing - Context-Sensitive Chunking Demo

A demonstration project showing the difference between traditional chunking and context-sensitive Late Chunking using the Agentic Memory RAG library.

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

```bash
cd chunkking
mvn clean compile
mvn exec:java -Dexec.mainClass="io.github.vishalmysore.chunkking.ChunkKingDemo"
```

## Project Structure

```
chunkking/
├── pom.xml                           # Maven configuration
├── README.md                         # This file
└── src/main/java/
    └── io/github/vishalmysore/chunkking/
        └── ChunkKingDemo.java        # Main demonstration
```

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

## Learn More

See the main Agentic Memory library documentation:
- [Late Chunking Guide](../lucenerag/LATE_CHUNKING.md)
- [Chunking Strategies](../lucenerag/CHUNKING_STRATEGIES.md)

## License

Same as the Agentic Memory library.
