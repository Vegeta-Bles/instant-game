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
  public Path artifactPath(Path implementDirectory, int cycle) {
    return implementDirectory.resolve("code").resolve("cycle-%d-implementation.md".formatted(cycle));
  }

  /** {@inheritDoc} */
  @Override
  public void generate(
      ProjectBrief brief, Path implementDirectory, int cycle, AgentCollaborationContext context)
      throws IOException {
    Path artifactPath = artifactPath(implementDirectory, cycle);

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
        Collaboration Round: %d/%d
        Competence Profile: %s
        Shared Team Context: %s
        Other Notes: %s
        Cycle: %d
        
        Produce concise markdown with:
        1. Core systems to implement this cycle
        2. Integration points for art and music
        3. Unit test priorities
        4. Concrete revisions from peer feedback when round > 1
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
                context.round(),
                context.totalRounds(),
                context.competenceProfile(),
                context.sharedContextSummary(),
                brief.otherNotes().isBlank() ? "None provided" : brief.otherNotes(),
                cycle);

    if (commandExecutor.executeConfiguredCommand(brief, key(), prompt, artifactPath, cycle, context)) {
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
        - Collaboration Round: %d/%d
        - Competence Profile: %s
        - Shared Team Context: %s
        - Other Notes: %s
        
        ## Tasks
        - Design production-ready architecture and concrete module seams.
        - Integrate explicit hooks for art asset loading and music event routing.
        - Define strict unit-test acceptance criteria for all core systems.
        - Apply peer-review refinements from previous rounds.
        - Emit traceable logs and deterministic outputs for CI.
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
                context.round(),
                context.totalRounds(),
                context.competenceProfile(),
                context.sharedContextSummary(),
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
