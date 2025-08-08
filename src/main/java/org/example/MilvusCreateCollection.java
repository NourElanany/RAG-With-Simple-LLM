package org.example;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.grpc.DataType;

public class MilvusCreateCollection {

    public static void main(String[] args) {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost("127.0.0.1")
                .withPort(19530)
                .build();

        MilvusServiceClient milvusClient = new MilvusServiceClient(connectParam);

        try {
            String collectionName = "documents";
            int dimension = 768; // Updated to match nomic-embed-text model dimensions

            // ✅ 1. Drop the collection if it already exists
            try {
                milvusClient.dropCollection(
                        DropCollectionParam.newBuilder()
                                .withCollectionName(collectionName)
                                .build()
                );
                System.out.println("⚠️ Collection dropped (if existed).");
            } catch (Exception ex) {
                System.out.println("ℹ️ Collection did not exist. Continuing...");
            }

            // ✅ 2. Define fields
            FieldType textField = FieldType.newBuilder()
                    .withName("text")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(1024)
                    .build();

            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build();

            //Define embeddingField
            FieldType embeddingField = FieldType.newBuilder()
                    .withName("embedding")
                    .withDataType(DataType.FloatVector)
                    .withDimension(dimension)
                    .build();

            // ✅ 3. Create collection
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription("A collection to store document embeddings")
                    .withShardsNum(2)
                    .addFieldType(idField)
                    .addFieldType(embeddingField)
                    .addFieldType(textField) // <-- don't forget to include text
                    .build();

            milvusClient.createCollection(createCollectionParam);
            System.out.println("✅ Collection created successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to create collection: " + e.getMessage());
        } finally {
            milvusClient.close();
        }
    }
}
