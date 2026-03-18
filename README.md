# AI Agent Evaluation Pipeline

A lightweight, modular, and production-ready Spring Boot application to ingest, evaluate, and improve AI agent conversations.

## Architecture
- **Controller Layer**: REST endpoints for ingestion, evaluation, and suggestions.
- **Service Layer**: Business logic for orchestration.
- **Evaluator Layer**: Pluggable evaluation strategies (LLM-as-Judge, Tool Call, Coherence, Heuristics).
- **In-Memory Repository**: Thread-safe storage using `ConcurrentHashMap`.
- **Meta-Evaluation**: Automatic calibration check between evaluator scores and user feedback.

## Tech Stack
- Java 17
- Spring Boot 3.2.x
- Maven
- Lombok
- Springdoc OpenAPI (Swagger UI)

## Key Features
1. **Modular Evaluators**: Add new evaluators by implementing the `Evaluator` interface.
2. **Self-Improvement**: Automatically detects latency issues, tool failures, and prompt quality.
3. **Meta-Evaluation**: Logs warnings when human feedback significantly differs from automated scores.
4. **Thread-Safe In-Memory Storage**: Works out-of-the-box without external DB dependencies.

## How to Run

### Using Maven
```bash
./mvnw spring-boot:run
```

### Using Docker
```bash
docker-compose up --build
```

Access Swagger UI at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## API Usage

### 1. Ingest Conversation
**POST** `/conversations`
```json
{
  "id": "conv-123",
  "metadata": { "user_id": "user-1" },
  "turns": [
    {
      "role": "user",
      "content": "What is the weather in Bangalore?",
      "timestamp": 1710758400000
    },
    {
      "role": "assistant",
      "content": "Checking weather for Bangalore...",
      "toolCalls": [
        {
          "name": "get_weather",
          "parameters": { "location": "Bangalore" },
          "executionSuccess": true
        }
      ],
      "latencyMs": 1500,
      "timestamp": 1710758401500
    }
  ]
}
```

### 2. Run Evaluation
**POST** `/evaluate/conv-123`

### 3. Fetch Suggestions
**GET** `/suggestions/conv-123`

### 4. Submit Feedback
**POST** `/feedback/conv-123`
```json
{
  "rating": 0.9,
  "annotations": { "comment": "Great tool usage!" }
}
```
