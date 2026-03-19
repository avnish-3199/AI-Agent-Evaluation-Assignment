# AI Agent Evaluation Pipeline (SDE-II Grade)

An automated evaluation pipeline for continuous improvement of AI agents, featuring multi-dimensional assessment, meta-evaluation calibration, and automated improvement suggestions.

## 🚀 Live Prototype
**Interactive Swagger API Docs:** [https://ai-evaluation-pipeline.onrender.com/swagger-ui/index.html](https://ai-evaluation-pipeline.onrender.com/swagger-ui/index.html#/)

## 🏗️ Architectural Standouts

This solution implements several patterns common in high-scale Swiggy systems:

1.  **Strategy Pattern for Evaluators**: Evaluators are decoupled from the service logic, allowing new metrics (LLM-as-Judge, Tool-Accuracy) to be added without code changes in the orchestration layer.
2.  **Meta-Evaluation Calibration (The Flywheel)**: 
    *   The system doesn't just evaluate conversations; it evaluates its own evaluators.
    *   **Dynamic Weighting**: Scores are weighted by the evaluator's historical accuracy against human ground truth.
    *   **Blind Spot Detection**: Automatically identifies conversations where automated scores significantly diverge from human feedback.
3.  **Confidence-Based Routing (Human-in-the-Loop)**: 
    *   If evaluator disagreement exceeds a threshold (e.g., `0.4`), the evaluation is flagged with a `CONFIDENCE_FLAG`.
    *   This enables a high-throughput pipeline where only "tricky" cases are routed for expensive human review.
4.  **Global Resilience & Standardization**: 
    *   Implemented a `GlobalExceptionHandler` to ensure the API never leaks internal stack traces and always returns standardized JSON error responses.

---

## 🚀 Scaling Strategy (Swiggy Scale: 100k+ TPM)

To handle Swiggy-level throughput (1000+ conv/min), the architecture would evolve as follows:

1.  **Asynchronous Ingestion (Kafka)**:
    *   Move from synchronous REST to an event-driven model.
    *   Ingestion API writes raw logs to a Kafka topic. Workers consume from this topic to run evaluations asynchronously, preventing spike-induced API latency.
2.  **Distributed State (Redis + MongoDB)**:
    *   Replace `ConcurrentHashMap` with **Redis** for hot metadata and **MongoDB** for long-term storage of semi-structured conversation turns.
3.  **Search & Analytics (Elasticsearch)**:
    *   Index evaluation results in Elasticsearch to enable sub-second querying of "all conversations with >20% tool failure in the last hour."
4.  **Batch Processing (Spark/Flink)**:
    *   Run heavy Meta-Evaluation (calibration of millions of turns) as a nightly Spark job rather than in-process.

---

## 📋 API Usage & Payloads

### 1. Ingest Conversation (Single)
`POST /api/conversations`
```json
{
  "conversation_id": "conv_abc123",
  "agent_version": "v2.3.1",
  "metadata": {
    "user_id": "user_789",
    "region": "US-East"
  },
  "turns": [
    {
      "turn_id": 1,
      "role": "user",
      "content": "I need to book a flight to NYC next week",
      "timestamp": "2024-03-19T10:30:00Z"
    },
    {
      "turn_id": 2,
      "role": "assistant",
      "content": "I'd be happy to help you book a flight to NYC...",
      "tool_calls": [
        {
          "tool_name": "flight_search",
          "parameters": {
            "destination": "NYC",
            "date_range": "2024-03-26/2024-04-01"
          },
          "result": {"status": "success", "flights": ["FL123", "FL456"]},
          "latency_ms": 450,
          "execution_success": true
        }
      ],
      "latency_ms": 1200,
      "timestamp": "2024-03-19T10:30:02Z"
    }
  ]
}
```

---

### 3. Batch Ingest Conversations
`POST /api/conversations/batch`
```json
[
  {
    "conversation_id": "conv_batch_001",
    "agent_version": "v2.3.1",
    "turns": [
      {
        "turn_id": 1,
        "role": "user",
        "content": "Find me a table for two at 7pm tonight.",
        "timestamp": "2024-03-19T12:00:00Z"
      }
    ]
  },
  {
    "conversation_id": "conv_batch_002",
    "agent_version": "v2.3.1",
    "turns": [
      {
        "turn_id": 1,
        "role": "user",
        "content": "Cancel my previous reservation.",
        "timestamp": "2024-03-19T12:05:00Z"
      }
    ]
  }
]
```

---

### 4. Submit Human Feedback (Flywheel Trigger)
`POST /api/evaluations/{conversationId}/feedback`
```json
{
  "userRating": 5,
  "opsReview": {
    "quality": "excellent",
    "notes": "Perfect tool selection and response speed."
  },
  "annotations": [
    {
      "type": "tool_accuracy",
      "label": "correct",
      "annotatorId": "human_expert_01",
      "confidence": 0.98
    }
  ]
}
```

---

### 5. Run Evaluation (Consolidated Output)
`POST /api/evaluations/{conversationId}/run`
**Sample Response:**
```json
{
  "evaluationId": "eval_7d2f9a1b",
  "conversationId": "conv_abc123",
  "scores": {
    "overall": 0.92,
    "response_quality": 0.90,
    "tool_accuracy": 0.95,
    "coherence": 0.91
  },
  "toolEvaluation": {
    "selectionAccuracy": 1.0,
    "parameterAccuracy": 0.95,
    "executionSuccess": true
  },
  "issuesDetected": [
    {
      "type": "latency",
      "severity": "warning",
      "description": "High turn latency detected."
    }
  ],
  "improvementSuggestions": [
    {
      "type": "latency",
      "suggestion": "Consider using a smaller model for simple turns...",
      "rationale": "Response latency exceeded 1000ms threshold.",
      "confidence": 0.85
    }
  ]
}
```

---

### 6. Meta-Evaluation Metrics
`GET /api/evaluations/meta-metrics`
Returns accuracy scores for each evaluator against human ground truth.

---

## 🛠️ Run Instructions
1.  **Local**: `./mvnw spring-boot:run`
2.  **Docker**: `docker-compose up --build`
3.  **Swagger**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
