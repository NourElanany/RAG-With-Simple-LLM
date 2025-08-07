package org.example;

import java.util.List;
import java.util.Scanner;

/**
 * Complete RAG System - Combines Retrieval and Generation
 */
public class RAGSystem implements AutoCloseable {

    private final Retriever retriever;
    private final Generator generator;

    public RAGSystem() {
        this.retriever = new Retriever();
        this.generator = new Generator();
        log("🚀 RAG System initialized successfully!");
    }

    public RAGSystem(String ollamaUrl, String model) {
        this.retriever = new Retriever();
        this.generator = new Generator(ollamaUrl, model);
        log("🚀 RAG System initialized with custom settings!");
    }

    public RAGResponse ask(String query) {
        log("🔍 Searching for relevant information...");

        List<String> retrievedDocs;
        try {
            retrievedDocs = retriever.search(query);
        } catch (Exception e) {
            throw new RuntimeException("Error during document retrieval: " + e.getMessage());
        }

        log("📋 Found " + retrievedDocs.size() + " relevant documents");

        String response;
        try {
            log("🧠 Generating response...");
            response = generator.generateWithContext(query, retrievedDocs);
        } catch (Exception e) {
            throw new RuntimeException("Error during response generation: " + e.getMessage());
        }

        return new RAGResponse(query, retrievedDocs, response);
    }

    public void startChatMode() {
        Scanner scanner = new Scanner(System.in);
        log("💬 RAG Chat Mode Started!");
        System.out.println("Type 'exit' to quit, 'clear' to see available commands\n");

        while (true) {
            System.out.print("🧠 Your question: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                log("👋 Goodbye!");
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

            if (input.isEmpty()) continue;

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
        log("🔒 Closing RAG System...");
        // Close resources if needed
    }

    private void log(String message) {
        System.out.println("[RAG] " + message);
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

            System.out.println("🔎 Retrieved Context (" + retrievedDocs.size() + "):");
            for (int i = 0; i < retrievedDocs.size(); i++) {
                String doc = retrievedDocs.get(i);
                String preview = doc.replaceAll("\\s+", " ").trim();
                preview = preview.length() > 120 ? preview.substring(0, 120) + "..." : preview;
                System.out.println("   " + (i + 1) + ". " + preview);
            }

            System.out.println("━".repeat(60));
            System.out.println("🤖 Response:\n" + response);
            System.out.println("━".repeat(60));
        }

        public String getQuery() { return query; }
        public List<String> getRetrievedDocs() { return retrievedDocs; }
        public String getResponse() { return response; }
        public long getTimestamp() { return timestamp; }
    }

    public static void main(String[] args) {
        try (RAGSystem rag = new RAGSystem()) {

            if (args.length > 0) {
                String question = String.join(" ", args);
                System.out.println("🚀 RAG System - Single Query Mode\n");

                RAGResponse response = rag.ask(question);
                response.printFormatted();

            } else {
                rag.startChatMode();
            }

        } catch (Exception e) {
            System.err.println("❌ Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}