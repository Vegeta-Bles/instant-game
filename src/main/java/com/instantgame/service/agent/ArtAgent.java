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
 * Produces art direction artifacts per generation cycle.
 */
public final class ArtAgent implements Agent {

  private final AgentCommandExecutor commandExecutor;

  /** Creates an art agent with the default command executor. */
  public ArtAgent() {
    this(new AgentCommandExecutor());
  }

  /**
   * @param commandExecutor executor for optional external agent commands
   */
  public ArtAgent(AgentCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
  }

  /** {@inheritDoc} */
  @Override
  public String key() {
    return "art";
  }

  /** {@inheritDoc} */
  @Override
  public Path artifactPath(Path implementDirectory, int cycle) {
    return implementDirectory.resolve("art").resolve("cycle-%d-art-direction.md".formatted(cycle));
  }

  /** {@inheritDoc} */
  @Override
  public void generate(
      ProjectBrief brief, Path implementDirectory, int cycle, AgentCollaborationContext context)
      throws IOException {
    Path artifactPath = artifactPath(implementDirectory, cycle);

    String prompt =
        """
        You are the Art Agent for an automated game generation loop.
        
        Project: %s
        Pitch: %s
        Core Loop: %s
        Genres: %s
        Art Styles: %s
        Major Mechanics: %s
        Minor Mechanics: %s
        Collaboration Round: %d/%d
        Competence Profile: %s
        Shared Team Context: %s
        Other Notes: %s
        Cycle: %d
        
        Produce concise markdown with:
        1. Visual direction for this cycle
        2. Asset priorities and naming guidance
        3. UI readability constraints
        4. Concrete refinements based on code/music peer feedback when round > 1
        """
            .formatted(
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                String.join(", ", brief.artStyles().isEmpty() ? List.of("Default house style") : brief.artStyles()),
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

    String styles =
        String.join(", ", brief.artStyles().isEmpty() ? List.of("Default house style") : brief.artStyles());

    String content =
        """
        # Cycle %d Art Direction
        
        ## Agent
        - Art Agent
        
        ## Inputs
        - Project: %s
        - Genres: %s
        - Selected Styles: %s
        - Major Mechanics: %s
        - Minor Mechanics: %s
        - Collaboration Round: %d/%d
        - Competence Profile: %s
        - Shared Team Context: %s
        - Other Notes: %s
        
        ## Deliverables
        - Palette and mood board summary with implementation constraints.
        - Character and environment shape language aligned to mechanics.
        - HUD sketch guidance integrated with code-level UI hooks.
        - Refinement pass notes based on peer outputs.
        """
            .formatted(
                cycle,
                brief.projectName(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                styles,
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
