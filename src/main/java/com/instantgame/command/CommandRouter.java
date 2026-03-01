package com.instantgame.command;

import com.instantgame.VersionInfo;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Routes CLI arguments to concrete commands.
 */
public final class CommandRouter {

  private final InitCommand initCommand;
  private final GenerateCommand generateCommand;
  private final String cliVersion;

  /**
   * Creates a router with command handlers.
   *
   * @param initCommand handler for initialization requests
   * @param generateCommand handler for generation requests
   */
  public CommandRouter(InitCommand initCommand, GenerateCommand generateCommand) {
    this(initCommand, generateCommand, VersionInfo.currentVersion());
  }

  /**
   * Creates a router with command handlers and an explicit CLI version.
   *
   * @param initCommand handler for initialization requests
   * @param generateCommand handler for generation requests
   * @param cliVersion version label to print in help and version output
   */
  public CommandRouter(InitCommand initCommand, GenerateCommand generateCommand, String cliVersion) {
    this.initCommand = Objects.requireNonNull(initCommand, "initCommand");
    this.generateCommand = Objects.requireNonNull(generateCommand, "generateCommand");
    this.cliVersion = Objects.requireNonNull(cliVersion, "cliVersion").trim();
  }

  /**
   * Routes the user input to the corresponding command.
   *
   * @param args raw CLI arguments
   * @param workingDirectory directory from which command was executed
   * @param out stdout stream
   * @param err stderr stream
   * @return exit code where {@code 0} indicates success
   */
  public int route(String[] args, Path workingDirectory, PrintStream out, PrintStream err) {
    Objects.requireNonNull(args, "args");
    Objects.requireNonNull(workingDirectory, "workingDirectory");
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(err, "err");

    String command = args.length == 0 ? "init" : args[0].trim().toLowerCase(Locale.ROOT);

    return switch (command) {
      case "init" -> initCommand.execute(workingDirectory, out, err);
      case "generate" -> generateCommand.execute(workingDirectory, out, err);
      case "version", "--version", "-v" -> {
        printVersion(out);
        yield 0;
      }
      case "help", "-h", "--help" -> {
        printHelp(out);
        yield 0;
      }
      default -> {
        err.printf("Unknown command: %s%n", command);
        printHelp(err);
        yield 1;
      }
    };
  }

  /**
   * Prints command usage instructions.
   *
   * @param stream output destination
   */
  public void printHelp(PrintStream stream) {
    stream.printf("instantgame %s%n", cliVersion);
    stream.println("Usage:");
    stream.println("  instantgame             # create instantgame/GENERATE.md");
    stream.println("  instantgame init        # same as above");
    stream.println("  instantgame generate    # run read -> map -> implement -> test loop");
    stream.println("  instantgame version     # print CLI version");
  }

  /**
   * Prints the CLI version.
   *
   * @param stream output destination
   */
  public void printVersion(PrintStream stream) {
    stream.printf("instantgame %s%n", cliVersion);
  }
}
