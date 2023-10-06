package org.crayne.metacolor;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.crayne.metacolor.api.MetaNameColor;
import org.crayne.metacolor.api.parse.MetaParseException;
import org.crayne.metacolor.api.parse.palette.MetaPalette;
import org.crayne.metacolor.api.parse.ptjd.MetaPTJDPalette;
import org.crayne.metacolor.api.parse.whitelist.MetaWhitelist;
import org.crayne.metacolor.command.MetaColorCommand;
import org.crayne.metacolor.command.MetaColorTabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaColorPlugin extends JavaPlugin {

    @Nullable
    private static MetaColorPlugin plugin;

    @NotNull
    private static Map<UUID, MetaNameColor> nameColorMap = new HashMap<>();

    @NotNull
    private final MetaPalette colorPalette = new MetaPalette();

    @NotNull
    private final MetaPTJDPalette ptjdPalette = new MetaPTJDPalette();

    @NotNull
    private final Set<MetaWhitelist> whitelists = new HashSet<>();

    public void onEnable() {
        plugin = this;

        config().options().copyDefaults();
        saveDefaultConfig();

        final File nameColorFile = nameColorFile();
        if (nameColorFile.isFile()) {
            try {
                nameColorMap = MetaNameColor.load(nameColorFile);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        colorPalette.load();
        ptjdPalette.load(colorPalette);
        whitelists.addAll(MetaWhitelist.loadWhitelists(colorPalette));

        Stream.of("namecolor", "nc")
                .map(this::getCommand)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    c.setExecutor(new MetaColorCommand());
                    c.setTabCompleter(new MetaColorTabCompleter());
                });
    }

    public void onDisable() {
        try {
            MetaNameColor.save(nameColorMap, nameColorFile());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean nameColorAccessible(@NotNull final Player player, @NotNull final String colorName) {
        return (player.isOp() && config().getBoolean("allow_op_bypass_restrictions"))
                || ptjdPalette.nameColorAccessible(player, colorName)
                || whitelists.stream().anyMatch(whitelist -> whitelist.nameColorAccessible(player, colorName));
    }

    @NotNull
    public List<String> accessibleNameColors(@NotNull final Player player) {
        return colorPalette.keys()
                .stream()
                .filter(nameColor -> nameColorAccessible(player, nameColor))
                .toList();
    }

    @NotNull
    public List<Component> accessibleNameColorsStylized(@NotNull final Player player) {
        return accessibleNameColors(player)
                .stream()
                .map(colorName -> colorPalette.findColor(colorName)
                        .orElseThrow()
                        .stylize(colorName))
                .toList();
    }

    public void updateNameColor(@NotNull final Player player) {
        final MetaNameColor nameColor = nameColorMap.get(player.getUniqueId());
        if (nameColor == null) return;

        player.displayName(nameColor.stylize(player.name()));
    }

    @NotNull
    public static MetaColorPlugin plugin() {
        return Optional.ofNullable(plugin).orElseThrow(() -> new RuntimeException("The plugin has not been initialized yet."));
    }

    @NotNull
    public static FileConfiguration config() {
        return plugin().getConfig();
    }

    @NotNull
    public static Map<String, String> readEntryList(@NotNull final String configKey) {
        try {
            return config().getStringList(configKey).stream()
                    .peek(s -> {
                        if (!s.contains("="))
                            throw new MetaParseException("Invalid configuration. Missing equals sign (" + s + ")");
                    })
                    .map(s -> Map.entry(StringUtils.substringBefore(s, "="), StringUtils.substringAfter(s, "=")))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
        } catch (final Exception e) {
            throw new MetaParseException(e);
        }
    }

    @NotNull
    private File nameColorFile() {
        return new File(getDataFolder(), "namecolors.nci");
    }

}
