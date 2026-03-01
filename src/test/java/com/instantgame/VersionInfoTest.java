package com.instantgame;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link VersionInfo}.
 */
class VersionInfoTest {

  @Test
  void returnsResolvedVersionString() {
    String version = VersionInfo.currentVersion();

    assertFalse(version.isBlank());
    assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(-[A-Za-z0-9.-]+)?"));
  }
}
