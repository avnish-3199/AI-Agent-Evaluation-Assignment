Senior Engineer: Take-Home Assignment

AI Agent Evaluation Pipeline
Context
Modern AI Agents handle complex, multi-turn customer interactions called and try
to accomplish the goal given to them, as they scale, they need robust infrastructure
to continuously evaluate and improve based on:
● User feedback - explicit ratings, implicit signals (rephrasing, early exits)
● Internal ops feedback - quality reviews, escalations
● Human annotations - labeled conversation samples
The Challenge
Design and build an automated evaluation pipeline that enables continuous
improvement of AI agents in production.
Key Goals:
1. Detect regressions before they impact users
2. Align evals score with the user feedback
3. Identify improvement opportunities across prompts AND tools
4. Generate actionable suggestions automatically
5. Scale to production throughput

Requirements
1. Data Ingestion Layer
   ● Ingest multi-turn conversation logs with full context
   ● Process feedback signals (user ratings, ops annotations, human labels)
   ● Handle high throughput (~1000+ conversations/minute)
   ● Support both batch and real-time processing

2. Evaluation Framework
   Build a modular evaluation system with multiple evaluator types:
   Evaluator Type Purpose
   LLM-as-Judge Assess response quality, helpfulness, factuality
   Tool Call Evaluator Verify correct tool selection and parameter accuracy
   Multi-turn Coherence Check context maintenance and consistency across

turns

Heuristic Checks Format compliance, latency thresholds, required fields

Tool Calling Evaluation should measure:
● Was the correct tool selected for the task?
● Were parameters extracted accurately from context?
● Did hallucinated (made-up) parameters occur?
● Did the tool execution succeed?
Multi-turn Evaluation should measure:
● Coherence across conversation turns
● Consistency (no contradictions)
● Proper handling of references and context
3. Feedback Integration
   ● Ingest annotations from multiple human annotators
   ● Handle annotator disagreement (consider agreement metrics)
   ● Weight evaluations by annotation quality/confidence
   ● Support confidence-based routing (auto-label vs. human review)
4. Self-Updating Mechanism
   This is the key differentiator we want to assess.
   The pipeline should automatically generate improvement suggestions:
   For Prompts:

