package com.instantgame.service.agent;

import com.instantgame.model.ProjectBrief;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

/**
 * Produces code-planning artifacts and implementation notes.
 */
public final class CodeAgent implements Agent {

  private final AgentCommandExecutor commandExecutor;

  /** Creates a code agent with the default command executor. */
  public CodeAgent() {
    this(new AgentCommandExecutor());
  }

  /**
   * @param commandExecutor executor for optional external agent commands
   */
  public CodeAgent(AgentCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
  }

  /** {@inheritDoc} */
  @Override
  public String key() {
    return "code";
  }

  /** {@inheritDoc} */
  @Override
  public void generate(ProjectBrief brief, Path implementDirectory, int cycle) throws IOException {
    Path artifactPath = implementDirectory.resolve("code").resolve("cycle-%d-implementation.md".formatted(cycle));

    String prompt =
        """
        You are the Code Agent for an automated game generation loop.
        
        Project: %s
        Pitch: %s
        Core Loop: %s
        Platforms: %s
        Genres: %s
        Major Mechanics: %s
        Minor Mechanics: %s
        Other Notes: %s
        Cycle: %d
        
        Produce concise markdown with:
        1. Core systems to implement this cycle
        2. Integration points for art and music
        3. Unit test priorities
        """
            .formatted(
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                String.join(
                    ", ",
                    brief.majorMechanics().isEmpty() ? List.of("None selected") : brief.majorMechanics()),
                String.join(
                    ", ",
                    brief.minorMechanics().isEmpty() ? List.of("None selected") : brief.minorMechanics()),
                brief.otherNotes().isBlank() ? "None provided" : brief.otherNotes(),
                cycle);

    if (commandExecutor.executeConfiguredCommand(brief, key(), prompt, artifactPath, cycle)) {
      return;
    }

    String content =
        """
        # Cycle %d Implementation Plan
        
        ## Agent
        - Code Agent
        
        ## Inputs
        - Project: %s
        - Pitch: %s
        - Core Loop: %s
        - Platforms: %s
        - Genres: %s
        - Major Mechanics: %s
        - Minor Mechanics: %s
        - Other Notes: %s
        
        ## Tasks
        - Create core loop scaffolding.
        - Add adapter hooks for art and music assets.
        - Keep systems testable with unit-level seams.
        - Emit traceable logs for each loop stage.
        """
            .formatted(
                cycle,
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                String.join(
                    ", ",
                    brief.majorMechanics().isEmpty() ? List.of("None selected") : brief.majorMechanics()),
                String.join(
                    ", ",
                    brief.minorMechanics().isEmpty() ? List.of("None selected") : brief.minorMechanics()),
                brief.otherNotes().isBlank() ? "None provided" : brief.otherNotes());

    Files.createDirectories(artifactPath.getParent());
    Files.writeString(
        artifactPath,
        content,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
  }
}
