package com.instantgame.service.agent;

import com.instantgame.model.ProjectBrief;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Executes external commands configured per agent in {@code GENERATE.md}.
 */
public final class AgentCommandExecutor {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

  private final Duration timeout;

  /** Creates an executor with a five-minute timeout. */
  public AgentCommandExecutor() {
    this(DEFAULT_TIMEOUT);
  }

  /**
   * @param timeout execution timeout for each agent process
   */
  public AgentCommandExecutor(Duration timeout) {
    this.timeout = Objects.requireNonNull(timeout, "timeout");
  }

  /**
   * Runs the configured command for an agent when present.
   *
   * <p>The command receives environment variables including:
   * <ul>
   *   <li>{@code INSTANTGAME_PROMPT}</li>
   *   <li>{@code INSTANTGAME_AGENT}</li>
   *   <li>{@code INSTANTGAME_PROJECT_NAME}</li>
   *   <li>{@code INSTANTGAME_CYCLE}</li>
   *   <li>{@code INSTANTGAME_OUTPUT_PATH}</li>
   * </ul>
   *
   * @param brief parsed project brief
   * @param agentKey stable agent key
   * @param prompt agent prompt
   * @param artifactPath destination file for command stdout
   * @param cycle generation cycle number
   * @return true if a command was configured and executed; false otherwise
   * @throws IOException when execution fails
   */
  public boolean executeConfiguredCommand(
      ProjectBrief brief, String agentKey, String prompt, Path artifactPath, int cycle) throws IOException {
    return executeConfiguredCommand(
        brief, agentKey, prompt, artifactPath, cycle, AgentCollaborationContext.singlePass());
  }

  /**
   * Runs the configured command for an agent when present with collaboration metadata.
   *
   * @param brief parsed project brief
   * @param agentKey stable agent key
   * @param prompt agent prompt
   * @param artifactPath destination file for command stdout
   * @param cycle generation cycle number
   * @param context collaboration metadata for this round
   * @return true if a command was configured and executed; false otherwise
   * @throws IOException when execution fails
   */
  public boolean executeConfiguredCommand(
      ProjectBrief brief,
      String agentKey,
      String prompt,
      Path artifactPath,
      int cycle,
      AgentCollaborationContext context)
      throws IOException {
    Objects.requireNonNull(brief, "brief");
    Objects.requireNonNull(agentKey, "agentKey");
    Objects.requireNonNull(prompt, "prompt");
    Objects.requireNonNull(artifactPath, "artifactPath");
    Objects.requireNonNull(context, "context");

    String command = brief.agentCommand(agentKey).orElse("");
    if (command.isBlank()) {
      return false;
    }

    Files.createDirectories(artifactPath.getParent());

    ProcessBuilder processBuilder = new ProcessBuilder("/bin/zsh", "-lc", command);
    Map<String, String> environment = processBuilder.environment();
    environment.put("INSTANTGAME_PROMPT", prompt);
    environment.put("INSTANTGAME_AGENT", agentKey);
    environment.put("INSTANTGAME_PROJECT_NAME", brief.projectName());
    environment.put("INSTANTGAME_CYCLE", String.valueOf(cycle));
    environment.put("INSTANTGAME_OUTPUT_PATH", artifactPath.toAbsolutePath().toString());
    environment.put("INSTANTGAME_COLLAB_ROUND", String.valueOf(context.round()));
    environment.put("INSTANTGAME_COLLAB_TOTAL_ROUNDS", String.valueOf(context.totalRounds()));
    environment.put("INSTANTGAME_COLLAB_ENABLED", String.valueOf(context.collaborative()));
    environment.put("INSTANTGAME_COMPETENCE_PROFILE", context.competenceProfile());
    environment.put("INSTANTGAME_SHARED_CONTEXT", context.sharedContextSummary());
    environment.put("INSTANTGAME_PEER_ARTIFACTS", toPeerArtifactString(context.peerArtifacts()));

    Process process = processBuilder.start();

    String stdout;
    String stderr;

    try {
      boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!finished) {
        process.destroyForcibly();
        throw new IOException(
            "Agent command timed out for '%s' after %d seconds."
                .formatted(agentKey, timeout.toSeconds()));
      }

      stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IOException("Agent command interrupted for '%s'.".formatted(agentKey), exception);
    }

    if (process.exitValue() != 0) {
      String stderrSuffix = stderr.isBlank() ? "" : " stderr: " + stderr.strip();
      throw new IOException(
          "Agent command failed for '%s' with exit code %d.%s"
              .formatted(agentKey, process.exitValue(), stderrSuffix));
    }

    if (!stdout.isBlank()) {
      Files.writeString(
          artifactPath,
          stdout,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
      return true;
    }

    if (Files.exists(artifactPath) && Files.size(artifactPath) > 0L) {
      return true;
    }

    throw new IOException(
        "Agent command for '%s' produced no stdout and did not write %s"
            .formatted(agentKey, artifactPath.getFileName()));
  }

  private static String toPeerArtifactString(Map<String, Path> peerArtifacts) {
    return peerArtifacts.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue().toAbsolutePath())
        .reduce((left, right) -> left + ";" + right)
        .orElse("");
  }
}
