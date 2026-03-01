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
  public void generate(ProjectBrief brief, Path implementDirectory, int cycle) throws IOException {
    Path artifactPath = implementDirectory.resolve("art").resolve("cycle-%d-art-direction.md".formatted(cycle));

    String prompt =
        """
        You are the Art Agent for an automated game generation loop.
        
        Project: %s
        Pitch: %s
        Core Loop: %s
        Genres: %s
        Art Styles: %s
        Mechanics: %s
        Other Notes: %s
        Cycle: %d
        
        Produce concise markdown with:
        1. Visual direction for this cycle
        2. Asset priorities and naming guidance
        3. UI readability constraints
        """
            .formatted(
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                String.join(", ", brief.artStyles().isEmpty() ? List.of("Default house style") : brief.artStyles()),
                String.join(", ", brief.mechanics().isEmpty() ? List.of("None selected") : brief.mechanics()),
                brief.otherNotes().isBlank() ? "None provided" : brief.otherNotes(),
                cycle);

    if (commandExecutor.executeConfiguredCommand(brief, key(), prompt, artifactPath, cycle)) {
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
        - Mechanics: %s
        - Other Notes: %s
        
        ## Deliverables
        - Palette and mood board summary.
        - Character and environment shape language.
        - HUD sketch guidance for programmers.
        """
            .formatted(
                cycle,
                brief.projectName(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                styles,
                String.join(", ", brief.mechanics().isEmpty() ? List.of("None selected") : brief.mechanics()),
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
