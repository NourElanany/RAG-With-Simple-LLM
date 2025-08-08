package org.example;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("🚀 Starting RAG System Quick Test...");
        
        try {
            // Step 1: Create Collection
            System.out.println("\n📝 Step 1: Creating Milvus Collection...");
            MilvusCreateCollection.main(new String[]{});
            
            // Step 2: Test Embedder
            System.out.println("\n🔧 Step 2: Testing Embedder...");
            Embedder.main(new String[]{});
            
            // Step 3: Insert Sample Documents
            System.out.println("\n📚 Step 3: Inserting Sample Documents...");
            MilvusEmbedAndInsert.main(new String[]{});
            
            // Step 4: Test Search
            System.out.println("\n🔍 Step 4: Testing Search...");
            MilvusSearch.main(new String[]{});
            
            // Step 5: Test RAG System
            System.out.println("\n🤖 Step 5: Testing RAG System...");
            RAGSystem.main(new String[]{"What is LangChain4j and how does it work?"});
            
            System.out.println("\n✅ All tests completed successfully!");
            System.out.println("🎉 Your RAG system is ready to use!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
