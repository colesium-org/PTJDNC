package org.crayne.metacolor;

import org.bukkit.plugin.java.JavaPlugin;
import org.crayne.metacolor.api.MetaNameColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MetaColorPlugin extends JavaPlugin {

    @Nullable
    private static MetaColorPlugin plugin;

    @NotNull
    private static Map<UUID, MetaNameColor> nameColorMap = new HashMap<>();

    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        final File nameColorFile = nameColorFile();
        if (nameColorFile.isFile()) {
            try {
                nameColorMap = MetaNameColor.load(nameColorFile);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onDisable() {
        try {
            MetaNameColor.save(nameColorMap, nameColorFile());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static MetaColorPlugin plugin() {
        return Optional.ofNullable(plugin).orElseThrow(() -> new RuntimeException("The plugin has not been initialized yet."));
    }

    @NotNull
    private File nameColorFile() {
        return new File(getDataFolder(), "namecolors.nci");
    }

}