● Identify failure patterns
● Suggest specific prompt modifications
● Provide rationale and expected impact
For Tools:
● Detect tool schema issues
● Suggest parameter description improvements
● Identify missing validation rules
5. Meta-Evaluation: Improving the Evals Themselves
   The evaluation pipeline should improve itself over time.
   Consider how to:
   ● Calibrate LLM-as-Judge: Compare evaluator scores against human annotations;
   adjust when they diverge
   ● Identify blind spots: Detect failure categories that current evaluators miss
   ● Track evaluator accuracy: Measure precision/recall of automated evaluators
   against human ground truth
   This creates a flywheel:
   Agent outputs → Evaluations → Human feedback → Better evaluators → Better evaluations
   
   Technical Specifications
   Sample Conversation Schema
   {
   &quot;conversation_id&quot;: &quot;conv_abc123&quot;,
   &quot;agent_version&quot;: &quot;v2.3.1&quot;,
   &quot;turns&quot;: [
   {
   &quot;turn_id&quot;: 1,
   &quot;role&quot;: &quot;user&quot;,
   &quot;content&quot;: &quot;I need to book a flight to NYC next week&quot;,
   &quot;timestamp&quot;: &quot;2024-01-15T10:30:00Z&quot;
   },
   {

&quot;turn_id&quot;: 2,
&quot;role&quot;: &quot;assistant&quot;,
&quot;content&quot;: &quot;I&#39;d be happy to help you book a flight to NYC...&quot;,
&quot;tool_calls&quot;: [
{
&quot;tool_name&quot;: &quot;flight_search&quot;,
&quot;parameters&quot;: {
&quot;destination&quot;: &quot;NYC&quot;,
&quot;date_range&quot;: &quot;2024-01-22/2024-01-28&quot;
},
&quot;result&quot;: {&quot;status&quot;: &quot;success&quot;, &quot;flights&quot;: [&quot;...&quot;]},
&quot;latency_ms&quot;: 450
}
],
&quot;timestamp&quot;: &quot;2024-01-15T10:30:02Z&quot;
}
],
&quot;feedback&quot;: {
&quot;user_rating&quot;: 4,
&quot;ops_review&quot;: {
&quot;quality&quot;: &quot;good&quot;,
&quot;notes&quot;: &quot;Correct tool usage&quot;
},
&quot;annotations&quot;: [
{
&quot;type&quot;: &quot;tool_accuracy&quot;,
&quot;label&quot;: &quot;correct&quot;,
&quot;annotator_id&quot;: &quot;ann_001&quot;
}
]
},
&quot;metadata&quot;: {
&quot;total_latency_ms&quot;: 1200,
&quot;mission_completed&quot;: true
}
}
Sample Expected Evaluation Output
{
&quot;evaluation_id&quot;: &quot;eval_xyz789&quot;,
&quot;conversation_id&quot;: &quot;conv_abc123&quot;,
&quot;scores&quot;: {
&quot;overall&quot;: 0.87,

&quot;response_quality&quot;: 0.90,
&quot;tool_accuracy&quot;: 0.95,
&quot;coherence&quot;: 0.85
},
&quot;tool_evaluation&quot;: {
&quot;selection_accuracy&quot;: 1.0,
&quot;parameter_accuracy&quot;: 0.95,
&quot;execution_success&quot;: true
},
&quot;issues_detected&quot;: [
{
&quot;type&quot;: &quot;latency&quot;,
&quot;severity&quot;: &quot;warning&quot;,
&quot;description&quot;: &quot;Response latency 1200ms exceeds 1000ms target&quot;
}
],
&quot;improvement_suggestions&quot;: [
{
&quot;type&quot;: &quot;prompt&quot;,
&quot;suggestion&quot;: &quot;Add explicit date format instruction&quot;,
&quot;rationale&quot;: &quot;Reduce date inference errors&quot;,
&quot;confidence&quot;: 0.72
}
]
}

Deliverables
1. GitHub Repository
   ● Working codebase with clear structure
   ● README with:
   ○ Architecture overview
   ○ Setup instructions
   ○ API documentation
   ● Docker/docker-compose for local development
2. Working Prototype
   ● Hosted demo (Render, Railway, Fly.io, or similar free tier)
   ● API endpoints for:
   ○ Ingesting conversations

○ Running evaluations
○ Querying results
○ Viewing improvement suggestions
● Simple UI (Streamlit/Gradio) or API docs (Swagger)
3. Design Documentation
   ● Architecture decisions: Why did you choose this design?
   ● Scaling strategy: How would this handle 10x, 100x load?
   ● Trade-offs: What did you optimize for and why?

Sample Scenarios
Your pipeline should handle scenarios like:
Scenario 1: Tool Call Regression
After a prompt update, the agent calls flight_search with incorrect date formats,
causing 15% failures.
Expected: Pipeline detects the pattern, alerts, suggests prompt fix.
Scenario 2: Context Loss
On conversations &gt; 5 turns, agent forgets preferences from turn 1-2.
Expected: Coherence evaluator flags context resolution failures.
Scenario 3: Annotator Disagreement
Two annotators disagree on response helpfulness.
Expected: System handles disagreement gracefully, routes to tiebreaker or flags for
review.

Time &amp; Submission
● Estimated effort: 12-15 hours
● Deadline: 2 days from receipt
Submit:

1. GitHub repository link
2. Hosted prototype URL
3. Brief video walkthrough (5-10 min) covering:
   ○ Architecture and key decisions
   ○ Demo of the working system
   ○ What you&#39;d do with more time