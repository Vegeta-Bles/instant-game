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
 * Produces music and audio direction artifacts per generation cycle.
 */
public final class MusicAgent implements Agent {

  private final AgentCommandExecutor commandExecutor;

  /** Creates a music agent with the default command executor. */
  public MusicAgent() {
    this(new AgentCommandExecutor());
  }

  /**
   * @param commandExecutor executor for optional external agent commands
   */
  public MusicAgent(AgentCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
  }

  /** {@inheritDoc} */
  @Override
  public String key() {
    return "music";
  }

  /** {@inheritDoc} */
  @Override
  public Path artifactPath(Path implementDirectory, int cycle) {
    return implementDirectory.resolve("music").resolve("cycle-%d-music-direction.md".formatted(cycle));
  }

  /** {@inheritDoc} */
  @Override
  public void generate(
      ProjectBrief brief, Path implementDirectory, int cycle, AgentCollaborationContext context)
      throws IOException {
    Path artifactPath = artifactPath(implementDirectory, cycle);

    String prompt =
        """
        You are the Music Agent for an automated game generation loop.
        
        Project: %s
        Pitch: %s
        Core Loop: %s
        Platforms: %s
        Music Styles: %s
        Major Mechanics: %s
        Minor Mechanics: %s
        Collaboration Round: %d/%d
        Competence Profile: %s
        Shared Team Context: %s
        Other Notes: %s
        Cycle: %d
        
        Produce concise markdown with:
        1. Musical direction and motifs
        2. SFX priorities aligned to gameplay events
        3. Runtime mixing notes
        4. Concrete adjustments from code/art peer feedback when round > 1
        """
            .formatted(
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.musicStyles().isEmpty() ? List.of("None selected") : brief.musicStyles()),
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
        # Cycle %d Music Direction
        
        ## Agent
        - Music Agent
        
        ## Inputs
        - Project: %s
        - Core Loop Tempo: %s
        - Platforms: %s
        - Selected Music Styles: %s
        - Major Mechanics: %s
        - Minor Mechanics: %s
        - Collaboration Round: %d/%d
        - Competence Profile: %s
        - Shared Team Context: %s
        - Other Notes: %s
        
        ## Deliverables
        - Background loop motifs tied to pacing and mechanics.
        - Event-driven stingers for wins/losses and key interactions.
        - Audio implementation notes for runtime mixing and ducking.
        - Refinement changes based on peer outputs.
        """
            .formatted(
                cycle,
                brief.projectName(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.musicStyles().isEmpty() ? List.of("None selected") : brief.musicStyles()),
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
