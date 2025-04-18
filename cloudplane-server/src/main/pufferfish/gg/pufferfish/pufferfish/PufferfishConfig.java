package gg.pufferfish.pufferfish;

import gg.pufferfish.pufferfish.flare.FlareCommand;
import gg.pufferfish.pufferfish.simd.SIMDDetection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class PufferfishConfig {

    private static final YamlConfiguration config = new YamlConfiguration();
    private static int updates = 0;

    public static ConfigurationSection getConfigCopy() {
        return config;
    }

    public static int getUpdates() {
        return updates;
    }

    public static void load(File configFile) throws IOException { // CloudPlane - configurable pufferfish config location
        // File configFile = new File("pufferfish.yml"); // CloudPlane - configurable pufferfish config location

        if (configFile.exists()) {
            try {
                config.load(configFile);
            } catch (InvalidConfigurationException e) {
                throw new IOException(e);
            }
        }

        getString("info.version", "1.0");
        setComment("info",
                "Pufferfish Configuration",
                "Check out Pufferfish Host for maximum performance server hosting: https://pufferfish.host",
                "Join our Discord for support: https://discord.gg/reZw4vQV9H",
                "Download new builds at https://ci.pufferfish.host/job/Pufferfish");

        for (Method method : PufferfishConfig.class.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPrivate(method.getModifiers()) && method.getParameterCount() == 0 &&
                    method.getReturnType() == Void.TYPE && !method.getName().startsWith("lambda")) {
                method.setAccessible(true);
                try {
                    method.invoke(null);
                } catch (Throwable t) {
                    MinecraftServer.LOGGER.warn("Failed to load configuration option from " + method.getName(), t);
                }
            }
        }

        updates++;

        config.save(configFile);

        // Attempt to detect vectorization
        try {
            SIMDDetection.isEnabled = SIMDDetection.canEnable(PufferfishLogger.LOGGER);
            SIMDDetection.versionLimited = SIMDDetection.getJavaVersion() != 17 && SIMDDetection.getJavaVersion() != 21;
        } catch (NoClassDefFoundError | Exception ignored) {}

        if (!SIMDDetection.isEnabled) {
            if (SIMDDetection.versionLimited) {
                PufferfishLogger.LOGGER.warning("Will not enable SIMD! These optimizations are only safely supported on Java 17 and Java 21.");
            } else {
                PufferfishLogger.LOGGER.warning("SIMD operations are available for your server, but are not configured!");
                PufferfishLogger.LOGGER.warning("To enable additional optimizations, add \"--add-modules=jdk.incubator.vector\" to your startup flags, BEFORE the \"-jar\".");
                PufferfishLogger.LOGGER.warning("If you have already added this flag, then SIMD operations are not supported on your JVM or CPU.");
                PufferfishLogger.LOGGER.warning("Debug: Java: " + System.getProperty("java.version") + ", test run: " + SIMDDetection.testRun);
            }
        }
    }

    private static void setComment(String key, String... comment) {
        if (config.contains(key)) {
            config.setComments(key, List.of(comment));
        }
    }

    private static void ensureDefault(String key, Object defaultValue, String... comment) {
        if (!config.contains(key)) {
            config.set(key, defaultValue);
            config.setComments(key, List.of(comment));
        }
    }

    private static boolean getBoolean(String key, boolean defaultValue, String... comment) {
        return getBoolean(key, null, defaultValue, comment);
    }

    private static boolean getBoolean(String key, @Nullable String oldKey, boolean defaultValue, String... comment) {
        ensureDefault(key, defaultValue, comment);
        return config.getBoolean(key, defaultValue);
    }

    private static int getInt(String key, int defaultValue, String... comment) {
        return getInt(key, null, defaultValue, comment);
    }

    private static int getInt(String key, @Nullable String oldKey, int defaultValue, String... comment) {
        ensureDefault(key, defaultValue, comment);
        return config.getInt(key, defaultValue);
    }

    private static double getDouble(String key, double defaultValue, String... comment) {
        return getDouble(key, null, defaultValue, comment);
    }

    private static double getDouble(String key, @Nullable String oldKey, double defaultValue, String... comment) {
        ensureDefault(key, defaultValue, comment);
        return config.getDouble(key, defaultValue);
    }

    private static String getString(String key, String defaultValue, String... comment) {
        return getOldString(key, null, defaultValue, comment);
    }

    private static String getOldString(String key, @Nullable String oldKey, String defaultValue, String... comment) {
        ensureDefault(key, defaultValue, comment);
        return config.getString(key, defaultValue);
    }

    private static List<String> getStringList(String key, List<String> defaultValue, String... comment) {
        return getStringList(key, null, defaultValue, comment);
    }

    private static List<String> getStringList(String key, @Nullable String oldKey, List<String> defaultValue, String... comment) {
        ensureDefault(key, defaultValue, comment);
        return config.getStringList(key);
    }

    public static String sentryDsn;
    private static void sentry() {
        String sentryEnvironment = System.getenv("SENTRY_DSN");
        String sentryConfig = getString("sentry-dsn", "", "Sentry DSN for improved error logging, leave blank to disable", "Obtain from https://sentry.io/");

        sentryDsn = sentryEnvironment == null ? sentryConfig : sentryEnvironment;
        if (sentryDsn != null && !sentryDsn.isBlank()) {
            gg.pufferfish.pufferfish.sentry.SentryManager.init();
        }
    }

    public static boolean enableBooks;
    private static void books() {
        enableBooks = getBoolean("enable-books", true,
                "Whether or not books should be writeable.",
                "Servers that anticipate being a target for duping may want to consider",
                "disabling this option.",
                "This can be overridden per-player with the permission pufferfish.usebooks");
    }

    public static boolean enableSuffocationOptimization;
    private static void suffocationOptimization() {
        enableSuffocationOptimization = getBoolean("enable-suffocation-optimization", true,
                "Optimizes the suffocation check by selectively skipping",
                "the check in a way that still appears vanilla. This should",
                "be left enabled on most servers, but is provided as a",
                "configuration option if the vanilla deviation is undesirable.");
    }

    public static boolean enableAsyncMobSpawning;
    public static boolean asyncMobSpawningInitialized;
    private static void asyncMobSpawning() {
        boolean temp = getBoolean("enable-async-mob-spawning", true,
                "Whether or not asynchronous mob spawning should be enabled.",
                "On servers with many entities, this can improve performance by up to 15%. You must have",
                "paper's per-player-mob-spawns setting set to true for this to work.",
                "One quick note - this does not actually spawn mobs async (that would be very unsafe).",
                "This just offloads some expensive calculations that are required for mob spawning.");

        // This prevents us from changing the value during a reload.
        if (!asyncMobSpawningInitialized) {
            asyncMobSpawningInitialized = true;
            enableAsyncMobSpawning = temp;
        }
    }

    public static int maxProjectileLoadsPerTick;
    public static int maxProjectileLoadsPerProjectile;
    private static void projectileLoading() {
        maxProjectileLoadsPerTick = getInt("projectile.max-loads-per-tick", 10, "Controls how many chunks are allowed", "to be sync loaded by projectiles in a tick.");
        maxProjectileLoadsPerProjectile = getInt("projectile.max-loads-per-projectile", 10, "Controls how many chunks a projectile", "can load in its lifetime before it gets", "automatically removed.");

        setComment("projectile", "Optimizes projectile settings");
    }


    public static boolean dearEnabled;
    public static int startDistance;
    public static int startDistanceSquared;
    public static int maximumActivationPrio;
    public static int activationDistanceMod;

    private static void dynamicActivationOfBrains() throws IOException {
        dearEnabled = getBoolean("dab.enabled", "activation-range.enabled", true);
        startDistance = getInt("dab.start-distance", "activation-range.start-distance", 12,
                "This value determines how far away an entity has to be",
                "from the player to start being effected by DEAR.");
        startDistanceSquared = startDistance * startDistance;
        maximumActivationPrio = getInt("dab.max-tick-freq", "activation-range.max-tick-freq", 20,
                "This value defines how often in ticks, the furthest entity",
                "will get their pathfinders and behaviors ticked. 20 = 1s");
        activationDistanceMod = getInt("dab.activation-dist-mod", "activation-range.activation-dist-mod", 8,
                "This value defines how much distance modifies an entity's",
                "tick frequency. freq = (distanceToPlayer^2) / (2^value)",
                "If you want further away entities to tick less often, use 7.",
                "If you want further away entities to tick more often, try 9.");

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            entityType.dabEnabled = true; // reset all, before setting the ones to true
        }
        getStringList("dab.blacklisted-entities", "activation-range.blacklisted-entities", Collections.emptyList(), "A list of entities to ignore for activation")
                .forEach(name -> EntityType.byString(name).ifPresentOrElse(entityType -> {
                    entityType.dabEnabled = false;
                }, () -> MinecraftServer.LOGGER.warn("Unknown entity \"" + name + "\"")));

        setComment("dab", "Optimizes entity brains when", "they're far away from the player");
    }

    public static boolean throttleInactiveGoalSelectorTick = true;
    private static void inactiveGoalSelectorThrottle() {
        throttleInactiveGoalSelectorTick = getBoolean("inactive-goal-selector-throttle", "inactive-goal-selector-disable", throttleInactiveGoalSelectorTick,
                "Throttles the AI goal selector in entity inactive ticks.",
                "This can improve performance by a few percent, but has minor gameplay implications.");
    }

    public static boolean allowEndCrystalRespawn = true;
    private static void allowEndCrystalRespawn() {
        allowEndCrystalRespawn = getBoolean("allow-end-crystal-respawn", allowEndCrystalRespawn,
                "Allows end crystals to respawn the ender dragon.",
                "On servers that expect end crystal fights in the end dimension, disabling this",
                "will prevent the server from performing an expensive search to attempt respawning",
                "the ender dragon whenever a player places an end crystal.");
    }

    public static URI profileWebUrl;
    private static void profilerOptions() {
        profileWebUrl = URI.create(getString("flare.url", "https://flare.airplane.gg", "Sets the server to use for profiles."));
        setComment("flare", "Configures Flare, the built-in profiler");
    }

    public static String accessToken;
    private static void airplaneWebServices() {
        accessToken = getString("web-services.token", "");
        // todo lookup token (off-thread) and let users know if their token is valid
        if (accessToken.length() > 0) {
            gg.pufferfish.pufferfish.flare.FlareSetup.init(); // Pufferfish
            SimpleCommandMap commandMap = MinecraftServer.getServer().server.getCommandMap();
            if (commandMap.getCommand("flare") == null) {
                commandMap.register("flare", "Pufferfish", new FlareCommand());
            }
        }

        setComment("web-services", "Options for connecting to Pufferfish/Airplane's online utilities");
    }

    public static boolean disableMethodProfiler = true;
     public static boolean disableOutOfOrderChat = false;
    private static void miscSettings() {
        disableMethodProfiler = getBoolean("misc.disable-method-profiler", disableMethodProfiler);
        disableOutOfOrderChat = getBoolean("misc.disable-out-of-order-chat", disableOutOfOrderChat);
       setComment("misc", "Settings for things that don't belong elsewhere");
    }
}
