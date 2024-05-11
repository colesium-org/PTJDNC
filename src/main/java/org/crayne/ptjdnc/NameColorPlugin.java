package org.crayne.ptjdnc;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.crayne.ptjdnc.api.config.ConfigParseException;
import org.crayne.ptjdnc.api.profile.GlobalNameStyleProfile;
import org.crayne.ptjdnc.command.ItemColorCommand;
import org.crayne.ptjdnc.command.NameColorCommand;
import org.crayne.ptjdnc.command.NameColorTabCompleter;
import org.crayne.ptjdnc.event.PlayerOpStatusChangeEvent;
import org.crayne.ptjdnc.event.listener.PlayerEventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NameColorPlugin extends JavaPlugin {

    @Nullable
    private static NameColorPlugin plugin;

    @NotNull
    private final Map<UUID, Boolean> previousOpValues = new HashMap<>();

    public void onEnable() {
        plugin = this;

        config().options().copyDefaults();
        saveDefaultConfig();

        GlobalNameStyleProfile.INSTANCE.load();
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);

        final NameColorTabCompleter ncTabCompleter = new NameColorTabCompleter();
        Stream.of("namecolor", "nc")
                .map(this::getCommand)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    c.setExecutor(new NameColorCommand());
                    c.setTabCompleter(ncTabCompleter);
                });

        Stream.of("itemcolor", "ic")
                .map(this::getCommand)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    c.setExecutor(new ItemColorCommand());
                    c.setTabCompleter(ncTabCompleter);
                });

        // probably not the best solution for a player op event, but atleast it works? spigot or paper, please add an op event :sob:
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            onlinePlayers.forEach(p -> previousOpValues.putIfAbsent(p.getUniqueId(), p.isOp()));

            final Map<UUID, Boolean> difference = new HashMap<>();

            onlinePlayers.forEach(player -> {
                final UUID uuid = player.getUniqueId();
                final boolean wasOp = previousOpValues.get(uuid);
                if (player.isOp() != wasOp) {
                    difference.put(uuid, wasOp);
                    previousOpValues.put(uuid, player.isOp());
                }
            });

            difference.forEach((uuid, wasOp) -> {
                final Player player = Bukkit.getPlayer(uuid);
                if (player == null) return;

                final PlayerOpStatusChangeEvent opStatusChangeEvent = new PlayerOpStatusChangeEvent(player, wasOp, player.isOp(), true);
                Bukkit.getPluginManager().callEvent(opStatusChangeEvent);
            });
        }, 20L, 20L);
    }

    public void onDisable() {
        GlobalNameStyleProfile.INSTANCE.save();
    }

    public void removeTrackedPlayer(@NotNull final UUID uuid) {
        previousOpValues.remove(uuid);
    }

    @NotNull
    public static NameColorPlugin plugin() {
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
                            throw new ConfigParseException("Invalid configuration. Missing equals sign (" + s + ")");
                    })
                    .map(s -> Map.entry(StringUtils.substringBefore(s, "="), StringUtils.substringAfter(s, "=")))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
        } catch (final Exception e) {
            throw new ConfigParseException(e);
        }
    }

}
