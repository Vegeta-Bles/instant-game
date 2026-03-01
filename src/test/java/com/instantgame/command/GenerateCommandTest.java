package com.instantgame.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.service.BriefParser;
import com.instantgame.service.GenerateTemplateWriter;
import com.instantgame.service.PipelineRunner;
import com.instantgame.service.agent.ArtAgent;
import com.instantgame.service.agent.CodeAgent;
import com.instantgame.service.agent.MusicAgent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link GenerateCommand}.
 */
class GenerateCommandTest {

  @TempDir Path tempDir;

  @Test
  void returnsErrorWhenBriefIsMissing() {
    GenerateCommand command =
        new GenerateCommand(
            new BriefParser(), new PipelineRunner(List.of(new CodeAgent(), new ArtAgent(), new MusicAgent())));

    ByteArrayOutputStream errors = new ByteArrayOutputStream();
    int exitCode = command.execute(tempDir, System.out, new PrintStream(errors));

    assertEquals(1, exitCode);
    assertTrue(errors.toString().contains("Run `instantgame` first"));
  }

  @Test
  void generatesArtifactsFromFilledBrief() throws Exception {
    Path projectDirectory = tempDir.resolve("instantgame");
    Files.createDirectories(projectDirectory);
    Path briefPath = projectDirectory.resolve("GENERATE.md");
    Files.writeString(
        briefPath,
        GenerateTemplateWriter.TEMPLATE
            .replace("<fill-me>", "filled")
            .replace("- Code Agent Command: filled", "- Code Agent Command: <fill-me>")
            .replace("- Art Agent Command: filled", "- Art Agent Command: <fill-me>")
            .replace("- Music Agent Command: filled", "- Music Agent Command: <fill-me>")
            .replace("- Iterations: 2", "- Iterations: 1")
            .replace("- Core Loop: filled", "- Core Loop: Explore, collect, survive")
            .replace("- Target Platforms: filled", "- Target Platforms: Web"));

    GenerateCommand command =
        new GenerateCommand(
            new BriefParser(), new PipelineRunner(List.of(new CodeAgent(), new ArtAgent(), new MusicAgent())));

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();
    int exitCode = command.execute(tempDir, new PrintStream(output), new PrintStream(errors));

    assertEquals(0, exitCode);
    assertEquals("", errors.toString());
    assertTrue(output.toString().contains("Generation finished"));
    assertTrue(
        Files.exists(
            projectDirectory.resolve("generated/filled/cycle-1/implement/code/cycle-1-implementation.md")));
  }
}
