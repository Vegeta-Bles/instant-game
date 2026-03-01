package com.instantgame.service;

import java.nio.file.Path;
import java.util.List;

/**
 * Summary emitted at the end of a generation run.
 *
 * @param completedCycles number of cycles that completed successfully
 * @param outputRoot generation root directory
 * @param executedStages ordered list of stages executed
 */
public record PipelineReport(int completedCycles, Path outputRoot, List<String> executedStages) {}
