package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Improved DocumentLoader with better text chunking strategies
 */
public class DocumentLoader {

    // Arabic sentence endings
    private static final Pattern ARABIC_SENTENCE_END = Pattern.compile("[.!?؟।।]\\s+");

    /**
     * Load text file content
     */
    public static String loadTextFile(String filePath) {
        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                System.err.println("File not found: " + filePath);
                return "";
            }

            String content = Files.readString(path);
            System.out.println("✅ Loaded file: " + filePath + " (" + content.length() + " characters)");
            return content;

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return "";
        }
    }

    /**
     * Improved text splitting with better sentence awareness
     */
    public static List<String> splitText(String text, int maxChunkSize, int overlap) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Clean the text first
        text = cleanText(text);

        List<String> chunks = new ArrayList<>();

        // Try to split by paragraphs first
        List<String> paragraphs = splitByParagraphs(text);

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            // If paragraph alone is too big, split it further
            if (paragraph.length() > maxChunkSize) {
                // Save current chunk if not empty
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }

                // Split large paragraph by sentences
                chunks.addAll(splitParagraphBySentences(paragraph, maxChunkSize, overlap));
            }
            // If adding this paragraph exceeds limit
            else if (currentChunk.length() + paragraph.length() > maxChunkSize) {
                // Save current chunk
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                }

                // Start new chunk with overlap if needed
                currentChunk = new StringBuilder();
                if (overlap > 0 && chunks.size() > 0) {
                    String lastChunk = chunks.get(chunks.size() - 1);
                    if (lastChunk.length() > overlap) {
                        currentChunk.append(lastChunk.substring(lastChunk.length() - overlap));
                        currentChunk.append(" ");
                    }
                }
                currentChunk.append(paragraph);
            }
            // Add paragraph to current chunk
            else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        // Add final chunk if not empty
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        // Filter out very short chunks
        chunks.removeIf(chunk -> chunk.trim().length() < 10);

        System.out.println("✅ Split text into " + chunks.size() + " chunks");
        return chunks;
    }

    /**
     * Clean text from unwanted characters and normalize
     */
    private static String cleanText(String text) {
        if (text == null) return "";

        return text
                // Remove excessive whitespace
                .replaceAll("\\s{3,}", "\n\n")
                // Normalize line breaks
                .replaceAll("\\r\\n|\\r", "\n")
                // Remove excessive line breaks
                .replaceAll("\\n{4,}", "\n\n\n")
                // Clean up spaces
                .replaceAll(" {2,}", " ")
                .trim();
    }



    public static void main(String[] args) {
        System.out.println("🧪 Testing DocumentLoader improvements...\n");

        // Test with sample text
        String sampleText = """
                البرمجة الحديثة تتطلب فهم عميق للذكاء الاصطناعي. تطبيقات الذكاء الاصطناعي تشمل معالجة اللغة الطبيعية والرؤية الحاسوبية.
                
                LangChain4j هو إطار عمل Java لبناء تطبيقات تستخدم نماذج اللغة الكبيرة. يوفر أدوات متقدمة للتكامل مع مختلف مزودي خدمات الذكاء الاصطناعي.
                
                قواعد البيانات الشعاعية مثل Milvus تمكن من البحث الدلالي السريع. تستخدم هذه التقنية في تطبيقات RAG لاسترجاع المعلومات ذات الصلة.
                """;

        List<String> chunks = splitText(sampleText, 200, 20);

        System.out.println("Generated chunks:");
        for (int i = 0; i < chunks.size(); i++) {
            System.out.println((i + 1) + ". " + chunks.get(i));
            System.out.println("   Length: " + chunks.get(i).length() + " characters\n");
        }
    }
}