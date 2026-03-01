# InstantGame Agent Guide

## Goal

Describe how InstantGame agents cooperate during `instantgame generate`.

## Active Agents

- `code`: Produces implementation planning artifacts.
- `art`: Produces visual direction artifacts.
- `music`: Produces audio direction artifacts.

Agents are enabled from `GENERATE.md` under `## Agent Toggles`.
Each agent can optionally run an external command from `## AI Agent Commands (Optional)`.

## Loop Contract

Each cycle executes this fixed sequence:

1. `read`
   - Parse and snapshot PRD inputs into `read/brief-snapshot.md`.
2. `map`
   - Create canonical scaffold paths and `map/scaffold-plan.md`.
3. `implement`
   - Invoke each selected agent to emit cycle-specific artifacts.
   - If an agent command is configured, run that command and capture stdout.
   - If no command is configured, use built-in fallback templates.
   - Agents also receive parsed genres, art styles, music styles, mechanics, and `Other Notes`.
4. `test`
   - Verify expected outputs exist.
   - Write `test/test-report.md` with PASS/FAIL.
5. `repeat`
   - Start next cycle until `Iterations` is exhausted.

## Adding New Agents

1. Implement `com.instantgame.service.agent.Agent`.
2. Return a stable key (for example, `narrative`).
3. Register the agent in `InstantGameCli` when constructing `PipelineRunner`.
4. Add a checkbox entry in `GenerateTemplateWriter.TEMPLATE`.
5. Extend tests for parser mapping and pipeline artifact checks.

## External Command Contract

Configured commands receive these environment variables:

- `INSTANTGAME_PROMPT`
- `INSTANTGAME_AGENT`
- `INSTANTGAME_PROJECT_NAME`
- `INSTANTGAME_CYCLE`
- `INSTANTGAME_OUTPUT_PATH`

Output rules:

- Preferred: print markdown to stdout.
- Alternative: write directly to `INSTANTGAME_OUTPUT_PATH`.
- Non-zero exit codes fail the current generation run.

## Testing Expectations

- Unit tests must cover agent output generation paths.
- Pipeline tests must validate the loop and required artifacts.
- Command-level tests must verify error handling and happy paths.
