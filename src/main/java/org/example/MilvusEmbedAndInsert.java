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
    
