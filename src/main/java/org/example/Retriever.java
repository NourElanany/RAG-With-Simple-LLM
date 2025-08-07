package org.example;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;

import java.util.*;

/**
 * Retriever class that connects to a Milvus vector database and retrieves top-k
 * semantically similar text entries based on a user query.
 */
public class Retriever {

    private final MilvusServiceClient milvusClient;
    private final Embedder embedder;

    // Constants
    private final String collectionName = "documents";
    private final String embeddingField = "embedding";
    private final String outputField = "text";
    private final int topK = 3; // Number of results to return

    /**
     * Constructor: Connects to Milvus and initializes the embedder.
     */
    public Retriever() {
        // 1. Connect to Milvus
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost("127.0.0.1")
                .withPort(19530)
                .build();

        this.milvusClient = new MilvusServiceClient(connectParam);
        this.embedder = new Embedder(); // Custom embedder (e.g., Ollama)
    }

    /**
     * Searches Milvus using a vector representation of the input query.
     *
     * @param query The user question or search query.
     * @return A list of top-k matched texts from the collection.
     */
    public List<String> search(String query) {
        try {
            // 2. Convert query to embedding (float[] → List<Float>)
            float[] queryEmbeddingArray = embedder.embedTextAsArray(query);

            List<Float> queryEmbedding = new ArrayList<>();
            for (float value : queryEmbeddingArray) {
                queryEmbedding.add(value);
            }

            // 3. Build search parameters
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(io.milvus.param.MetricType.COSINE)
                    .withTopK(topK)
                    .withOutFields(Collections.singletonList(outputField))
                    .withVectors(Collections.singletonList(queryEmbedding))
                    .withVectorFieldName(embeddingField)
                    .withParams("{\"nprobe\":10}")
                    .build();

            // 4. Perform the search
            var searchResponse = milvusClient.search(searchParam);

            // 5. Parse and extract results
            SearchResultsWrapper results = new SearchResultsWrapper(searchResponse.getData().getResults());

            List<String> matchedTexts = results.getFieldData(outputField, 0)
                    .stream()
                    .filter(obj -> obj instanceof String)
                    .map(obj -> (String) obj)
                    .toList();

            return matchedTexts;

        } catch (Exception e) {
            System.err.println("❌ Error during retrieval: " + e.getMessage());
            return List.of(); // Return empty list on failure
        }
    }

    /**
     * Main method to run an interactive question-answer loop.
     */
    public static void main(String[] args) {
        Retriever retriever = new Retriever();
        Scanner scanner = new Scanner(System.in);

        System.out.print("🧠 Ask a question: ");
        String question = scanner.nextLine();

        List<String> results = retriever.search(question);

        System.out.println("\n🔎 Top matched results:");
        if (results.isEmpty()) {
            System.out.println("❌ No results found.");
        } else {
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
        }
    }
}
