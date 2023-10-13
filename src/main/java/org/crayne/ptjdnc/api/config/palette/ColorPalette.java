package org.crayne.ptjdnc.api.config.palette;

import net.kyori.adventure.text.Component;
import org.crayne.ptjdnc.NameColorPlugin;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.color.RGB;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ColorPalette {

    @NotNull
    private final Map<String, RGB> colorPalette;

    @NotNull
    private final Map<String, NameDecoration> decorationPalette;

    @NotNull
    private final List<String> keys;

    @NotNull
    private final Map<String, Component> nameColorsStylized;

    public ColorPalette() {
        this.colorPalette = new LinkedHashMap<>();
        this.decorationPalette = new LinkedHashMap<>();

        this.decorationPalette.put("bold",          new NameDecoration.Builder().bold(true).create());
        this.decorationPalette.put("italic",        new NameDecoration.Builder().italic(true).create());
        this.decorationPalette.put("strikethrough", new NameDecoration.Builder().strikethrough(true).create());
        this.decorationPalette.put("underlined",    new NameDecoration.Builder().underlined(true).create());
        this.decorationPalette.put("obfuscated",    new NameDecoration.Builder().obfuscated(true).create());
        this.decorationPalette.put("gradient",      new NameDecoration.Builder().gradient(true).create());
        this.decorationPalette.put("flag",          new NameDecoration.Builder().flag(true).create());
        this.decorationPalette.put("alternating",   new NameDecoration.Builder().alternating(true).create());

        this.keys = new ArrayList<>();
        this.nameColorsStylized = new LinkedHashMap<>();
    }

    public void load() {
        keys.clear();
        nameColorsStylized.clear();
        colorPalette.clear();

        NameColorPlugin.readEntryList("color_palette")
                .entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), Color.decode(e.getValue())))
                .forEach(e -> colorPalette.put(e.getKey(), RGB.of(e.getValue())));

        keys.addAll(colorPalette.keySet());
        keys.addAll(decorationPalette.keySet());

        keys.add("hex");
        keys.add("rgb");

        keys.forEach(colorName -> nameColorsStylized.put(colorName, findColor(colorName)
                        .orElseThrow()
                        .stylize(colorName)));
    }

    @NotNull
    public Map<String, Component> colorsStylized() {
        return nameColorsStylized;
    }

    @NotNull
    public Map<String, RGB> colorPalette() {
        return colorPalette;
    }

    @NotNull
    public Map<String, NameDecoration> decorationPalette() {
        return decorationPalette;
    }

    @NotNull
    public List<String> keys() {
        return keys;
    }

    @NotNull
    public Optional<ColorLike> findColor(@NotNull final String name) {
        if (name.equals("rgb") || name.equals("hex")) return Optional.of(RGB.of(Color.LIGHT_GRAY));

        if (decorationPalette.containsKey(name)) return Optional.ofNullable(decorationPalette.get(name));
        return Optional.ofNullable(colorPalette.get(name));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasColor(@NotNull final String name) {
        return keys.contains(name);
    }

}
