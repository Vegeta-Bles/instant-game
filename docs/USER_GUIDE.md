# InstantGame User Guide

## Purpose

Use `instantgame` to convert a filled PRD (`GENERATE.md`) into iterative generation artifacts. This workflow helps you drive AI-assisted game production from one source of truth.

## Use Cases

- Rapidly prototype game concepts from a structured brief.
- Coordinate code/art/music workstreams from one document.
- Produce deterministic stage artifacts for review and version control.

## Process Flow

1. Run `bin/instantgame` in your working directory.
2. Open `instantgame/GENERATE.md` and fill all required fields.
3. Select genres, art styles, music styles, and gameplay mechanics split into major and minor sections, and fill custom lines when needed.
4. Optionally configure `Code/Art/Music Agent Command` values to run external AI tools.
5. Set `Collaboration Rounds` and `Competence Profile` under the agent collaboration section.
6. Fill detailed planning sections (vision, systems, scope, rough timeline events, constraints, and `Other Notes`).
7. Set iteration count.
8. Run `bin/instantgame generate`.
9. Review outputs under `instantgame/generated/<project-slug>/cycle-*`.

## Required Fields

- `Project Name`
- `One-line Pitch`
- `Core Loop`
- `Target Platforms`
- `Iterations` (must be >= 1)

## Output Layout

- `read/brief-snapshot.md`: Snapshot of parsed requirements.
- `map/scaffold-plan.md`: Generated project scaffold plan.
- `implement/*`: Agent artifacts (code, art, music).
- `implement/collaboration/round-<n>-shared-context.md`: Cross-agent synthesis and refinement context for each collaboration round.
- `test/test-report.md`: Stage checks and pass/fail result.

## External AI Command Contract

When an agent command is configured in `GENERATE.md`:

- The process receives `INSTANTGAME_PROMPT` and related context env vars, including collaboration metadata (`INSTANTGAME_COLLAB_ROUND`, `INSTANTGAME_COLLAB_TOTAL_ROUNDS`, `INSTANTGAME_COMPETENCE_PROFILE`, and `INSTANTGAME_SHARED_CONTEXT`).
- Stdout is captured into the agent artifact file.
- If stdout is empty, the process must write its result to `INSTANTGAME_OUTPUT_PATH`.
- Non-zero exit codes fail generation immediately.

## Double-Click Mode (macOS)

- Double-click `bin/instantgame.command` to run the init flow.
- You can also run `bin/instantgame.command /path/to/your/project` from terminal.

## Versioning

- Run `bin/instantgame version` or `bin/instantgame --version` to print the current CLI version.
- The launcher rebuilds `target/instantgame.jar` automatically when source files or build config are newer than the jar.

## Troubleshooting

- If `GENERATE.md` is missing, run `bin/instantgame` first.
- If generation fails, open `test-report.md` for missing artifacts.
- If build tooling fails, set `JAVA_HOME` to Java 21 and rerun `mvn test`.
