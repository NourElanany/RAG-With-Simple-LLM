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