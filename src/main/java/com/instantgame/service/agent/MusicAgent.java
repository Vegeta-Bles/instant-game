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
  public void generate(ProjectBrief brief, Path implementDirectory, int cycle) throws IOException {
    Path artifactPath = implementDirectory.resolve("music").resolve("cycle-%d-music-direction.md".formatted(cycle));

    String prompt =
        """
        You are the Music Agent for an automated game generation loop.
        
        Project: %s
        Pitch: %s
        Core Loop: %s
        Platforms: %s
        Music Styles: %s
        Other Notes: %s
        Cycle: %d
        
        Produce concise markdown with:
        1. Musical direction and motifs
        2. SFX priorities aligned to gameplay events
        3. Runtime mixing notes
        """
            .formatted(
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.musicStyles().isEmpty() ? List.of("None selected") : brief.musicStyles()),
                brief.otherNotes().isBlank() ? "None provided" : brief.otherNotes(),
                cycle);

    if (commandExecutor.executeConfiguredCommand(brief, key(), prompt, artifactPath, cycle)) {
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
        - Other Notes: %s
        
        ## Deliverables
        - Background loop motifs.
        - Event-driven stingers for wins/losses.
        - Audio implementation notes for runtime mixing.
        """
            .formatted(
                cycle,
                brief.projectName(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.musicStyles().isEmpty() ? List.of("None selected") : brief.musicStyles()),
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
