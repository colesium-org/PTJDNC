package org.crayne.ptjdnc.api.config.ptjd;

import org.crayne.ptjdnc.NameColorPlugin;
import org.crayne.ptjdnc.api.config.ConfigParseException;
import org.crayne.ptjdnc.api.config.palette.ColorPalette;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PlaytimeJoindatePalette {

    @NotNull
    private final Map<String, PlaytimeJoindateRequirement> requirementMap;

    public PlaytimeJoindatePalette() {
        this.requirementMap = new LinkedHashMap<>();
    }

    public void load(@NotNull final ColorPalette palette) {
        final Map<String, String> requirementEntries = NameColorPlugin.readEntryList("playtime_joindate_requirements");

        requirementEntries
                .entrySet()
                .stream()
                .peek(e -> {
                    if (!palette.hasColor(e.getKey())) throw new ConfigParseException("Unknown color: " + e.getKey());
                })
                .map(e -> Map.entry(e.getKey(), PlaytimeJoindateRequirement.parseRequirement(e.getValue())))
                .forEach(e -> requirementMap.put(e.getKey(), e.getValue()));
    }

    @NotNull
    public Map<String, PlaytimeJoindateRequirement> requirementMap() {
        return requirementMap;
    }

    @NotNull
    public Optional<PlaytimeJoindateRequirement> nameColorRequirement(@NotNull final String colorName) {
        return Optional.ofNullable(requirementMap.get(colorName));
    }

}
