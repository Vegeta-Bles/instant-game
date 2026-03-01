package com.instantgame.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.service.GenerateTemplateWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link InitCommand}.
 */
class InitCommandTest {

  @TempDir Path tempDir;

  @Test
  void createsProjectDirectoryAndTemplate() throws Exception {
    InitCommand command = new InitCommand(new GenerateTemplateWriter());
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();

    int exitCode = command.execute(tempDir, new PrintStream(output), new PrintStream(errors));

    assertEquals(0, exitCode);
    Path briefPath = tempDir.resolve("instantgame/GENERATE.md");
    assertTrue(Files.exists(briefPath));
    assertTrue(Files.readString(briefPath).contains("# InstantGame PRD"));
    assertEquals("", errors.toString());
  }

  @Test
  void isIdempotentWhenTemplateAlreadyExists() {
    InitCommand command = new InitCommand(new GenerateTemplateWriter());

    int first = command.execute(tempDir, System.out, System.err);
    int second = command.execute(tempDir, System.out, System.err);

    assertEquals(0, first);
    assertEquals(0, second);
  }
}
