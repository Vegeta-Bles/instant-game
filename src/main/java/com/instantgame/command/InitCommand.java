package com.instantgame.command;

import com.instantgame.service.GenerateTemplateWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Creates the local InstantGame project folder and the PRD template file.
 */
public final class InitCommand {

  private static final String PROJECT_DIRECTORY_NAME = "instantgame";
  private static final String BRIEF_FILENAME = "GENERATE.md";

  private final GenerateTemplateWriter templateWriter;

  /**
   * @param templateWriter writer responsible for GENERATE.md content
   */
  public InitCommand(GenerateTemplateWriter templateWriter) {
    this.templateWriter = Objects.requireNonNull(templateWriter, "templateWriter");
  }

  /**
   * Executes project initialization.
   *
   * @param workingDirectory current working directory
   * @param out stdout stream
   * @param err stderr stream
   * @return exit code
   */
  public int execute(Path workingDirectory, PrintStream out, PrintStream err) {
    Objects.requireNonNull(workingDirectory, "workingDirectory");
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(err, "err");

    Path projectDirectory = workingDirectory.resolve(PROJECT_DIRECTORY_NAME);
    Path briefPath = projectDirectory.resolve(BRIEF_FILENAME);

    try {
      Files.createDirectories(projectDirectory);
      boolean created = templateWriter.writeTemplateIfMissing(briefPath);

      if (created) {
        out.printf("Created %s%n", briefPath);
      } else {
        out.printf("Reusing existing %s%n", briefPath);
      }

      return 0;
    } catch (IOException exception) {
      err.printf("Failed to initialize project: %s%n", exception.getMessage());
      return 1;
    }
  }
}
