package org.crayne.ptjdnc.api.color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.crayne.ptjdnc.api.ColorLike;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class RGB implements ColorLike {

    private final int r, g, b;

    public RGB(final int r, final int g, final int b) {
        if (invalidRange(r) || invalidRange(g) || invalidRange(b))
            throw new IllegalArgumentException("RGB values are out of range (min 0, max 255) " +
                    "(" + r + ", " + g + ", " + b + ")");

        this.r = r;
        this.g = g;
        this.b = b;
    }

    private static boolean invalidRange(final int component) {
        return component < 0 || component > 255;
    }

    public byte @NotNull [] encode() {
        return new byte[] {(byte) r, (byte) g, (byte) b};
    }

    @NotNull
    public TextColor textColor() {
        return TextColor.color(r, g, b);
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        return colorize(Component.text(string));
    }

    @NotNull
    public Component stylize(@NotNull final String s) {
        return colorize(s);
    }

    @NotNull
    public Component colorize(final char character) {
        return colorize(Component.text(character));
    }

    @NotNull
    public Component colorize(@NotNull final Component component) {
        return component.color(textColor());
    }

    @NotNull
    public Component stylize(@NotNull final Component component) {
        return colorize(component);
    }

    @NotNull
    public RGB interpolate(final double step, @NotNull final RGB other) {
        return RGB.of(
                lerp(step, r, other.r),
                lerp(step, g, other.g),
                lerp(step, b, other.b)
        );
    }

    private static int lerp(final double step, final int min, final int max) {
        return (int) (min + step * (max - min));
    }

    @NotNull
    public static RGB of(final int r, final int g, final int b) {
        return new RGB(r, g, b);
    }

    @NotNull
    public static RGB of(@NotNull final Color color) {
        return of(color.getRed(), color.getGreen(), color.getBlue());
    }

    public String toString() {
        return "RGB{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                '}';
    }
}
