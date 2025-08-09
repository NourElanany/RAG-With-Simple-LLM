package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Generator class for RAG - integrates with Ollama to generate responses
 * based on retrieved context and user query
 */
public class Generator {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String ollamaUrl;
    private final String model;

    public Generator() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.ollamaUrl = "http://localhost:11434/api/generate";
        this.model = "llama3.2"; // أو أي model عندك
    }

    public Generator(String ollamaUrl, String model) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.ollamaUrl = ollamaUrl;
        this.model = model;
    }

    /**
     * Generate response using RAG approach
     * @param query User question
     * @param context Retrieved documents from vector search
     * @return Generated response
     */
    public String generateWithContext(String query, List<String> context) {
        if (context.isEmpty()) {
            return generateDirectResponse(query + "\n(ملاحظة: لم يتم العثور على سياق مناسب في قاعدة البيانات)");
        }

        String contextText = String.join("\n\n", context);
        String prompt = buildRAGPrompt(query, contextText);

        return callOllama(prompt);
    }

    /**
     * Generate direct response without context
     */
    public String generateDirectResponse(String query) {
        return callOllama(query);
    }

    /**
     * Build RAG prompt template
     */
    private String buildRAGPrompt(String query, String context) {
        return String.format("""
            You are a helpful Arabic assistant that answers questions based on provided information.
            
            CONTEXT (Available Information):
            %s
            
            QUESTION: %s
            
            INSTRUCTIONS:
            - Answer ONLY based on the provided context above
            - If the answer is not found in the context, clearly state: "لا توجد معلومات كافية في السياق المتوفر للإجابة على هذا السؤال"
            - Be accurate and helpful
            - Respond in Arabic only
            - Use the exact information from the context, don't make up details
            
            ANSWER IN ARABIC:
            """, context, query);
    }

    /**
     * Call Ollama API to generate response
     */
    private String callOllama(String prompt) {
        try {
            // Build request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.7);

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse response
                ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response.body());
                return responseJson.get("response").asText();
            } else {
                return "خطأ في الاتصال مع نموذج اللغة : " + response.statusCode();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error calling Ollama : " + e.getMessage());
            return "عذراً، حدث خطأ أثناءإنتاج الإجابة : " + e.getMessage();
        }
    }

    /**
     * Test method for the generator
     */
    public static void main(String[] args) {
        Generator generator = new Generator();

        // Test direct generation
        String directResponse = generator.generateDirectResponse("ما هو الذكاء الاصطناعي؟");
        System.out.println("Direct Response : " + directResponse);

        // Test RAG generation
        List<String> context = List.of(
                "LangChain4j is a Java framework for building applications with Large Language Models.",
                "Vector embeddings represent text as numerical vectors in high-dimensional space for semantic search."
        );

        String ragResponse = generator.generateWithContext(" ما هو LangChain4j ؟ ", context);
        System.out.println("\nRAG Response: " + ragResponse);
    }
}