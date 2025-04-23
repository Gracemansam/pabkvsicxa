package com.lamiplus_common_api.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoggingManager {
    private static final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    private static final Map<String, Logger> pluginLoggers = new HashMap<>();
    private static final Path LOG_DIR = Paths.get("logs");
    private static final String DEFAULT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";
    private static final String CORE_LOG_FILE = "application.log";
    private static final String PLUGINS_LOG_FILE = "plugins.log";
    private static final String ERROR_LOG_FILE = "errors.log";

    static {
        configureLogging();
    }

    private static void configureLogging() {

        if (!LOG_DIR.toFile().exists()) {
            LOG_DIR.toFile().mkdirs();
        }


        ConsoleAppender<ILoggingEvent> consoleAppender = configureConsoleAppender();


        configureFileAppender(CORE_LOG_FILE, "coreapplication", Level.INFO, consoleAppender);

        configureFileAppender(PLUGINS_LOG_FILE, "plugin", Level.INFO, null);


        configureErrorLogger();
    }

    private static ConsoleAppender<ILoggingEvent> configureConsoleAppender() {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("consoleAppender");

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(DEFAULT_PATTERN);
        encoder.start();

        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        return consoleAppender;
    }

    private static void configureFileAppender(String filename, String loggerName, Level level,
                                              ConsoleAppender<ILoggingEvent> consoleAppender) {
        Logger logger = context.getLogger(loggerName);
        logger.setLevel(level);
        logger.setAdditive(false);

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName(loggerName + "FileAppender");
        fileAppender.setFile(LOG_DIR.resolve(filename).toString());

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(fileAppender);
        policy.setFileNamePattern(LOG_DIR.resolve(filename + ".%d{yyyy-MM-dd}.%i").toString());
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        policy.setMaxHistory(14);
        policy.setTotalSizeCap(FileSize.valueOf("1GB"));
        policy.start();

        fileAppender.setRollingPolicy(policy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(DEFAULT_PATTERN);
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        logger.addAppender(fileAppender);


        if (consoleAppender != null) {
            logger.addAppender(consoleAppender);
        }
    }

    private static void configureErrorLogger() {
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        RollingFileAppender<ILoggingEvent> errorAppender = new RollingFileAppender<>();
        errorAppender.setContext(context);
        errorAppender.setName("errorFileAppender");
        errorAppender.setFile(LOG_DIR.resolve(ERROR_LOG_FILE).toString());

        // Rolling policy for errors
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(errorAppender);
        policy.setFileNamePattern(LOG_DIR.resolve(ERROR_LOG_FILE + ".%d{yyyy-MM-dd}.%i").toString());
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        policy.setMaxHistory(30); // Keep error logs longer
        policy.setTotalSizeCap(FileSize.valueOf("2GB"));
        policy.start();

        errorAppender.setRollingPolicy(policy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(DEFAULT_PATTERN);
        encoder.start();

        errorAppender.setEncoder(encoder);
        errorAppender.start();

        // Add a filter to only log errors
        ch.qos.logback.classic.filter.ThresholdFilter filter = new ch.qos.logback.classic.filter.ThresholdFilter();
        filter.setLevel(Level.ERROR.toString());
        filter.start();
        errorAppender.addFilter(filter);

        rootLogger.addAppender(errorAppender);

        // Add console logging for errors as well
        ConsoleAppender<ILoggingEvent> errorConsoleAppender = new ConsoleAppender<>();
        errorConsoleAppender.setContext(context);
        errorConsoleAppender.setName("errorConsoleAppender");

        PatternLayoutEncoder errorConsoleEncoder = new PatternLayoutEncoder();
        errorConsoleEncoder.setContext(context);
        errorConsoleEncoder.setPattern(DEFAULT_PATTERN);
        errorConsoleEncoder.start();

        errorConsoleAppender.setEncoder(errorConsoleEncoder);

        // Add the same error filter to console output
        ch.qos.logback.classic.filter.ThresholdFilter consoleFilter = new ch.qos.logback.classic.filter.ThresholdFilter();
        consoleFilter.setLevel(Level.ERROR.toString());
        consoleFilter.start();
        errorConsoleAppender.addFilter(consoleFilter);

        errorConsoleAppender.start();
        rootLogger.addAppender(errorConsoleAppender);
    }


    public static org.slf4j.Logger getPluginLogger(String pluginId) {
        if (!pluginLoggers.containsKey(pluginId)) {
            // Create a specific logger for this plugin
            Logger pluginLogger = context.getLogger("plugin." + pluginId);
            pluginLogger.setLevel(Level.INFO);

            // Ensure it inherits from the plugin parent logger
            pluginLogger.setAdditive(true);

            pluginLoggers.put(pluginId, pluginLogger);
        }

        return pluginLoggers.get(pluginId);
    }


    public static void setPluginLogLevel(String pluginId, Level level) {
        Logger logger = (Logger) getPluginLogger(pluginId);
        logger.setLevel(level);
    }

    /**
     * Reset logger configuration
     */
    public static void resetConfiguration() {
        context.reset();
        configureLogging();
    }
}