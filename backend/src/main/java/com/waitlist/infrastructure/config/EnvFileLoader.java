package com.waitlist.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EnvFileLoader implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EnvFileLoader.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // Try to find env file in multiple locations
            // First, try in the current working directory
            String currentDir = System.getProperty("user.dir");
            String[] possiblePaths = {
                    currentDir + "/backend/env",
                    currentDir + "/env",
                    "./backend/env",
                    "./env",
                    "backend/env",
                    "env"
            };

            Dotenv dotenv = null;
            String loadedPath = null;

            for (String path : possiblePaths) {
                File envFile = new File(path);
                if (envFile.exists() && envFile.isFile()) {
                    try {
                        dotenv = Dotenv.configure()
                                .directory(envFile.getParent() != null ? envFile.getParent() : ".")
                                .filename(envFile.getName())
                                .load();
                        loadedPath = envFile.getAbsolutePath();
                        break;
                    } catch (Exception e) {
                        logger.debug("Failed to load env file from {}: {}", path, e.getMessage());
                    }
                }
            }

            // If not found in relative paths, try default location
            if (dotenv == null) {
                try {
                    dotenv = Dotenv.configure()
                            .filename("env")
                            .ignoreIfMissing()
                            .load();
                } catch (Exception e) {
                    logger.debug("Failed to load env file from default location: {}", e.getMessage());
                }
            }

            if (dotenv != null) {
                Map<String, Object> envMap = new HashMap<>();
                dotenv.entries().forEach(entry -> {
                    envMap.put(entry.getKey(), entry.getValue());
                });

                if (!envMap.isEmpty()) {
                    environment.getPropertySources().addFirst(
                            new MapPropertySource("envFile", envMap));
                    logger.info("Loaded {} environment variables from env file{}",
                            envMap.size(),
                            loadedPath != null ? " at " + loadedPath : "");
                } else {
                    logger.debug("No environment variables found in env file");
                }
            } else {
                logger.debug("Env file not found, skipping environment variable loading");
            }
        } catch (Exception e) {
            logger.warn("Could not load env file: {}", e.getMessage());
        }
    }
}
