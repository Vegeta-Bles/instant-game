package com.instantgame.service.agent;

import com.instantgame.model.ProjectBrief;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Contract for a generation agent participating in the implement stage.
 */
public interface Agent {

  /**
   * @return stable key used in PRD agent toggles (for example {@code code})
   */
  String key();

  /**
   * Resolves the canonical artifact path for this agent in a cycle.
   *
   * @param implementDirectory base implement directory
   * @param cycle cycle number (1-based)
   * @return expected artifact path
   */
  Path artifactPath(Path implementDirectory, int cycle);

  /**
   * Generates artifacts for one iteration with collaboration context.
   *
   * @param brief parsed project brief
   * @param implementDirectory base directory for implementation outputs
   * @param cycle cycle number, starting at 1
   * @param context collaboration metadata for this round
   * @throws IOException when writing artifacts fails
   */
  void generate(ProjectBrief brief, Path implementDirectory, int cycle, AgentCollaborationContext context)
      throws IOException;

  /**
   * Generates artifacts for one iteration without collaborative refinement.
   *
   * @param brief parsed project brief
   * @param implementDirectory base directory for implementation outputs
   * @param cycle cycle number, starting at 1
   * @throws IOException when writing artifacts fails
   */
  default void generate(ProjectBrief brief, Path implementDirectory, int cycle) throws IOException {
    generate(brief, implementDirectory, cycle, AgentCollaborationContext.singlePass());
  }
}
