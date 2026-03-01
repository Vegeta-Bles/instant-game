package com.instantgame.service;

import com.instantgame.model.ProjectBrief;
import com.instantgame.service.agent.Agent;
import com.instantgame.service.agent.AgentCollaborationContext;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Executes the repeatable generation loop: read, map, implement, test.
 */
public final class PipelineRunner {

  private final Map<String, Agent> agentsByKey;

  /**
   * @param agents available agent implementations
   */
  public PipelineRunner(List<Agent> agents) {
    Objects.requireNonNull(agents, "agents");

    this.agentsByKey =
        agents.stream()
            .collect(
                Collectors.toMap(
                    Agent::key,
                    agent -> agent,
                    (left, right) -> left,
                    LinkedHashMap::new));
  }

  /**
   * Runs the generation loop for the configured number of iterations.
   *
   * @param brief project brief loaded from GENERATE.md
   * @param projectDirectory project root (contains GENERATE.md)
   * @param out stdout stream
   * @return generation report
   * @throws IOException when disk operations fail
   */
  public PipelineReport run(ProjectBrief brief, Path projectDirectory, PrintStream out) throws IOException {
    Objects.requireNonNull(brief, "brief");
    Objects.requireNonNull(projectDirectory, "projectDirectory");
    Objects.requireNonNull(out, "out");

    Path outputRoot = projectDirectory.resolve("generated").resolve(brief.slug());
    Files.createDirectories(outputRoot);

    List<String> executedStages = new ArrayList<>();
    Set<String> selectedAgentKeys = new LinkedHashSet<>(brief.enabledAgents());

    if (selectedAgentKeys.isEmpty()) {
      selectedAgentKeys.addAll(agentsByKey.keySet());
    }

    for (int cycle = 1; cycle <= brief.iterations(); cycle++) {
      out.printf("Cycle %d/%d%n", cycle, brief.iterations());

      Path cycleDirectory = outputRoot.resolve("cycle-" + cycle);
      Path readDirectory = cycleDirectory.resolve("read");
      Path mapDirectory = cycleDirectory.resolve("map");
      Path implementDirectory = cycleDirectory.resolve("implement");
      Path testDirectory = cycleDirectory.resolve("test");

      Files.createDirectories(readDirectory);
      Files.createDirectories(mapDirectory);
      Files.createDirectories(implementDirectory);
      Files.createDirectories(testDirectory);

      runReadStage(brief, readDirectory);
      executedStages.add("read");

      runMapStage(brief, mapDirectory, selectedAgentKeys);
      executedStages.add("map");

      runImplementStage(brief, implementDirectory, cycle, selectedAgentKeys);
      executedStages.add("implement");

      runTestStage(brief, cycleDirectory, testDirectory, selectedAgentKeys);
      executedStages.add("test");
    }

    return new PipelineReport(brief.iterations(), outputRoot, List.copyOf(executedStages));
  }

