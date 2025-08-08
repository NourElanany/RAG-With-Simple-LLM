@echo off
echo 🚀 RAG System with LangChain4j, Milvus, and Ollama
echo ================================================

echo.
echo 📋 Choose an option:
echo 1. Quick Test (Run all components)
echo 2. Create Milvus Collection
echo 3. Test Embedder
echo 4. Insert Sample Documents
echo 5. Test Search
echo 6. Run RAG System (Interactive)
echo 7. Run RAG System (Single Query)
echo 8. Exit

set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" (
    echo 🧪 Running Quick Test...
    mvn exec:java -Dexec.mainClass="org.example.QuickTest"
) else if "%choice%"=="2" (
    echo 📝 Creating Milvus Collection...
    mvn exec:java -Dexec.mainClass="org.example.MilvusCreateCollection"
) else if "%choice%"=="3" (
    echo 🔧 Testing Embedder...
    mvn exec:java -Dexec.mainClass="org.example.Embedder"
) else if "%choice%"=="4" (
    echo 📚 Inserting Sample Documents...
    mvn exec:java -Dexec.mainClass="org.example.MilvusEmbedAndInsert"
) else if "%choice%"=="5" (
    echo 🔍 Testing Search...
    mvn exec:java -Dexec.mainClass="org.example.MilvusSearch"
) else if "%choice%"=="6" (
    echo 🤖 Starting RAG System (Interactive Mode)...
    mvn exec:java -Dexec.mainClass="org.example.RAGSystem"
) else if "%choice%"=="7" (
    set /p query="Enter your question: "
    echo 🤖 Running RAG System with query: !query!
    mvn exec:java -Dexec.mainClass="org.example.RAGSystem" -Dexec.args="!query!"
) else if "%choice%"=="8" (
    echo 👋 Goodbye!
    exit /b 0
) else (
    echo ❌ Invalid choice. Please try again.
    pause
    goto :eof
)

echo.
echo ✅ Operation completed!
pause
