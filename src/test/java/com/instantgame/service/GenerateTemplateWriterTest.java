package com.instantgame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link GenerateTemplateWriter}.
 */
class GenerateTemplateWriterTest {

  @TempDir Path tempDir;

  @Test
  void writesTemplateWhenMissing() throws IOException {
    GenerateTemplateWriter writer = new GenerateTemplateWriter();
    Path briefPath = tempDir.resolve("GENERATE.md");

    boolean created = writer.writeTemplateIfMissing(briefPath);

    assertTrue(created);
    assertTrue(Files.exists(briefPath));
    String content = Files.readString(briefPath);
    assertEquals(GenerateTemplateWriter.TEMPLATE, content);
    assertTrue(content.contains("## Rough Timeline"));
    assertTrue(content.contains("- Timeline: <start> - - - - - - - - - - - - - <end>"));
  }

  @Test
  void doesNotOverwriteExistingTemplate() throws IOException {
    GenerateTemplateWriter writer = new GenerateTemplateWriter();
    Path briefPath = tempDir.resolve("GENERATE.md");
    Files.writeString(briefPath, "custom");

    boolean created = writer.writeTemplateIfMissing(briefPath);

    assertFalse(created);
    assertEquals("custom", Files.readString(briefPath));
  }
}
