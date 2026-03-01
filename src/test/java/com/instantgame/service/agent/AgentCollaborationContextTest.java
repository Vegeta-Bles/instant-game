package com.instantgame.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AgentCollaborationContext}.
 */
class AgentCollaborationContextTest {

  @Test
  void singlePassUsesExpectedDefaults() {
    AgentCollaborationContext context = AgentCollaborationContext.singlePass();

    assertEquals(1, context.round());
    assertEquals(1, context.totalRounds());
    assertEquals("extreme-99999", context.competenceProfile());
    assertTrue(context.sharedContextSummary().contains("No collaboration context"));
    assertTrue(context.peerArtifacts().isEmpty());
    assertFalse(context.collaborative());
  }

  @Test
  void marksContextCollaborativeWhenTotalRoundsIsGreaterThanOne() {
    AgentCollaborationContext context =
        new AgentCollaborationContext(
            2,
            3,
            "maximum-rigor-99999",
            "Round summary",
            Map.of("code", Path.of("implement/code/cycle-1-implementation.md")));

    assertTrue(context.collaborative());
    assertEquals(1, context.peerArtifacts().size());
  }

  @Test
  void rejectsInvalidRoundConfiguration() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AgentCollaborationContext(0, 1, "profile", "summary", Map.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new AgentCollaborationContext(1, 0, "profile", "summary", Map.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new AgentCollaborationContext(3, 2, "profile", "summary", Map.of()));
  }
}
