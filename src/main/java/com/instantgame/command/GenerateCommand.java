package com.instantgame.command;

import com.instantgame.model.ProjectBrief;
import com.instantgame.service.BriefParser;
import com.instantgame.service.PipelineReport;
import com.instantgame.service.PipelineRunner;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Runs the generation pipeline from a completed GENERATE.md brief.
 */
public final class GenerateCommand {

  private static final String PROJECT_DIRECTORY_NAME = "instantgame";
  private static final String BRIEF_FILENAME = "GENERATE.md";

  private final BriefParser briefParser;
  private final PipelineRunner pipelineRunner;

  /**
   * @param briefParser parser for the markdown brief
   * @param pipelineRunner generation pipeline implementation
   */
  public GenerateCommand(BriefParser briefParser, PipelineRunner pipelineRunner) {
    this.briefParser = Objects.requireNonNull(briefParser, "briefParser");
    this.pipelineRunner = Objects.requireNonNull(pipelineRunner, "pipelineRunner");
  }

  /**
   * Executes generation in the current working directory.
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

    if (!Files.exists(briefPath)) {
      err.printf("Missing %s. Run `instantgame` first to create the template.%n", briefPath);
      return 1;
    }

    try {
      ProjectBrief brief = briefParser.parse(briefPath);
      PipelineReport report = pipelineRunner.run(brief, projectDirectory, out);
      out.printf(
          "Generation finished: %d cycle(s) completed. Output root: %s%n",
          report.completedCycles(), report.outputRoot());
      return 0;
    } catch (IOException | IllegalArgumentException exception) {
      err.printf("Generation failed: %s%n", exception.getMessage());
      return 1;
    }
  }
}
