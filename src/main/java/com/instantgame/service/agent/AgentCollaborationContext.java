package com.instantgame.service.agent;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Collaboration metadata provided to agents during the implement stage.
 *
 * <p>The pipeline runs one or more rounds. In later rounds, agents receive a shared summary and
 * peer artifact references so they can critique and refine outputs collaboratively.
 */
public final class AgentCollaborationContext {

  private final int round;
  private final int totalRounds;
  private final String competenceProfile;
  private final String sharedContextSummary;
  private final Map<String, Path> peerArtifacts;

  /**
   * Creates a collaboration context.
   *
   * @param round current round, starting at 1
   * @param totalRounds total number of rounds
   * @param competenceProfile quality profile that should guide depth/rigor
   * @param sharedContextSummary synthesized context from prior round artifacts
   * @param peerArtifacts map of agent key to artifact path
   */
  public AgentCollaborationContext(
      int round,
      int totalRounds,
      String competenceProfile,
      String sharedContextSummary,
      Map<String, Path> peerArtifacts) {
    this.round = round;
    this.totalRounds = totalRounds;
    this.competenceProfile = Objects.requireNonNull(competenceProfile, "competenceProfile").trim();
    this.sharedContextSummary =
        Objects.requireNonNull(sharedContextSummary, "sharedContextSummary").trim();
    this.peerArtifacts = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(peerArtifacts, "peerArtifacts")));

    if (round < 1) {
      throw new IllegalArgumentException("Round must be at least 1.");
    }
    if (totalRounds < 1) {
      throw new IllegalArgumentException("Total rounds must be at least 1.");
    }
    if (round > totalRounds) {
      throw new IllegalArgumentException("Round cannot exceed total rounds.");
    }
  }

  /**
   * Creates a non-collaborative single-pass context.
   *
   * @return default context for one-pass generation
   */
  public static AgentCollaborationContext singlePass() {
    return new AgentCollaborationContext(
        1, 1, "extreme-99999", "No collaboration context provided yet.", Map.of());
  }

  /** @return current round */
  public int round() {
    return round;
  }

  /** @return total rounds */
  public int totalRounds() {
    return totalRounds;
  }

  /** @return requested competence profile */
  public String competenceProfile() {
    return competenceProfile;
  }

  /** @return shared synthesis from prior artifacts */
  public String sharedContextSummary() {
    return sharedContextSummary;
  }

  /** @return peer artifact paths keyed by agent */
  public Map<String, Path> peerArtifacts() {
    return peerArtifacts;
  }

  /**
   * Indicates whether this run uses multi-round collaboration.
   *
   * @return true when total rounds is greater than one
   */
  public boolean collaborative() {
    return totalRounds > 1;
  }
}
