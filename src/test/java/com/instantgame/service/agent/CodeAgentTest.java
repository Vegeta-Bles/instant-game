package com.instantgame.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.model.ProjectBrief;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link CodeAgent}.
 */
class CodeAgentTest {

  @TempDir Path tempDir;

  @Test
  void usesConfiguredCommandWhenPresent() throws Exception {
    ProjectBrief brief =
        new ProjectBrief(
            "Agent Exec",
            "Pitch",
            "Loop",
            "Desktop",
            List.of("Action"),
            List.of(),
            List.of(),
            Set.of("code"),
            Map.of("code", "printf '# AI CODE OUTPUT'"),
            1);

    CodeAgent agent = new CodeAgent();
    agent.generate(brief, tempDir, 1);

    Path artifact = tempDir.resolve("code/cycle-1-implementation.md");
    assertEquals("# AI CODE OUTPUT", Files.readString(artifact));
  }

  @Test
  void fallsBackToBuiltInTemplateWhenNoCommandConfigured() throws Exception {
    ProjectBrief brief =
        new ProjectBrief(
            "Fallback",
            "Pitch",
            "Loop",
            "Desktop",
            List.of("Action"),
            List.of(),
            List.of(),
            Set.of("code"),
            Map.of(),
            1);

    CodeAgent agent = new CodeAgent();
    agent.generate(brief, tempDir, 1);

    Path artifact = tempDir.resolve("code/cycle-1-implementation.md");
    String content = Files.readString(artifact);
    assertTrue(content.contains("Cycle 1 Implementation Plan"));
    assertTrue(content.contains("Code Agent"));
  }
}
