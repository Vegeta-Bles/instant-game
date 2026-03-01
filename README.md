# instantgame

`instantgame` is a Java CLI automation script that turns a filled `GENERATE.md` PRD into iterative generation artifacts using a repeatable loop:

1. Read
2. Map
3. Implement
4. Test
5. Repeat

## Quickstart

1. Build the project:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   mvn test
   mvn package
   ```
2. Initialize a new PRD in your current working directory:
   ```bash
   bin/instantgame
   ```
   This creates `./instantgame/GENERATE.md`.
3. Fill out the PRD and run generation:
   ```bash
   bin/instantgame generate
   ```
4. Inspect output under:
   - `./instantgame/generated/<project-slug>/cycle-*/read`
   - `./instantgame/generated/<project-slug>/cycle-*/map`
   - `./instantgame/generated/<project-slug>/cycle-*/implement`
   - `./instantgame/generated/<project-slug>/cycle-*/test`

## Commands

- `bin/instantgame` or `bin/instantgame init`: Create scaffold folder + PRD template.
- `bin/instantgame generate`: Parse PRD and run the loop.
- `bin/instantgame.command [target-directory]`: macOS double-clickable launcher that runs `init`.

## AI Agent Commands

In `GENERATE.md`, set optional per-agent command lines:

- `Code Agent Command`
- `Art Agent Command`
- `Music Agent Command`

Each configured command receives:

- `INSTANTGAME_PROMPT`
- `INSTANTGAME_AGENT`
- `INSTANTGAME_PROJECT_NAME`
- `INSTANTGAME_CYCLE`
- `INSTANTGAME_OUTPUT_PATH`

Command behavior:

- If stdout is produced, InstantGame writes it to the agent artifact file.
- If stdout is empty, the command must write to `INSTANTGAME_OUTPUT_PATH`.
- If no command is configured, InstantGame uses the built-in fallback templates.

## Notes

- Agent toggles in `GENERATE.md` currently support Code, Art, and Music.
- The PRD template includes extensive genre, art style, and music style checklists with custom fill-in fields.
- Unit tests validate command behavior, parsing, stage outputs, and command-backed AI execution.
