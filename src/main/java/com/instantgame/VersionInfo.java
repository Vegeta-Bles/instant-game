package com.instantgame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Resolves the CLI version embedded at build time.
 */
public final class VersionInfo {

  private static final String VERSION_RESOURCE = "/instantgame-version.properties";
  private static final String VERSION_KEY = "version";
  private static final String DEFAULT_VERSION = "0.0.0-dev";
  private static final String VERSION = loadVersion();

  private VersionInfo() {
    // Utility class.
  }

  /**
   * Returns the current CLI version string.
   *
   * @return semantic version of this build
   */
  public static String currentVersion() {
    return VERSION;
  }

  private static String loadVersion() {
    try (InputStream inputStream = VersionInfo.class.getResourceAsStream(VERSION_RESOURCE)) {
      if (inputStream == null) {
        return DEFAULT_VERSION;
      }

      Properties properties = new Properties();
      properties.load(inputStream);
      String configured = Objects.toString(properties.getProperty(VERSION_KEY), "").trim();

      if (!configured.isEmpty() && !configured.contains("${")) {
        return configured;
      }
      return DEFAULT_VERSION;
    } catch (IOException exception) {
      return DEFAULT_VERSION;
    }
  }
}
