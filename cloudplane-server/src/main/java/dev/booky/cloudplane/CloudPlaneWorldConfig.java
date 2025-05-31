package dev.booky.cloudplane;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

import static dev.booky.cloudplane.CloudPlaneConfig.log;

public class CloudPlaneWorldConfig {

    private final String worldName;
    private final World.Environment environment;

    public CloudPlaneWorldConfig(String worldName, World.Environment environment) {
        this.worldName = worldName;
        this.environment = environment;
        this.init();
    }

    public void init() {
        log("-------- World Settings For [" + worldName + "] --------");
        CloudPlaneConfig.readConfig(CloudPlaneWorldConfig.class, this);
    }

    private void set(String path, Object val) {
        CloudPlaneConfig.config.addDefault("world-settings.default." + path, val);
        CloudPlaneConfig.config.set("world-settings.default." + path, val);
        if (CloudPlaneConfig.config.get("world-settings." + this.worldName + "." + path) != null) {
            CloudPlaneConfig.config.addDefault("world-settings." + this.worldName + "." + path, val);
            CloudPlaneConfig.config.set("world-settings." + this.worldName + "." + path, val);
        }
    }

    private void setComment(String path, String... comment) {
        CloudPlaneConfig.config.setComments("world-settings.default." + path, List.of(comment));
        if (CloudPlaneConfig.config.get("world-settings." + this.worldName + "." + path) != null) {
            CloudPlaneConfig.config.setComments("world-settings.default." + path, List.of(comment));
        }
    }

    private ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection section = CloudPlaneConfig.config.getConfigurationSection("world-settings." + this.worldName + "." + path);
        return section != null ? section : CloudPlaneConfig.config.getConfigurationSection("world-settings.default." + path);
    }

    private boolean getBoolean(String path, boolean def) {
        CloudPlaneConfig.config.addDefault("world-settings.default." + path, def);
        return CloudPlaneConfig.config.getBoolean("world-settings." + this.worldName + "." + path, CloudPlaneConfig.config.getBoolean("world-settings.default." + path));
    }

    private double getDouble(String path, double def) {
        CloudPlaneConfig.config.addDefault("world-settings.default." + path, def);
        return CloudPlaneConfig.config.getDouble("world-settings." + this.worldName + "." + path, CloudPlaneConfig.config.getDouble("world-settings.default." + path));
    }

    private int getInt(String path, int def) {
        CloudPlaneConfig.config.addDefault("world-settings.default." + path, def);
        return CloudPlaneConfig.config.getInt("world-settings." + this.worldName + "." + path, CloudPlaneConfig.config.getInt("world-settings.default." + path));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getList(String path, List<T> def) {
        CloudPlaneConfig.config.addDefault("world-settings.default." + path, def);
        return (List<T>) CloudPlaneConfig.config.getList("world-settings." + this.worldName + "." + path, CloudPlaneConfig.config.getList("world-settings.default." + path));
    }

    private String getString(String path, String def) {
        CloudPlaneConfig.config.addDefault("world-settings.default." + path, def);
        return CloudPlaneConfig.config.getString("world-settings." + this.worldName + "." + path, CloudPlaneConfig.config.getString("world-settings.default." + path));
    }

    private Component getComponent(String path, Component def) {
        return MiniMessage.miniMessage().deserialize(getString(path,
                MiniMessage.miniMessage().serialize(def)));
    }
}
