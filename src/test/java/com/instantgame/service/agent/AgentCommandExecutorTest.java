package com.instantgame.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.model.ProjectBrief;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link AgentCommandExecutor}.
 */
class AgentCommandExecutorTest {

  @TempDir Path tempDir;

  @Test
  void executesConfiguredCommandAndCapturesStdout() throws Exception {
    ProjectBrief brief =
        new ProjectBrief(
            "Prompt Game",
            "Pitch",
            "Loop",
            "Desktop",
            List.of(),
            List.of(),
            List.of(),
            Set.of("code"),
            Map.of("code", "printf '%s' \"$INSTANTGAME_PROMPT\""),
            1);

    AgentCommandExecutor executor = new AgentCommandExecutor(Duration.ofSeconds(5));
    Path output = tempDir.resolve("code.md");

    boolean executed = executor.executeConfiguredCommand(brief, "code", "PROMPT-123", output, 1);

    assertTrue(executed);
    assertEquals("PROMPT-123", Files.readString(output));
  }

  @Test
  void supportsCommandsThatWriteDirectlyToOutputPath() throws Exception {
    ProjectBrief brief =
        new ProjectBrief(
            "Direct Write",
            "Pitch",
            "Loop",
            "Desktop",
            List.of(),
            List.of(),
            List.of(),
            Set.of("art"),
            Map.of("art", "printf 'ARTIFACT' > \"$INSTANTGAME_OUTPUT_PATH\""),
            1);

    AgentCommandExecutor executor = new AgentCommandExecutor(Duration.ofSeconds(5));
    Path output = tempDir.resolve("art.md");

    boolean executed = executor.executeConfiguredCommand(brief, "art", "unused", output, 2);

    assertTrue(executed);
    assertEquals("ARTIFACT", Files.readString(output));
  }

  @Test
  void returnsFalseWhenNoCommandConfigured() throws Exception {
    ProjectBrief brief =
        new ProjectBrief(
            "No Command",
            "Pitch",
            "Loop",
            "Desktop",
            List.of(),
            List.of(),
            List.of(),
            Set.of("music"),
            Map.of(),
            1);

    AgentCommandExecutor executor = new AgentCommandExecutor(Duration.ofSeconds(5));
    Path output = tempDir.resolve("music.md");

    boolean executed = executor.executeConfiguredCommand(brief, "music", "prompt", output, 1);

    assertFalse(executed);
    assertFalse(Files.exists(output));
  }

  @Test
  void throwsWhenCommandFails() {
    ProjectBrief brief =
        new ProjectBrief(
            "Failing Command",
            "Pitch",
            "Loop",
            "Desktop",
            List.of(),
            List.of(),
            List.of(),
            Set.of("code"),
            Map.of("code", "echo boom >&2; exit 9"),
            1);

    AgentCommandExecutor executor = new AgentCommandExecutor(Duration.ofSeconds(5));
    Path output = tempDir.resolve("code.md");

    IOException exception =
        assertThrows(
            IOException.class,
            () -> executor.executeConfiguredCommand(brief, "code", "prompt", output, 1));

    assertTrue(exception.getMessage().contains("exit code 9"));
  }
}
