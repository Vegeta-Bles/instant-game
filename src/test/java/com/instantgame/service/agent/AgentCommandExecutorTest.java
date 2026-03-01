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
  void passesCollaborationEnvironmentVariablesToCommand() throws Exception {
    ProjectBrief brief =
        new ProjectBrief(
            "Collab Prompt Game",
            "Pitch",
            "Loop",
            "Desktop",
            List.of(),
            List.of(),
            List.of(),
            Set.of("code"),
            Map.of(
                "code",
                "printf 'ROUND=%s\\nTOTAL=%s\\nENABLED=%s\\nPROFILE=%s\\nSHARED=%s\\nPEERS=%s\\n' "
                    + "\"$INSTANTGAME_COLLAB_ROUND\" \"$INSTANTGAME_COLLAB_TOTAL_ROUNDS\" "
                    + "\"$INSTANTGAME_COLLAB_ENABLED\" \"$INSTANTGAME_COMPETENCE_PROFILE\" "
                    + "\"$INSTANTGAME_SHARED_CONTEXT\" \"$INSTANTGAME_PEER_ARTIFACTS\""),
            1);

    AgentCommandExecutor executor = new AgentCommandExecutor(Duration.ofSeconds(5));
    Path output = tempDir.resolve("code.md");
    Path codeArtifact = tempDir.resolve("implement/code/cycle-1-implementation.md");
    Path artArtifact = tempDir.resolve("implement/art/cycle-1-art-direction.md");
    AgentCollaborationContext context =
        new AgentCollaborationContext(
            2,
            3,
            "maximum-rigor-99999",
            "Cross-review findings",
            Map.of("code", codeArtifact, "art", artArtifact));

    boolean executed = executor.executeConfiguredCommand(brief, "code", "PROMPT", output, 1, context);

    assertTrue(executed);
    String content = Files.readString(output);
    assertTrue(content.contains("ROUND=2"));
    assertTrue(content.contains("TOTAL=3"));
    assertTrue(content.contains("ENABLED=true"));
    assertTrue(content.contains("PROFILE=maximum-rigor-99999"));
    assertTrue(content.contains("SHARED=Cross-review findings"));
    assertTrue(content.contains("PEERS="));
    assertTrue(content.contains("code=" + codeArtifact.toAbsolutePath()));
    assertTrue(content.contains("art=" + artArtifact.toAbsolutePath()));
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
