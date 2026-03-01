package com.instantgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end tests for CLI routing through {@link InstantGameCli#run(String[], PrintStream, PrintStream, Path)}.
 */
class InstantGameCliTest {

  @TempDir Path tempDir;

  @Test
  void initCreatesTemplateViaCli() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();

    int exitCode =
        InstantGameCli.run(new String[] {"init"}, new PrintStream(output), new PrintStream(errors), tempDir);

    assertEquals(0, exitCode);
    assertTrue(Files.exists(tempDir.resolve("instantgame/GENERATE.md")));
    assertEquals("", errors.toString());
  }

  @Test
  void generateFailsWithoutTemplateViaCli() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();

    int exitCode =
        InstantGameCli.run(
            new String[] {"generate"}, new PrintStream(output), new PrintStream(errors), tempDir);

    assertEquals(1, exitCode);
    assertTrue(errors.toString().contains("Missing"));
  }

  @Test
  void versionFlagPrintsCurrentVersionViaCli() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();

    int exitCode =
        InstantGameCli.run(
            new String[] {"--version"}, new PrintStream(output), new PrintStream(errors), tempDir);

    assertEquals(0, exitCode);
    assertEquals("", errors.toString());
    assertTrue(output.toString().contains("instantgame " + VersionInfo.currentVersion()));
  }
}
