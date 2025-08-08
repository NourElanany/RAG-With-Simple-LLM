# RAG System with LangChain4j, Milvus, and Ollama

A comprehensive Retrieval-Augmented Generation (RAG) system built with Java JDK17, LangChain4j, Milvus, and local Ollama LLM. This project demonstrates how to build a production-ready RAG pipeline for question-answering and information retrieval applications.

## 🏗️ System Architecture

The system follows a streamlined pipeline for processing user queries and generating contextual responses:

```
User Query → Embedder (Ollama) → Milvus Vector Search → Context Retrieval → LLM (Llama3.2) → Response Generation
```

### Key Components:
- **Ollama**: Local LLM for generating embeddings and text generation
- **Milvus**: High-performance vector database for efficient similarity search
- **LangChain4j**: Java implementation of LangChain for building LLM applications
- **Llama3.2**: Open-weight LLM for response generation

## 📁 Project Structure

```
src/main/java/org/example/
├── MilvusCreateCollection.java  # إنشاء مجموعة Milvus
├── Embedder.java               # تحويل النص إلى vectors
├── MilvusEmbedAndInsert.java   # إدراج البيانات في Milvus
├── MilvusSearch.java           # البحث في Milvus
├── RAGSystem.java              # النظام الكامل للـ RAG
└── QuickTest.java              # اختبار سريع لكل المكونات

docker-compose.yml              # خدمات Milvus
run-rag.bat                    # سكريبت تشغيل سهل
setup.md                       # دليل الإعداد التفصيلي
```

## 🚀 Quick Start

### Prerequisites
- Java JDK 17 or higher
- Docker and Docker Compose
- Ollama installed locally
- Maven 3.6+

### 1. Start Milvus Services
```bash
docker-compose up -d
```

### 2. Verify Local Ollama Installation
```bash
ollama list
# Ensure you have the following models:
# - llama3.2
# - nomic-embed-text
```

### 3. Run Quick Test
```bash
# On Windows
run-rag.bat

# Or using Maven directly
mvn exec:java -Dexec.mainClass="org.example.QuickTest"
```

### 4. Start Interactive RAG System
```bash
mvn exec:java -Dexec.mainClass="org.example.RAGSystem"
```

## 🔧 Core Components

### 1. MilvusCreateCollection
Creates and configures a Milvus collection with the following schema:
- `id`: Unique identifier (Int64, auto-generated)
- `embedding`: Embedding vector (FloatVector, 384 dimensions)
- `text`: Original text content (VarChar, max 512 characters)

### 2. Embedder
Handles text-to-vector conversion using Ollama with the `nomic-embed-text` model. This component is responsible for generating dense vector representations of text documents and queries.

### 3. MilvusEmbedAndInsert
Manages the process of ingesting documents into the system by:
- Processing raw text documents
- Generating embeddings
- Storing them in the Milvus vector database

### 4. MilvusSearch
Implements efficient vector similarity search with the following features:
- Cosine similarity-based retrieval
- Configurable top-k results
- Filtering capabilities

### 5. RAGSystem
The main orchestrator that combines all components to provide a complete RAG pipeline:
1. Processes user queries
2. Retrieves relevant context using vector similarity search
3. Generates accurate and context-aware responses using Llama3.2
4. Handles conversation history and context management

## 💡 Usage Examples

### 1. Direct Vector Search
```java
// Initialize the search component
MilvusSearch searcher = new MilvusSearch();

// Search for similar documents
List<SearchResult> results = searcher.searchSimilarDocuments("What is LangChain4j?", 3);

// Process and display results
results.forEach(result -> System.out.println("Similarity: " + result.getScore() 
    + "\nText: " + result.getText() + "\n"));
```

### 2. Complete RAG Pipeline
```java
// Initialize the RAG system
RAGSystem ragSystem = new RAGSystem();

// Generate a response with context
String response = ragSystem.generateRAGResponse(
    "Explain vector databases",  // User query
    3,                           // Number of context documents
    0.7,                         // Similarity threshold
    1024                         // Max tokens in response
);

System.out.println("Generated Response: " + response);
```

### 3. Interactive Mode
```bash
# Start the interactive RAG console
mvn exec:java -Dexec.mainClass="org.example.RAGSystem"

# Example interaction:
# > What is the capital of France?
# < The capital of France is Paris.
# > How does vector search work?
# < Vector search works by...
```

## 🛠️ Configuration

### Environment Variables
Create a `.env` file in the project root with the following variables:
```
MILVUS_HOST=127.0.0.1
MILVUS_PORT=19530
OLLAMA_BASE_URL=http://localhost:11434
EMBEDDING_MODEL=nomic-embed-text
LLM_MODEL=llama3.2
```

### Milvus Connection
- Host: `127.0.0.1`
- Port: `19530`
- Collection Name: `rag_documents`
- Vector Dimension: `384`

### Performance Tuning
- Batch size for embeddings: `8`
- Max tokens in response: `1024`
- Similarity threshold: `0.7`
- Top-k results: `3`
- Port: `19530`

### Ollama Connection
- Base URL: `http://localhost:11434`
- Chat Model: `llama3.2`
- Embedding Model: `nomic-embed-text`

## 📊 Sample Data

النظام يأتي مع مستندات عينة حول:
- LangChain4j framework
- Milvus vector database
- Ollama local models
- RAG concepts
- Vector embeddings

## 🔍 Testing

```bash
# اختبار كامل
mvn exec:java -Dexec.mainClass="org.example.QuickTest"

# اختبار مكون واحد
mvn exec:java -Dexec.mainClass="org.example.Embedder"
mvn exec:java -Dexec.mainClass="org.example.MilvusSearch"
```

## 🚨 Troubleshooting

### مشاكل شائعة:
1. **Connection refused**: تأكد من تشغيل Docker services
2. **Model not found**: تأكد من تثبيت نماذج Ollama
3. **Memory issues**: زيادة ذاكرة Docker

### التحقق من الخدمات:
```bash
# Docker services
docker ps

# Ollama models
ollama list

# Milvus connection
mvn exec:java -Dexec.mainClass="org.example.MilvusPingTest"
```

## 🎯 Next Steps

- إضافة تحميل المستندات من ملفات
- تقسيم المستندات الكبيرة
- واجهة ويب
- ذاكرة المحادثة
- دعم أنواع ملفات متعددة

## 📝 License

هذا المشروع مفتوح المصدر ويمكن استخدامه بحرية.
