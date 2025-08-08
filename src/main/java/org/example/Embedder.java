package org.example;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Embedder {
    private final EmbeddingModel embeddingModel;

    public Embedder() {
        this.embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();
    }

    public Embedder(String modelName) {
        this.embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(modelName)
                .build();
    }

    public List<Float> embedText(String text) {
        try {
            var response = embeddingModel.embed(text);
            Embedding embedding = response.content();
            float[] vectorArray = embedding.vector();

            // تحويل float[] إلى List<Float>
            List<Float> result = new java.util.ArrayList<>();
            for (float value : vectorArray) {
                result.add(value);
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error embedding text: " + e.getMessage());
            throw new RuntimeException("Failed to embed text", e);
        }
    }

    public List<List<Float>> embedTexts(List<String> texts) {
        try {
            // تحويل String إلى TextSegment
            List<TextSegment> textSegments = texts.stream()
                    .map(TextSegment::from)
                    .collect(Collectors.toList());

            var response = embeddingModel.embedAll(textSegments);
            return response.content().stream()
                    .map(embedding -> {
                        float[] vectorArray = embedding.vector();
                        List<Float> result = new ArrayList<>();
                        for (float value : vectorArray) {
                            result.add(value);
                        }
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error embedding texts: " + e.getMessage());
            throw new RuntimeException("Failed to embed texts", e);
        }
    }

    // دالة مساعدة للحصول على float[] مباشرة (أسرع للاستخدام مع Milvus)
    public float[] embedTextAsArray(String text) {
        try {
            var response = embeddingModel.embed(text);
            Embedding embedding = response.content();
            return embedding.vector();
        } catch (Exception e) {
            System.err.println("Error embedding text: " + e.getMessage());
            throw new RuntimeException("Failed to embed text", e);
        }
    }

    public List<float[]> embedTextsAsArrays(List<String> texts) {
        try {
            // تحويل String إلى TextSegment
            List<TextSegment> textSegments = texts.stream()
                    .map(TextSegment::from)
                    .collect(Collectors.toList());

            var response = embeddingModel.embedAll(textSegments);
            return response.content().stream()
                    .map(Embedding::vector)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error embedding texts: " + e.getMessage());
            throw new RuntimeException("Failed to embed texts", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing Embedder...");

        try {
            String filePath = "data/sample.txt";
            String text = DocumentLoader.loadTextFile(filePath);

            List<String> chunks = DocumentLoader.splitText(text, 500, 50);

            System.out.println("Loaded text chunks:");
        System.out.println(chunks);
            for (String chunk : chunks) {
                System.out.println("🔹 " + chunk);
                System.out.println("----------");
            }
            Embedder embedder = new Embedder();

            // اختبار النصوص المتعددة
            List<String> testTexts = List.of(
                    "First test document",
                    "Second test document",
                    "Third test document"
            );

            List<List<Float>> multipleVectors = embedder.embedTexts(chunks);
            System.out.println("✅ Multiple embeddings successful!");
            System.out.println("Number of embeddings: " + multipleVectors.size());
            System.out.println("Each embedding size: " + multipleVectors.get(0).size());

            // اختبار List<Float>
            List<Float> vectorList = embedder.embedText("This is a test document for embedding.");
            System.out.println("✅ Embedding (List<Float>) successful!");
            System.out.println("Embedding size: " + vectorList.size());
            System.out.println("First 5 values: " + vectorList.subList(0, Math.min(5, vectorList.size())));

            // اختبار float[] (أسرع)
            float[] vectorArray = embedder.embedTextAsArray("This is another test document.");
            System.out.println("✅ Embedding (float[]) successful!");
            System.out.println("Array length: " + vectorArray.length);
            System.out.print("First 5 values: [");
            for (int i = 0; i < Math.min(5, vectorArray.length); i++) {
                System.out.print(vectorArray[i]);
                if (i < Math.min(5, vectorArray.length) - 1) System.out.print(", ");
            }


        } catch (Exception e) {
            System.err.println("❌ Embedding failed: " + e.getMessage());
            System.err.println("Make sure Ollama is running and nomic-embed-text model is installed.");
        }
    }
}