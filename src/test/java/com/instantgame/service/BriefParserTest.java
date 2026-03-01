package com.instantgame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.model.ProjectBrief;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link BriefParser}.
 */
class BriefParserTest {

  @TempDir Path tempDir;

  @Test
  void parsesCompletedBrief() throws IOException {
    Path briefPath = tempDir.resolve("GENERATE.md");
    Files.writeString(
        briefPath,
        """
        # InstantGame PRD

        ## Project Identity
        - Project Name: Orbit Runner
        - One-line Pitch: Dash through orbit lanes and survive waves.
        - Core Loop: Dodge asteroids, collect fuel, bank score, repeat.
        - Target Platforms: Web, Desktop

        ## Genre Checkboxes
        - [x] Action
        - [ ] Puzzle
        - [x] RPG
        - Custom Genre: Narrative Sandbox

        ## Art Style Checkboxes
        - [x] Pixel Art
        - [ ] Minimalist
        - Custom Art Style: Inked Collage

        ## Music Style Checkboxes
        - [x] Chiptune
        - [ ] Ambient
        - Custom Music Style: Dream Pop

        ## Agent Toggles
        - [x] Code Agent
        - [x] Music Agent
        - [ ] Art Agent

        ## AI Agent Commands (Optional)
        - Code Agent Command: printf 'code'
        - Art Agent Command: <fill-me>
        - Music Agent Command: printf 'music'

        ## Loop Settings
        - Iterations: 3

        ## Other Notes
        - Other Notes: Prioritize controller support.
        """);

    BriefParser parser = new BriefParser();
    ProjectBrief brief = parser.parse(briefPath);

    assertEquals("Orbit Runner", brief.projectName());
    assertEquals("Dash through orbit lanes and survive waves.", brief.oneLinePitch());
    assertEquals(3, brief.iterations());
    assertEquals(3, brief.genres().size());
    assertTrue(brief.genres().contains("Action"));
    assertTrue(brief.genres().contains("Narrative Sandbox"));
    assertEquals(2, brief.artStyles().size());
    assertTrue(brief.artStyles().contains("Pixel Art"));
    assertTrue(brief.artStyles().contains("Inked Collage"));
    assertTrue(brief.musicStyles().contains("Chiptune"));
    assertTrue(brief.musicStyles().contains("Dream Pop"));
    assertTrue(brief.enabledAgents().contains("code"));
    assertTrue(brief.enabledAgents().contains("music"));
    assertEquals("printf 'code'", brief.agentCommand("code").orElseThrow());
    assertTrue(brief.agentCommand("art").isEmpty());
    assertEquals("printf 'music'", brief.agentCommand("music").orElseThrow());
    assertEquals("Prioritize controller support.", brief.otherNotes());
  }

  @Test
  void defaultsAgentsWhenNoneSelected() throws IOException {
    Path briefPath = tempDir.resolve("GENERATE.md");
    Files.writeString(
        briefPath,
        """
        ## Project Identity
        - Project Name: Tiny Loop
        - One-line Pitch: A tiny test.
        - Core Loop: Click and score.
        - Target Platforms: Desktop

        ## Agent Toggles
        - [ ] Code Agent
        - [ ] Art Agent
        - [ ] Music Agent

        ## Loop Settings
        - Iterations: 1
        """);

    BriefParser parser = new BriefParser();
    ProjectBrief brief = parser.parse(briefPath);

    assertEquals(3, brief.enabledAgents().size());
    assertTrue(brief.enabledAgents().contains("code"));
    assertTrue(brief.enabledAgents().contains("art"));
    assertTrue(brief.enabledAgents().contains("music"));
  }

  @Test
  void rejectsMissingRequiredField() throws IOException {
    Path briefPath = tempDir.resolve("GENERATE.md");
    Files.writeString(
        briefPath,
        """
        ## Project Identity
        - Project Name: Missing Pitch
        - One-line Pitch: <fill-me>
        - Core Loop: Loop
        - Target Platforms: Desktop

        ## Loop Settings
        - Iterations: 1
        """);

    BriefParser parser = new BriefParser();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> parser.parse(briefPath));

    assertTrue(exception.getMessage().contains("One-line Pitch"));
  }
}
