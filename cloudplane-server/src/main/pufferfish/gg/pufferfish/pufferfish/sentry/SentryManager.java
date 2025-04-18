package gg.pufferfish.pufferfish.sentry;

import gg.pufferfish.pufferfish.PufferfishConfig;
import io.sentry.Sentry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SentryManager {

    private static final Logger LOGGER = LogManager.getLogger(SentryManager.class);

    private SentryManager() {
    }

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            initialized = true;

            Sentry.init(options -> {
                options.setDsn(PufferfishConfig.sentryDsn);
                options.setMaxBreadcrumbs(100);
            });

            PufferfishSentryAppender appender = new PufferfishSentryAppender();
            appender.start();
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);
            LOGGER.info("Sentry logging successfully started");
        } catch (Throwable throwable) {
            LOGGER.warn("Failed to initialize sentry", throwable);
            initialized = false;
        }
    }
}
