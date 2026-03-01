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
3. Select genres, art styles, and music styles, and fill custom style lines when needed.
4. Optionally configure `Code/Art/Music Agent Command` values to run external AI tools.
5. Fill detailed planning sections (vision, systems, scope, constraints, and `Other Notes`).
6. Set iteration count.
7. Run `bin/instantgame generate`.
8. Review outputs under `instantgame/generated/<project-slug>/cycle-*`.

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
- `test/test-report.md`: Stage checks and pass/fail result.

## External AI Command Contract

When an agent command is configured in `GENERATE.md`:

- The process receives `INSTANTGAME_PROMPT` and related context env vars.
- Stdout is captured into the agent artifact file.
- If stdout is empty, the process must write its result to `INSTANTGAME_OUTPUT_PATH`.
- Non-zero exit codes fail generation immediately.

## Double-Click Mode (macOS)

- Double-click `bin/instantgame.command` to run the init flow.
- You can also run `bin/instantgame.command /path/to/your/project` from terminal.

## Troubleshooting

- If `GENERATE.md` is missing, run `bin/instantgame` first.
- If generation fails, open `test-report.md` for missing artifacts.
- If build tooling fails, set `JAVA_HOME` to Java 21 and rerun `mvn test`.
