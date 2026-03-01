package com.instantgame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.model.ProjectBrief;
import com.instantgame.service.agent.ArtAgent;
import com.instantgame.service.agent.CodeAgent;
import com.instantgame.service.agent.MusicAgent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link PipelineRunner}.
 */
class PipelineRunnerTest {

  @TempDir Path tempDir;

  @Test
  void runsAllStagesAcrossIterations() throws IOException {
    ProjectBrief brief =
        new ProjectBrief(
            "Star Forge",
            "Build and defend orbital stations.",
            "Mine ore, craft modules, repel raids.",
            "Desktop",
            List.of("Strategy"),
            List.of("Low Poly 3D"),
            Set.of("code", "art", "music"),
            2);

    PipelineRunner runner =
        new PipelineRunner(List.of(new CodeAgent(), new ArtAgent(), new MusicAgent()));

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PipelineReport report = runner.run(brief, tempDir, new PrintStream(output));

    assertEquals(2, report.completedCycles());
    assertTrue(Files.exists(report.outputRoot().resolve("cycle-1/read/brief-snapshot.md")));
    assertTrue(Files.exists(report.outputRoot().resolve("cycle-1/map/scaffold-plan.md")));
    assertTrue(Files.exists(report.outputRoot().resolve("cycle-1/implement/code/cycle-1-implementation.md")));
    assertTrue(Files.exists(report.outputRoot().resolve("cycle-1/implement/art/cycle-1-art-direction.md")));
    assertTrue(Files.exists(report.outputRoot().resolve("cycle-1/implement/music/cycle-1-music-direction.md")));
    assertTrue(
        Files.exists(report.outputRoot().resolve("cycle-1/implement/collaboration/round-1-shared-context.md")));
    assertTrue(
        Files.exists(report.outputRoot().resolve("cycle-1/implement/collaboration/round-2-shared-context.md")));
    String roundTwoSummary =
        Files.readString(report.outputRoot().resolve("cycle-1/implement/collaboration/round-2-shared-context.md"));
    assertTrue(roundTwoSummary.contains("Collaboration Summary - Round 2"));
    assertTrue(roundTwoSummary.contains("Competence Profile: extreme-99999"));
    assertTrue(Files.exists(report.outputRoot().resolve("cycle-1/test/test-report.md")));
    assertEquals(8, report.executedStages().size());
    assertTrue(output.toString().contains("Cycle 1/2"));
  }

  @Test
  void failsWhenUnknownAgentIsSelected() {
    ProjectBrief brief =
        new ProjectBrief(
            "Unknown Agent Game",
            "Pitch",
            "Loop",
            "Web",
            List.of(),
            List.of(),
            Set.of("voice"),
            1);

    PipelineRunner runner = new PipelineRunner(List.of(new CodeAgent()));

    assertThrows(IllegalArgumentException.class, () -> runner.run(brief, tempDir, System.out));
  }
}
