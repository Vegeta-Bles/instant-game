package com.instantgame.service;

import com.instantgame.model.ProjectBrief;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code GENERATE.md} markdown into a {@link ProjectBrief}.
 */
public final class BriefParser {

  private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^-\\s*([^:]+):\\s*(.*)$");
  private static final Pattern CHECKBOX_PATTERN = Pattern.compile("^-\\s*\\[([ xX])\\]\\s*(.+)$");

  /**
   * Parses the markdown brief from disk.
   *
   * @param briefPath path to GENERATE.md
   * @return parsed brief model
   * @throws IOException when reading fails
   */
  public ProjectBrief parse(Path briefPath) throws IOException {
    Objects.requireNonNull(briefPath, "briefPath");

    List<String> lines = Files.readAllLines(briefPath);

    String projectName = "";
    String oneLinePitch = "";
    String coreLoop = "";
    String targetPlatforms = "";
    String otherNotes = "";
    int iterations = 1;

    List<String> genres = new ArrayList<>();
    List<String> artStyles = new ArrayList<>();
    List<String> musicStyles = new ArrayList<>();
    List<String> mechanics = new ArrayList<>();
    Set<String> enabledAgents = new LinkedHashSet<>();
    Map<String, String> agentCommands = new LinkedHashMap<>();

    String currentSection = "";

    for (String rawLine : lines) {
      String line = rawLine.trim();

      if (line.startsWith("## ")) {
        currentSection = line.substring(3).trim().toLowerCase(Locale.ROOT);
        continue;
      }

      Matcher keyValueMatcher = KEY_VALUE_PATTERN.matcher(line);
      if (keyValueMatcher.matches()) {
        String key = keyValueMatcher.group(1).trim();
        String value = keyValueMatcher.group(2).trim();

        if (key.equalsIgnoreCase("Project Name")) {
          projectName = normalizeValue(value);
        } else if (key.equalsIgnoreCase("One-line Pitch")) {
          oneLinePitch = normalizeValue(value);
        } else if (key.equalsIgnoreCase("Core Loop")) {
          coreLoop = normalizeValue(value);
        } else if (key.equalsIgnoreCase("Target Platforms")) {
          targetPlatforms = normalizeValue(value);
        } else if (key.equalsIgnoreCase("Other Notes") || key.equalsIgnoreCase("Notes")) {
          otherNotes = normalizeValue(value);
        } else if (key.equalsIgnoreCase("Iterations")) {
          iterations = parseIterations(value);
        } else if (key.toLowerCase(Locale.ROOT).endsWith("agent command")) {
          String agentKey = normalizeAgentLabel(key.replaceAll("(?i)\\s*command$", ""));
          String command = normalizeValue(value);
          if (!command.isBlank()) {
            agentCommands.put(agentKey, command);
          }
        } else if (currentSection.contains("genre") && key.toLowerCase(Locale.ROOT).startsWith("custom genre")) {
          String customGenre = normalizeValue(value);
          if (!customGenre.isBlank()) {
            genres.add(customGenre);
          }
        } else if (currentSection.contains("art style")
            && key.toLowerCase(Locale.ROOT).startsWith("custom art style")) {
          String customArtStyle = normalizeValue(value);
          if (!customArtStyle.isBlank()) {
            artStyles.add(customArtStyle);
          }
        } else if (currentSection.contains("music style")
            && key.toLowerCase(Locale.ROOT).startsWith("custom music style")) {
          String customMusicStyle = normalizeValue(value);
          if (!customMusicStyle.isBlank()) {
            musicStyles.add(customMusicStyle);
          }
        } else if (currentSection.contains("mechanic")
            && key.toLowerCase(Locale.ROOT).startsWith("custom mechanic")) {
          String customMechanic = normalizeValue(value);
          if (!customMechanic.isBlank()) {
            mechanics.add(customMechanic);
          }
        }
      }

      Matcher checkboxMatcher = CHECKBOX_PATTERN.matcher(line);
      if (checkboxMatcher.matches()) {
        boolean selected = checkboxMatcher.group(1).equalsIgnoreCase("x");
        String label = checkboxMatcher.group(2).trim();

        if (!selected) {
          continue;
        }

        if (currentSection.contains("genre")) {
          genres.add(label);
        } else if (currentSection.contains("art style")) {
          artStyles.add(label);
        } else if (currentSection.contains("music style")) {
          musicStyles.add(label);
        } else if (currentSection.contains("mechanic")) {
          mechanics.add(label);
        } else if (currentSection.contains("agent")) {
          enabledAgents.add(normalizeAgentLabel(label));
        }
      }
    }

    if (enabledAgents.isEmpty()) {
      enabledAgents.add("code");
      enabledAgents.add("art");
      enabledAgents.add("music");
    }

    return new ProjectBrief(
        requireField(projectName, "Project Name"),
        requireField(oneLinePitch, "One-line Pitch"),
        requireField(coreLoop, "Core Loop"),
        requireField(targetPlatforms, "Target Platforms"),
        genres,
        artStyles,
        musicStyles,
        mechanics,
        enabledAgents,
        agentCommands,
        iterations,
        otherNotes);
  }

  private static int parseIterations(String value) {
    String normalized = normalizeValue(value);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Iterations cannot be empty.");
    }

    try {
      int parsed = Integer.parseInt(normalized);
      if (parsed < 1) {
        throw new IllegalArgumentException("Iterations must be at least 1.");
      }
      return parsed;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Iterations must be a whole number.", exception);
    }
  }

  private static String normalizeValue(String value) {
    String trimmed = Objects.requireNonNull(value, "value").trim();
    if (trimmed.equalsIgnoreCase("<fill-me>")) {
      return "";
    }
    return trimmed;
  }

  private static String requireField(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " is required in GENERATE.md.");
    }
    return value;
  }

  private static String normalizeAgentLabel(String label) {
    String lower = label.toLowerCase(Locale.ROOT);
    if (lower.contains("code")) {
      return "code";
    }
    if (lower.contains("art")) {
      return "art";
    }
    if (lower.contains("music") || lower.contains("audio")) {
      return "music";
    }
    return lower.replaceAll("\\s+agent", "").trim();
  }
}
