package com.instantgame.service;

import com.instantgame.model.ProjectBrief;
import com.instantgame.service.agent.Agent;
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
    for (String key : selectedAgentKeys) {
      Agent agent = agentsByKey.get(key);
      if (agent == null) {
        throw new IllegalArgumentException("No agent registered for key: " + key);
      }
      agent.generate(brief, implementDirectory, cycle);
    }
  }

  private static void runTestStage(
      ProjectBrief brief, Path cycleDirectory, Path testDirectory, Set<String> selectedAgentKeys)
      throws IOException {
    List<String> failures = new ArrayList<>();

    Path scaffoldPlan = cycleDirectory.resolve("map/scaffold-plan.md");
    if (!Files.exists(scaffoldPlan)) {
      failures.add("Missing map/scaffold-plan.md");
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
