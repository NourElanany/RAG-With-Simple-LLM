package org.example;

import java.util.List;
import java.util.Scanner;

/**
 * Complete RAG System - Combines Retrieval and Generation
 *
 * This class orchestrates the full RAG pipeline:
 * 1. Takes user query
 * 2. Retrieves relevant documents from vector database
 * 3. Generates response using LLM with retrieved context
<<<<<<< HEAD
 *Final
=======
>>>>>>> c9cba5e (Merged main into MohamedRamadan)
 */
public class RAGSystem implements AutoCloseable {

    private final Retriever retriever;
    private final Generator generator;

    public RAGSystem() {
        this.retriever = new Retriever();
        this.generator = new Generator();

        System.out.println("🚀 RAG System initialized successfully!");
    }

    public RAGSystem(String ollamaUrl, String model) {
        this.retriever = new Retriever();
        this.generator = new Generator(ollamaUrl, model);

        System.out.println("🚀 RAG System initialized with custom settings!");
    }

    /**
     * Main RAG method - the complete pipeline
     * @param query User question
     * @return Generated response with context
     */
    public RAGResponse ask(String query) {
        System.out.println("🔍 Searching for relevant information...");

        // Step 1: Retrieve relevant documents
        List<String> retrievedDocs = retriever.search(query);

        System.out.println("📋 Found " + retrievedDocs.size() + " relevant documents");

        // Step 2: Generate response with context
        System.out.println("🧠 Generating response...");
        String response = generator.generateWithContext(query, retrievedDocs);

        return new RAGResponse(query, retrievedDocs, response);
    }

    /**
     * Interactive chat mode
     */
    public void startChatMode() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("💬 RAG Chat Mode Started!");
        System.out.println("Type 'exit' to quit, 'clear' to see available commands\n");

        while (true) {
            System.out.print("🧠 Your question: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("👋 Goodbye!");
                break;
            }

            if (input.equalsIgnoreCase("clear")) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("Available commands:");
                System.out.println("- 'exit': Quit the program");
                System.out.println("- 'clear': Show this help");
                System.out.println("- Any question: Get RAG response");
                System.out.println("=".repeat(50) + "\n");
                continue;
            }

            if (input.isEmpty()) {
                continue;
            }

            try {
                RAGResponse response = ask(input);
                response.printFormatted();
                System.out.println();

            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    @Override
    public void close() {
        System.out.println("🔒 Closing RAG System...");
        // Close any resources if needed
    }

    /**
     * Response class to hold RAG results
     */
    public static class RAGResponse {
        private final String query;
        private final List<String> retrievedDocs;
        private final String response;
        private final long timestamp;

        public RAGResponse(String query, List<String> retrievedDocs, String response) {
            this.query = query;
            this.retrievedDocs = retrievedDocs;
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        public void printFormatted() {
            System.out.println("━".repeat(60));
            System.out.println("📝 Question: " + query);
            System.out.println("━".repeat(60));

            System.out.println("🔎 Retrieved Context (" + retrievedDocs.size() + " documents):");
            for (int i = 0; i < retrievedDocs.size(); i++) {
                String doc = retrievedDocs.get(i);
                String preview = doc.length() > 100 ? doc.substring(0, 100) + "..." : doc;
                System.out.println("   " + (i + 1) + ". " + preview);
            }

            System.out.println("━".repeat(60));
            System.out.println("🤖 Response:");
            System.out.println(response);
            System.out.println("━".repeat(60));
        }

        // Getters
        public String getQuery() { return query; }
        public List<String> getRetrievedDocs() { return retrievedDocs; }
        public String getResponse() { return response; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Main method - can run in different modes
     */
    public static void main(String[] args) {

        try (RAGSystem rag = new RAGSystem()) {

            if (args.length > 0) {
                // Command line mode - single question
                String question = String.join(" ", args);
                System.out.println("🚀 RAG System - Single Query Mode\n");

                RAGResponse response = rag.ask(question);
                response.printFormatted();

            } else {
                // Interactive chat mode
                rag.startChatMode();
            }

        }
    }
}