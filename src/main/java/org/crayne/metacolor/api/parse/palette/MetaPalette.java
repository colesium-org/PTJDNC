package org.crayne.metacolor.api.parse.palette;

import org.crayne.metacolor.MetaColorPlugin;
import org.crayne.metacolor.api.MetaColorLike;
import org.crayne.metacolor.api.color.RGB;
import org.crayne.metacolor.api.style.MetaDecoration;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MetaPalette {

    @NotNull
    private final Map<String, RGB> colorPalette;

    @NotNull
    private final Map<String, MetaDecoration> decorationPalette;

    @NotNull
    private final List<String> keys;

    public MetaPalette() {
        this.colorPalette = new LinkedHashMap<>();
        this.decorationPalette = new LinkedHashMap<>(TEXT_DECORATION_KEYS);
        this.keys = new ArrayList<>();
    }

    @NotNull
    private static final Map<String, MetaDecoration> TEXT_DECORATION_KEYS = Map.of(
            "bold",          new MetaDecoration.Builder().bold(true).create(),
            "italic",        new MetaDecoration.Builder().italic(true).create(),
            "strikethrough", new MetaDecoration.Builder().strikethrough(true).create(),
            "underlined",    new MetaDecoration.Builder().underlined(true).create(),
            "obfuscated",    new MetaDecoration.Builder().obfuscated(true).create(),
            "gradient",      new MetaDecoration.Builder().gradient(true).create(),
            "flag",          new MetaDecoration.Builder().flag(true).create(),
            "alternating",   new MetaDecoration.Builder().alternating(true).create()
    );

    public void load() {
        keys.clear();
        colorPalette.clear();

        MetaColorPlugin.readEntryList("color_palette")
                .entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), Color.decode(e.getValue())))
                .forEach(e -> colorPalette.put(e.getKey(), RGB.of(e.getValue())));

        keys.addAll(colorPalette.keySet());
        keys.addAll(decorationPalette.keySet());
    }

    @NotNull
    public Map<String, RGB> colorPalette() {
        return colorPalette;
    }

    @NotNull
    public Map<String, MetaDecoration> decorationPalette() {
        return decorationPalette;
    }

    @NotNull
    public List<String> keys() {
        return keys;
    }

    @NotNull
    public Optional<MetaColorLike> findColor(@NotNull final String name) {
        if (decorationPalette.containsKey(name)) return Optional.ofNullable(decorationPalette.get(name));
        return Optional.ofNullable(colorPalette.get(name));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasColor(@NotNull final String name) {
        return keys.contains(name);
    }

}
