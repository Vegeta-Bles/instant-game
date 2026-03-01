package com.instantgame.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parsed representation of {@code GENERATE.md}.
 */
public final class ProjectBrief {

  private final String projectName;
  private final String oneLinePitch;
  private final String coreLoop;
  private final String targetPlatforms;
  private final List<String> genres;
  private final List<String> artStyles;
  private final List<String> musicStyles;
  private final List<String> mechanics;
  private final Set<String> enabledAgents;
  private final Map<String, String> agentCommands;
  private final int iterations;
  private final String otherNotes;

  /**
   * Creates a validated immutable project brief.
   *
   * @param projectName game name from PRD
   * @param oneLinePitch one-line summary
   * @param coreLoop player loop description
   * @param targetPlatforms target platform description
   * @param genres selected genres
   * @param artStyles selected art directions
   * @param musicStyles selected music directions
   * @param mechanics selected gameplay mechanics
   * @param enabledAgents selected agents
   * @param agentCommands configured command lines keyed by agent (for example code/art/music)
   * @param iterations number of generation cycles
   * @param otherNotes optional notes for additional implementation context
   */
  public ProjectBrief(
      String projectName,
      String oneLinePitch,
      String coreLoop,
      String targetPlatforms,
      List<String> genres,
      List<String> artStyles,
      List<String> musicStyles,
      List<String> mechanics,
      Set<String> enabledAgents,
      Map<String, String> agentCommands,
      int iterations,
      String otherNotes) {
    this.projectName = Objects.requireNonNull(projectName, "projectName").trim();
    this.oneLinePitch = Objects.requireNonNull(oneLinePitch, "oneLinePitch").trim();
    this.coreLoop = Objects.requireNonNull(coreLoop, "coreLoop").trim();
    this.targetPlatforms = Objects.requireNonNull(targetPlatforms, "targetPlatforms").trim();
    this.genres = List.copyOf(Objects.requireNonNull(genres, "genres"));
    this.artStyles = List.copyOf(Objects.requireNonNull(artStyles, "artStyles"));
    this.musicStyles = List.copyOf(Objects.requireNonNull(musicStyles, "musicStyles"));
    this.mechanics = List.copyOf(Objects.requireNonNull(mechanics, "mechanics"));
    this.enabledAgents =
        Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(enabledAgents, "enabledAgents")));
    this.agentCommands =
        Map.copyOf(
            Objects.requireNonNull(agentCommands, "agentCommands").entrySet().stream()
                .collect(
                    Collectors.toMap(
                        entry -> entry.getKey().toLowerCase(Locale.ROOT).trim(),
                        entry -> entry.getValue().trim())));
    this.iterations = iterations;
    this.otherNotes = Objects.requireNonNull(otherNotes, "otherNotes").trim();

    if (this.projectName.isEmpty()) {
      throw new IllegalArgumentException("Project Name cannot be empty.");
    }
    if (this.oneLinePitch.isEmpty()) {
      throw new IllegalArgumentException("One-line Pitch cannot be empty.");
    }
    if (this.coreLoop.isEmpty()) {
      throw new IllegalArgumentException("Core Loop cannot be empty.");
    }
    if (this.targetPlatforms.isEmpty()) {
      throw new IllegalArgumentException("Target Platforms cannot be empty.");
    }
    if (iterations < 1) {
      throw new IllegalArgumentException("Iterations must be at least 1.");
    }
  }

  /**
   * Creates a validated immutable project brief with default empty {@code otherNotes}.
   *
   * @param projectName game name from PRD
   * @param oneLinePitch one-line summary
   * @param coreLoop player loop description
   * @param targetPlatforms target platform description
   * @param genres selected genres
   * @param artStyles selected art directions
   * @param musicStyles selected music directions
   * @param enabledAgents selected agents
   * @param agentCommands configured command lines keyed by agent (for example code/art/music)
   * @param iterations number of generation cycles
   */
  public ProjectBrief(
      String projectName,
      String oneLinePitch,
      String coreLoop,
      String targetPlatforms,
      List<String> genres,
      List<String> artStyles,
      List<String> musicStyles,
      Set<String> enabledAgents,
      Map<String, String> agentCommands,
      int iterations) {
    this(
        projectName,
        oneLinePitch,
        coreLoop,
        targetPlatforms,
        genres,
        artStyles,
        musicStyles,
        List.of(),
        enabledAgents,
        agentCommands,
        iterations,
        "");
  }

  /**
   * Creates a validated immutable project brief with default empty {@code otherNotes}.
   *
   * @param projectName game name from PRD
   * @param oneLinePitch one-line summary
   * @param coreLoop player loop description
   * @param targetPlatforms target platform description
   * @param genres selected genres
   * @param artStyles selected art directions
   * @param musicStyles selected music directions
   * @param mechanics selected gameplay mechanics
   * @param enabledAgents selected agents
   * @param agentCommands configured command lines keyed by agent (for example code/art/music)
   * @param iterations number of generation cycles
   */
  public ProjectBrief(
      String projectName,
      String oneLinePitch,
      String coreLoop,
      String targetPlatforms,
      List<String> genres,
      List<String> artStyles,
      List<String> musicStyles,
      List<String> mechanics,
      Set<String> enabledAgents,
      Map<String, String> agentCommands,
      int iterations) {
    this(
        projectName,
        oneLinePitch,
        coreLoop,
        targetPlatforms,
        genres,
        artStyles,
        musicStyles,
        mechanics,
        enabledAgents,
        agentCommands,
        iterations,
        "");
  }

  /** @return project name */
  public String projectName() {
    return projectName;
  }

  /** @return one-line pitch */
  public String oneLinePitch() {
    return oneLinePitch;
  }

  /** @return core gameplay loop */
  public String coreLoop() {
    return coreLoop;
  }

  /** @return target platforms */
  public String targetPlatforms() {
    return targetPlatforms;
  }

  /** @return selected genres */
  public List<String> genres() {
    return genres;
  }

  /** @return selected art styles */
  public List<String> artStyles() {
    return artStyles;
  }

  /** @return selected music styles */
  public List<String> musicStyles() {
    return musicStyles;
  }

  /** @return selected gameplay mechanics */
  public List<String> mechanics() {
    return mechanics;
  }

  /** @return selected agent names */
  public Set<String> enabledAgents() {
    return enabledAgents;
  }

  /** @return configured command map keyed by agent id */
  public Map<String, String> agentCommands() {
    return agentCommands;
  }

  /**
   * Looks up an external command for an agent.
   *
   * @param agentName agent key
   * @return optional command string
   */
  public Optional<String> agentCommand(String agentName) {
    String normalized = Objects.requireNonNull(agentName, "agentName").toLowerCase(Locale.ROOT).trim();
    String command = agentCommands.get(normalized);
    return command == null || command.isBlank() ? Optional.empty() : Optional.of(command);
  }

  /** @return number of loop repetitions */
  public int iterations() {
    return iterations;
  }

  /** @return optional additional notes */
  public String otherNotes() {
    return otherNotes;
  }

  /**
   * Checks if the brief enables a specific agent.
   *
   * @param agentName agent key
   * @return true if selected
   */
  public boolean isAgentEnabled(String agentName) {
    String normalized = Objects.requireNonNull(agentName, "agentName").toLowerCase(Locale.ROOT);
    return enabledAgents.stream().map(value -> value.toLowerCase(Locale.ROOT)).anyMatch(normalized::equals);
  }

  /**
   * Produces a filesystem-safe project slug.
   *
   * @return lowercase alphanumeric slug with hyphen separators
   */
  public String slug() {
    String sanitized = projectName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    sanitized = sanitized.replaceAll("^-+", "").replaceAll("-+$", "");
    return sanitized.isEmpty() ? "project" : sanitized;
  }

  /**
   * Creates a validated immutable project brief with no external commands, music styles, or notes.
   *
   * @param projectName game name from PRD
   * @param oneLinePitch one-line summary
   * @param coreLoop player loop description
   * @param targetPlatforms target platform description
   * @param genres selected genres
   * @param artStyles selected art directions
   * @param enabledAgents selected agents
   * @param iterations number of generation cycles
   */
  public ProjectBrief(
      String projectName,
      String oneLinePitch,
      String coreLoop,
      String targetPlatforms,
      List<String> genres,
      List<String> artStyles,
      Set<String> enabledAgents,
      int iterations) {
    this(
        projectName,
        oneLinePitch,
        coreLoop,
        targetPlatforms,
        genres,
        artStyles,
        List.of(),
        List.of(),
        enabledAgents,
        Map.of(),
        iterations,
        "");
  }
}
