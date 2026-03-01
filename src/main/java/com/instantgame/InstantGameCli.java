package com.instantgame;

import com.instantgame.command.CommandRouter;
import com.instantgame.command.GenerateCommand;
import com.instantgame.command.InitCommand;
import com.instantgame.service.BriefParser;
import com.instantgame.service.GenerateTemplateWriter;
import com.instantgame.service.PipelineRunner;
import com.instantgame.service.agent.ArtAgent;
import com.instantgame.service.agent.CodeAgent;
import com.instantgame.service.agent.MusicAgent;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Entrypoint for the InstantGame CLI.
 *
 * <p>This executable supports two modes:
 * <ul>
 *   <li>{@code instantgame} or {@code instantgame init} to create a PRD template.</li>
 *   <li>{@code instantgame generate} to run the read-map-implement-test loop.</li>
 * </ul>
 */
public final class InstantGameCli {

  private InstantGameCli() {
    // Utility class.
  }

  /**
   * Runs the CLI as a standalone process.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    int exitCode = run(args, System.out, System.err, Path.of("").toAbsolutePath());
    System.exit(exitCode);
  }

  /**
   * Executes the CLI with explicit IO streams and working directory.
   *
   * @param args command line arguments
   * @param out stdout stream
   * @param err stderr stream
   * @param workingDirectory current working directory
   * @return process exit code where {@code 0} means success
   */
  static int run(String[] args, PrintStream out, PrintStream err, Path workingDirectory) {
    Objects.requireNonNull(args, "args");
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(err, "err");
    Objects.requireNonNull(workingDirectory, "workingDirectory");

    CommandRouter router =
        new CommandRouter(
            new InitCommand(new GenerateTemplateWriter()),
            new GenerateCommand(
                new BriefParser(),
                new PipelineRunner(List.of(new CodeAgent(), new ArtAgent(), new MusicAgent()))));

    return router.route(args, workingDirectory, out, err);
  }
}
