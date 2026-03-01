package com.instantgame.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.instantgame.service.BriefParser;
import com.instantgame.service.GenerateTemplateWriter;
import com.instantgame.service.PipelineRunner;
import com.instantgame.service.agent.ArtAgent;
import com.instantgame.service.agent.CodeAgent;
import com.instantgame.service.agent.MusicAgent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link CommandRouter}.
 */
class CommandRouterTest {

  @TempDir Path tempDir;

  @Test
  void defaultsToInitWhenNoArgs() {
    CommandRouter router =
        new CommandRouter(
            new InitCommand(new GenerateTemplateWriter()),
            new GenerateCommand(
                new BriefParser(),
                new PipelineRunner(List.of(new CodeAgent(), new ArtAgent(), new MusicAgent()))));

    int exitCode = router.route(new String[] {}, tempDir, System.out, System.err);

    assertEquals(0, exitCode);
  }

  @Test
  void printsHelpForUnknownCommand() {
    CommandRouter router =
        new CommandRouter(
            new InitCommand(new GenerateTemplateWriter()),
            new GenerateCommand(
                new BriefParser(),
                new PipelineRunner(List.of(new CodeAgent(), new ArtAgent(), new MusicAgent()))));

    ByteArrayOutputStream errors = new ByteArrayOutputStream();

    int exitCode =
        router.route(new String[] {"wat"}, tempDir, System.out, new PrintStream(errors));

    assertEquals(1, exitCode);
    assertTrue(errors.toString().contains("Unknown command"));
  }
}
