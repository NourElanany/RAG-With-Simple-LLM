package org.example;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.grpc.DataType;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MilvusInserter - A refactored class for inserting documents into Milvus vector database
 * 
 * This class provides a clean, modular approach to:
 * - Managing Milvus connections
 * - Creating and managing indexes
 * - Batch inserting documents with embeddings
 * - Error handling and resource management
 */
public class MilvusEmbedAndInsert implements AutoCloseable {
    
    // Configuration constants
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 19530;
    private static final String DEFAULT_COLLECTION = "documents";
    private static final int DEFAULT_BATCH_SIZE = 100;
    
    // Core components
    private final MilvusServiceClient milvusClient;
    private final Embedder embedder;
    private final String collectionName;
    private final ExecutorService executorService;
    
    // Configuration
    private final int batchSize;
    
    /**
     * Default constructor with standard configuration
     */
    public MilvusEmbedAndInsert() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_COLLECTION, DEFAULT_BATCH_SIZE);
    }
    
    /**
     * Constructor with custom configuration
     */
    public MilvusEmbedAndInsert(String host, int port, String collectionName, int batchSize) {
        this.collectionName = collectionName;
        this.batchSize = batchSize;
        this.executorService = Executors.newFixedThreadPool(2);
        
        // Initialize Milvus client
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        
        this.milvusClient = new MilvusServiceClient(connectParam);
        this.embedder = new Embedder();
        
        System.out.println("Connected to Milvus at " + host + ":" + port);
    }
    
    /**
     * Initialize the collection for document insertion
     * Creates index and loads collection into memory
     */
    public boolean initializeCollection() {
        try {
            System.out.println("Initializing collection: " + collectionName);

            boolean indexCreated = createIndexIfNotExists();
            boolean collectionLoaded = loadCollection();

            if (indexCreated && collectionLoaded) {
                System.out.println("Collection initialized successfully!");
                return true;
            } else {
                System.err.println("Collection initialization had issues");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize collection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create index for the embedding field if it doesn't exist
     */
    private boolean createIndexIfNotExists() {
        try {
            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("embedding")
                    .withIndexType(io.milvus.param.IndexType.IVF_FLAT)
                    .withMetricType(io.milvus.param.MetricType.COSINE)
                    .withExtraParam("{\"nlist\":128}")
                    .build();

            milvusClient.createIndex(indexParam);
            System.out.println("✅ Index created successfully!");
            return true;
        } catch (Exception e) {
            System.out.println("ℹ️ Index may already exist: " + e.getMessage());
            return true; // Assume it exists
        }
    }

    /**
     * Load collection into memory for search operations
     */
    private boolean loadCollection() {
        try {
            LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();

            milvusClient.loadCollection(loadParam);
            System.out.println("✅ Collection loaded into memory!");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error loading collection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insert documents in batches for better performance
     */
    public InsertionResult insertDocuments(List<String> documents) {
        if (documents == null || documents.isEmpty()) {
            return new InsertionResult(false, "No documents provided", 0);
        }

        System.out.println("Processing " + documents.size() + " documents...");

        try {
            int totalInserted = 0;
            int batchCount = (int) Math.ceil((double) documents.size() / batchSize);

            for (int i = 0; i < batchCount; i++) {
                int startIdx = i * batchSize;
                int endIdx = Math.min(startIdx + batchSize, documents.size());
                List<String> batch = documents.subList(startIdx, endIdx);

                System.out.println("Processing batch " + (i + 1) + "/" + batchCount + " (" + batch.size() + " documents)");

                if (insertBatch(batch)) {
                    totalInserted += batch.size();
                } else {
                    return new InsertionResult(false, "Failed to insert batch " + (i + 1), totalInserted);
                }
            }

            System.out.println("Successfully inserted " + totalInserted + " documents!");
            return new InsertionResult(true, "All documents inserted successfully", totalInserted);

        } catch (Exception e) {
            System.err.println("Error during document insertion: " + e.getMessage());
            e.printStackTrace();
            return new InsertionResult(false, e.getMessage(), 0);
        }
    }

    /**
     * Insert a single batch of documents
     */
    private boolean insertBatch(List<String> documents) {
        try {
            // Generate embeddings
            List<List<Float>> embeddings = embedder.embedTexts(documents);

            if (embeddings.size() != documents.size()) {
                System.err.println("Mismatch between documents and embeddings count");
                return false;
            }

            // Insert into Milvus
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(Arrays.asList(
                            new InsertParam.Field("text", documents),
                            new InsertParam.Field("embedding", embeddings)
                    ))
                    .build();

            milvusClient.insert(insertParam);
            System.out.println("✅ Batch inserted: " + documents.size() + " documents");
            return true;

        } catch (Exception e) {
            System.err.println("Error inserting batch: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get sample documents for testing
     */
    public static List<String> getSampleDocuments() {

        return Arrays.asList(
                "LangChain4j is a Java framework for building applications with Large Language Models.",
                "Milvus is an open-source vector database built for scalable similarity search and AI applications.",
                "Ollama allows you to run large language models locally on your machine with ease.",
                "Retrieval-Augmented Generation (RAG) combines information retrieval with text generation for better AI responses.",
                "Vector embeddings represent text as numerical vectors in high-dimensional space for semantic search.",
                "Docker containers provide a lightweight, portable way to package and deploy applications.",
                "Java JDK 17 is a long-term support version with many performance improvements and new features.",
                "Maven is a build automation tool used primarily for Java projects and dependency management.",
                "Cosine similarity is a metric used to measure similarity between two non-zero vectors.",
                "Natural Language Processing (NLP) enables computers to understand and process human language."
        );
    }

    /**
     * Close all resources
     */
    @Override
    public void close() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            if (milvusClient != null) {
                milvusClient.close();
                System.out.println("Milvus connection closed");
            }
        } catch (Exception e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    /**
     * Result class for insertion operations
     */
    public static class InsertionResult {
        private final boolean success;
        private final String message;
        private final int insertedCount;

        public InsertionResult(boolean success, String message, int insertedCount) {
            this.success = success;
            this.message = message;
            this.insertedCount = insertedCount;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getInsertedCount() { return insertedCount; }

        @Override
        public String toString() {
            return String.format("InsertionResult{success=%s, message='%s', insertedCount=%d}",
                    success, message, insertedCount);
        }
    }

    /**
     * Main method for testing the inserter
     */
    public static void main(String[] args) {
        System.out.println("Starting Milvus Document Inserter...");


        try (MilvusEmbedAndInsert inserter = new MilvusEmbedAndInsert()) {

            // Initialize collection
            if (!inserter.initializeCollection()) {
                System.err.println("Failed to initialize collection. Exiting.");
                return;
            }

            // Get sample documents
            List<String> documents = getSampleDocuments();

            // load document
            String filePath = "data/sample.txt";
            String text = DocumentLoader.loadTextFile(filePath);

            documents = DocumentLoader.splitText(text, 500, 50);

            System.out.println("Loaded text chunks:");

//            List<String> documents = createQualityDocuments();

            System.out.println("Prepared " + documents.size() + " sample documents");

            // Insert documents
            InsertionResult result = inserter.insertDocuments(documents);
            System.out.println("Result: " + result);

            if (result.isSuccess()) {
                System.out.println("Document insertion completed successfully!");
            } else {
                System.err.println("Document insertion failed: " + result.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error in main: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create high-quality, well-structured documents for testing
     */
    private static List<String> createQualityDocuments() {
        return Arrays.asList(
                // Git
                "Git هو نظام إدارة الإصدارات الموزع الأكثر استخداماً في العالم. يمكن للمطورين استخدام Git لتتبع التغييرات في الكود المصدري والتعاون مع فرق التطوير المختلفة. Git يحفظ تاريخ كامل للتغييرات ويسمح بإنشاء فروع متعددة للعمل على ميزات مختلفة بشكل منفصل.",

                // Docker
                "Docker هو منصة للحاويات (Containerization) تسمح بتغليف التطبيقات مع جميع متطلباتها وتبعياتها في حاوية واحدة منعزلة. Docker يضمن أن التطبيق سيعمل بنفس الطريقة على أي نظام تشغيل يدعم Docker، مما يحل مشكلة 'يعمل على جهازي'. الحاويات أخف وزناً من الآلات الافتراضية وتبدأ بسرعة أكبر.",

                // Maven
                "Maven هو أداة لإدارة وبناء المشاريع في لغة Java وتطويرها. Maven يستخدم ملف pom.xml لتحديد تبعيات المشروع وإعدادات البناء. يحمل Maven المكتبات اللازمة تلقائياً من المستودعات المركزية ويدير دورة حياة بناء المشروع من التطوير إلى التوزيع. Maven يوفر هيكل موحد لمشاريع Java ويسهل مشاركة المكتبات بين المطورين.",

                // الفروقات
                "الفرق الأساسي بين Git و Docker و Maven هو أن كل أداة تخدم غرضاً مختلفاً في دورة التطوير. Git يُستخدم لإدارة الإصدارات والتحكم في الكود المصدري، بينما Docker يُستخدم لتعبئة ونشر التطبيقات، أما Maven فيُستخدم لبناء وإدارة تبعيات مشاريع Java. هذه الأدوات الثلاث تكمل بعضها البعض في بيئة التطوير الحديثة.",

                // LangChain4j
                "LangChain4j هو إطار عمل Java مفتوح المصدر لبناء تطبيقات الذكاء الاصطناعي. يوفر LangChain4j تكاملاً سهلاً مع نماذج اللغة الكبيرة مثل OpenAI وOllama وClaude وGoogle PaLM. يمكن للمطورين استخدام LangChain4j لبناء chatbots ومساعدين ذكيين وتطبيقات معالجة اللغة الطبيعية بسهولة.",

                // Milvus
                "Milvus هو قاعدة بيانات شعاعية مفتوحة المصدر مصممة خصيصاً لتطبيقات الذكاء الاصطناعي. يمكن لـ Milvus تخزين وفهرسة والبحث في مليارات التمثيلات الشعاعية بسرعة عالية. يُستخدم Milvus في تطبيقات البحث الدلالي وأنظمة التوصية وأنظمة RAG للذكاء الاصطناعي.",

                // RAG Systems
                "نظام RAG (Retrieval-Augmented Generation) يجمع بين تقنيتين مهمتين: استرجاع المعلومات من قاعدة بيانات والتوليد باستخدام نماذج اللغة. في نظام RAG، يتم أولاً البحث عن المعلومات ذات الصلة في قاعدة البيانات، ثم تُستخدم هذه المعلومات كسياق لنموذج اللغة لتوليد إجابة دقيقة ومفيدة. هذا يقلل من ظاهرة الهلوسة في النماذج ويحسن جودة الإجابات.",

                // Mobile Development
                "تطوير تطبيقات الهاتف المحمول يتطلب فهماً للتقنيات المختلفة مثل تطوير الواجهات الأمامية (Frontend) والخوادم الخلفية (Backend). تقنيات مثل React Native وFlutter تمكن المطورين من كتابة تطبيق واحد يعمل على أنظمة iOS وAndroid معاً، مما يوفر الوقت والجهد في التطوير.",

                // Vector Databases
                "قواعد البيانات الشعاعية مثل Milvus وChroma وPinecone وWeaviate تمكن من تخزين النصوص والبيانات كتمثيلات شعاعية عالية الأبعاد. هذه التقنية ضرورية لبناء تطبيقات البحث الدلالي حيث يمكن العثور على المعلومات بناءً على المعنى وليس فقط الكلمات المفتاحية.",

                // Programming Concepts
                "البرمجة الحديثة تعتمد على مفاهيم متقدمة مثل البرمجة كائنية التوجه والبرمجة الوظيفية والبرمجة غير المتزامنة. هذه المفاهيم تساعد في كتابة كود أكثر كفاءة وقابلية للصيانة. كما أن فهم خوارزميات البيانات وهياكل البيانات أمر ضروري لأي مطور محترف."
        );
    }
}