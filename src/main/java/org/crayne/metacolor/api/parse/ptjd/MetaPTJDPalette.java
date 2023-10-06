package org.crayne.metacolor.api.parse.ptjd;

import org.bukkit.entity.Player;
import org.crayne.metacolor.MetaColorPlugin;
import org.crayne.metacolor.api.parse.MetaParseException;
import org.crayne.metacolor.api.parse.palette.MetaPalette;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetaPTJDPalette {

    @NotNull
    private final Map<String, MetaPTJDRequirement> requirementMap;

    public MetaPTJDPalette() {
        this.requirementMap = new LinkedHashMap<>();
    }

    public void load(@NotNull final MetaPalette palette) {
        final Map<String, String> requirementEntries = MetaColorPlugin.readEntryList("playtime_joindate_requirements");

        requirementEntries
                .entrySet()
                .stream()
                .peek(e -> {
                    if (!palette.hasColor(e.getKey())) throw new MetaParseException("Unknown color: " + e.getKey());
                })
                .map(e -> Map.entry(e.getKey(), MetaPTJDRequirement.parseRequirement(e.getValue())))
                .forEach(e -> requirementMap.put(e.getKey(), e.getValue()));
    }

    @NotNull
    public Map<String, MetaPTJDRequirement> requirementMap() {
        return requirementMap;
    }

    public boolean nameColorAccessible(@NotNull final Player player, @NotNull final String colorName) {
        return requirementMap.containsKey(colorName) && requirementMap.get(colorName).allows(player);
    }

}