  private static void runReadStage(ProjectBrief brief, Path readDirectory) throws IOException {
    String content =
        """
        # Read Stage Snapshot
        
        - Timestamp: %s
        - Project Name: %s
        - Pitch: %s
        - Core Loop: %s
        - Platforms: %s
        - Genres: %s
        - Art Styles: %s
        - Music Styles: %s
        - Major Mechanics: %s
        - Minor Mechanics: %s
        - Collaboration Rounds: %d
        - Competence Profile: %s
        - Other Notes: %s
        - External Agent Commands: %s
        """
            .formatted(
                Instant.now(),
                brief.projectName(),
                brief.oneLinePitch(),
                brief.coreLoop(),
                brief.targetPlatforms(),
                String.join(", ", brief.genres().isEmpty() ? List.of("None selected") : brief.genres()),
                String.join(
                    ", ", brief.artStyles().isEmpty() ? List.of("None selected") : brief.artStyles()),
                String.join(
                    ", ", brief.musicStyles().isEmpty() ? List.of("None selected") : brief.musicStyles()),
                String.join(
                    ", ", brief.majorMechanics().isEmpty() ? List.of("None selected") : brief.majorMechanics()),
                String.join(
                    ", ", brief.minorMechanics().isEmpty() ? List.of("None selected") : brief.minorMechanics()),
                brief.collaborationRounds(),
                brief.competenceProfile(),
                brief.otherNotes().isBlank() ? "None provided" : brief.otherNotes(),
                brief.agentCommands().isEmpty()
                    ? "None configured"
                    : String.join(", ", brief.agentCommands().keySet()));

    Files.writeString(
        readDirectory.resolve("brief-snapshot.md"),
        content,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
  }

  private static void runMapStage(ProjectBrief brief, Path mapDirectory, Set<String> selectedAgentKeys)
      throws IOException {
    List<String> scaffolding =
        List.of(
            "game/src",
            "game/assets/art",
            "game/assets/music",
            "game/tests",
            "pipeline/logs",
            "pipeline/reports");

    for (String relative : scaffolding) {
      Files.createDirectories(mapDirectory.resolve(relative));
    }

    String plan =
        """
        # Map Stage Plan
        
        ## Overview
        - Project: %s
        - Loop: %s
        - Iterations: %d
        - Enabled Agents: %s
        
        ## Scaffolding
        %s
        """
            .formatted(
                brief.projectName(),
                brief.coreLoop(),
                brief.iterations(),
                String.join(", ", selectedAgentKeys),
                scaffolding.stream().map(path -> "- " + path).collect(Collectors.joining("\n")));

    Files.writeString(
        mapDirectory.resolve("scaffold-plan.md"),
        plan,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
  }

  private void runImplementStage(
      ProjectBrief brief, Path implementDirectory, int cycle, Set<String> selectedAgentKeys)
      throws IOException {
    int rounds = Math.max(1, brief.collaborationRounds());
    Path collaborationDirectory = implementDirectory.resolve("collaboration");
    Files.createDirectories(collaborationDirectory);

    Map<String, Path> artifactPaths = new LinkedHashMap<>();
    for (String key : selectedAgentKeys) {
      Agent agent = requireAgent(key);
      artifactPaths.put(key, agent.artifactPath(implementDirectory, cycle));
    }

    String sharedSummary = "Round 0 baseline: no artifacts generated yet.";

    for (int round = 1; round <= rounds; round++) {
      for (String key : selectedAgentKeys) {
        Agent agent = requireAgent(key);
        AgentCollaborationContext context =
            new AgentCollaborationContext(
                round,
                rounds,
                brief.competenceProfile(),
                sharedSummary,
                Map.copyOf(artifactPaths));
        agent.generate(brief, implementDirectory, cycle, context);
      }
      sharedSummary = buildSharedSummary(round, artifactPaths, brief.competenceProfile());
      writeRoundSummary(collaborationDirectory, round, sharedSummary);
    }
  }

  private Agent requireAgent(String key) {
    Agent agent = agentsByKey.get(key);
    if (agent == null) {
      throw new IllegalArgumentException("No agent registered for key: " + key);
    }
    return agent;
  }

  private static void writeRoundSummary(Path collaborationDirectory, int round, String sharedSummary)
      throws IOException {
    Files.writeString(
        collaborationDirectory.resolve("round-%d-shared-context.md".formatted(round)),
        sharedSummary,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
  }

  private static String buildSharedSummary(
      int round, Map<String, Path> artifactPaths, String competenceProfile) throws IOException {
    StringBuilder builder = new StringBuilder();
    builder.append("# Collaboration Summary - Round ").append(round).append("\n\n");
    builder.append("- Competence Profile: ").append(competenceProfile).append("\n");
    builder.append("- Objective: cross-critique and refine all agent outputs.\n\n");

    for (Map.Entry<String, Path> entry : artifactPaths.entrySet()) {
      builder.append("## ").append(entry.getKey()).append(" Artifact\n");
      builder.append("- Path: ").append(entry.getValue()).append("\n");
      if (!Files.exists(entry.getValue())) {
        builder.append("- Status: missing\n\n");
        continue;
      }

      String content = Files.readString(entry.getValue(), StandardCharsets.UTF_8).trim();
      String condensed = content.replaceAll("\\s+", " ");
      String excerpt = condensed.length() > 500 ? condensed.substring(0, 500) + "..." : condensed;
      builder.append("- Excerpt: ").append(excerpt.isBlank() ? "<empty>" : excerpt).append("\n\n");
    }

    builder.append("## Refinement Focus\n");
    builder.append("- Improve alignment between code, art, and music.\n");
    builder.append("- Raise specificity and implementation clarity.\n");
    builder.append("- Preserve testability and delivery realism.\n");
    return builder.toString();
  }

  private static void runTestStage(
      ProjectBrief brief, Path cycleDirectory, Path testDirectory, Set<String> selectedAgentKeys)
      throws IOException {
    List<String> failures = new ArrayList<>();

    Path scaffoldPlan = cycleDirectory.resolve("map/scaffold-plan.md");
    if (!Files.exists(scaffoldPlan)) {
      failures.add("Missing map/scaffold-plan.md");
    }
    Path collaborationSummary = cycleDirectory.resolve("implement/collaboration/round-1-shared-context.md");
    if (!Files.exists(collaborationSummary)) {
      failures.add("Missing implement/collaboration/round-1-shared-context.md");
    }

    if (selectedAgentKeys.contains("code")
        && !Files.exists(expectedCodeArtifact(cycleDirectory))) {
      failures.add("Missing implement/code/%s".formatted(expectedCodeArtifact(cycleDirectory).getFileName()));
    }
    if (selectedAgentKeys.contains("art")
        && !Files.exists(expectedArtArtifact(cycleDirectory))) {
      failures.add("Missing implement/art/%s".formatted(expectedArtArtifact(cycleDirectory).getFileName()));
    }
    if (selectedAgentKeys.contains("music")
        && !Files.exists(expectedMusicArtifact(cycleDirectory))) {
      failures.add(
          "Missing implement/music/%s".formatted(expectedMusicArtifact(cycleDirectory).getFileName()));
    }

    String summary =
        """
        # Test Stage Report
        
        - Project: %s
        - Result: %s
        
        ## Checks
        %s
        """
            .formatted(
                brief.projectName(),
                failures.isEmpty() ? "PASS" : "FAIL",
                failures.isEmpty()
                    ? "- All required artifacts were generated."
                    : failures.stream().map(item -> "- " + item).collect(Collectors.joining("\n")));

    Files.writeString(
        testDirectory.resolve("test-report.md"),
        summary,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);

    if (!failures.isEmpty()) {
      throw new IOException("Test stage failed with %d issue(s).".formatted(failures.size()));
    }
  }

  private static String cycleSuffix(Path cycleDirectory) {
    String name = cycleDirectory.getFileName().toString();
    return name.replace("cycle-", "");
  }

  private static Path expectedCodeArtifact(Path cycleDirectory) {
    return cycleDirectory.resolve("implement/code/cycle-%s-implementation.md".formatted(cycleSuffix(cycleDirectory)));
  }

  private static Path expectedArtArtifact(Path cycleDirectory) {
    return cycleDirectory.resolve("implement/art/cycle-%s-art-direction.md".formatted(cycleSuffix(cycleDirectory)));
  }

  private static Path expectedMusicArtifact(Path cycleDirectory) {
    return cycleDirectory.resolve("implement/music/cycle-%s-music-direction.md".formatted(cycleSuffix(cycleDirectory)));
  }
}
