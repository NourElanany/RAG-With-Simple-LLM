# RAG System with LangChain4j, Milvus, and Ollama

نظام RAG (Retrieval-Augmented Generation) متكامل باستخدام Java JDK17، LangChain4j، Milvus، و Ollama المحلي.

## 🏗️ Architecture

```
User Query → Embedder (Ollama) → Milvus Search → Context Retrieval → LLM (Llama3.2) → Response
```

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

### 1. تشغيل خدمات Milvus
```bash
docker-compose up -d
```

### 2. التأكد من Ollama المحلي
```bash
ollama list
# تأكد من وجود llama3.2 و nomic-embed-text
```

### 3. تشغيل الاختبار السريع
```bash
# على Windows
run-rag.bat

# أو باستخدام Maven مباشرة
mvn exec:java -Dexec.mainClass="org.example.QuickTest"
```

### 4. تشغيل النظام التفاعلي
```bash
mvn exec:java -Dexec.mainClass="org.example.RAGSystem"
```

## 🔧 Components

### MilvusCreateCollection
ينشئ مجموعة في Milvus بالحقول التالية:
- `id`: معرف فريد (Int64, auto-generated)
- `embedding`: vector التضمين (FloatVector, 384 dimensions)
- `text`: النص الأصلي (VarChar, max 512 chars)

### Embedder
يستخدم Ollama مع نموذج `nomic-embed-text` لتحويل النصوص إلى vectors.

### MilvusEmbedAndInsert
يدرج مستندات عينة في قاعدة البيانات مع embeddings الخاصة بها.

### MilvusSearch
يبحث عن المستندات المشابهة باستخدام cosine similarity.

### RAGSystem
النظام الكامل الذي:
1. يأخذ استعلام المستخدم
2. يبحث عن مستندات مشابهة
3. يستخدم السياق مع Llama3.2 لتوليد الإجابة

## 💡 Usage Examples

### البحث المباشر
```java
MilvusSearch searcher = new MilvusSearch();
List<SearchResult> results = searcher.searchSimilarDocuments("What is LangChain4j?", 3);
```

### نظام RAG الكامل
```java
RAGSystem ragSystem = new RAGSystem();
String response = ragSystem.generateRAGResponse("Explain vector databases", 3);
```

### الوضع التفاعلي
```bash
mvn exec:java -Dexec.mainClass="org.example.RAGSystem"
# ثم اكتب أسئلتك
```

## 🛠️ Configuration

### Milvus Connection
- Host: `127.0.0.1`
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
