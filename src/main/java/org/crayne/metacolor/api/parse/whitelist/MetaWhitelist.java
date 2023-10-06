package org.crayne.metacolor.api.parse.whitelist;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.crayne.metacolor.MetaColorPlugin;
import org.crayne.metacolor.api.parse.MetaParseException;
import org.crayne.metacolor.api.parse.palette.MetaPalette;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record MetaWhitelist(@NotNull Set<UUID> whitelistedUsers, @NotNull List<String> whitelistedColors) {

    @NotNull
    public static Set<MetaWhitelist> loadWhitelists(@NotNull final MetaPalette palette) {
        final ConfigurationSection whitelistSection = MetaColorPlugin.config().getConfigurationSection("whitelists");
        if (whitelistSection == null) return new HashSet<>();

        return whitelistSection
                .getValues(true)
                .keySet()
                .stream()
                .map(whitelist -> new MetaWhitelist(
                        whitelistSection.getStringList(whitelist + ".whitelisted_users")
                                .stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toSet()),

                        whitelistSection.getStringList(whitelist + ".color_palette")
                                .stream()
                                .peek(s -> {
                                    if (!palette.hasColor(s)) throw new MetaParseException("Unknown color: " + s);
                                })
                                .toList())
                )
                .collect(Collectors.toSet());
    }

    public boolean nameColorAccessible(@NotNull final Player player, @NotNull final String colorName) {
        return whitelistedUsers.contains(player.getUniqueId()) && whitelistedColors.contains(colorName);
    }

}
