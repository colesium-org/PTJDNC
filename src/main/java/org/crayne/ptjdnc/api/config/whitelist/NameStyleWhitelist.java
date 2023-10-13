package org.crayne.ptjdnc.api.config.whitelist;

import org.bukkit.configuration.ConfigurationSection;
import org.crayne.ptjdnc.NameColorPlugin;
import org.crayne.ptjdnc.api.config.ConfigParseException;
import org.crayne.ptjdnc.api.config.palette.ColorPalette;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record NameStyleWhitelist(@NotNull Set<UUID> whitelistedUsers, @NotNull List<String> whitelistedColors) {

    @NotNull
    public static Set<NameStyleWhitelist> loadWhitelists(@NotNull final ColorPalette palette) {
        final ConfigurationSection whitelistSection = NameColorPlugin.config().getConfigurationSection("whitelists");
        if (whitelistSection == null) return new HashSet<>();

        return whitelistSection
                .getValues(true)
                .keySet()
                .stream()
                .map(whitelist -> new NameStyleWhitelist(
                        whitelistSection.getStringList(whitelist + ".whitelisted_users")
                                .stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toSet()),

                        whitelistSection.getStringList(whitelist + ".color_palette")
                                .stream()
                                .peek(s -> {
                                    if (!palette.hasColor(s)) throw new ConfigParseException("Unknown color: " + s);
                                })
                                .toList())
                )
                .collect(Collectors.toSet());
    }

    public boolean nameColorAccessible(@NotNull final UUID uuid, @NotNull final String colorName) {
        return whitelistedUsers.contains(uuid) && whitelistedColors.contains(colorName);
    }

}
