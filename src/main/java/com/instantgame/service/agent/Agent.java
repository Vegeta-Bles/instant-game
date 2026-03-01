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
   * Generates artifacts for one iteration.
   *
   * @param brief parsed project brief
   * @param implementDirectory base directory for implementation outputs
   * @param cycle cycle number, starting at 1
   * @throws IOException when writing artifacts fails
   */
  void generate(ProjectBrief brief, Path implementDirectory, int cycle) throws IOException;
}
