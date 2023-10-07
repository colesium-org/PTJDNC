package org.crayne.metacolor.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.metacolor.api.color.MetaColor;
import org.crayne.metacolor.api.color.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GradientMetaColor extends MetaColor {

    public GradientMetaColor(@NotNull final List<RGB> colors) {
        super(colors);
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        if (colors().isEmpty() || string.isBlank()) return Component.text(string);
        if (colors().size() == 1) return colors().get(0).stylize(Component.text(string));
        final int length = string.length();
        final int colors = colors().size();

        Component result = Component.text("");
        final int flagColors = colors - 1;
        final int individualStringLength = (length + (length % flagColors)) / flagColors;

        for (int i = 0; i < flagColors; i++) {
            final RGB color = (RGB) colors().get(i);
            final RGB nextColor = (RGB) colors().get(i + 1);

            final int end = i + 1 == flagColors ? length : Math.min(length, (i + 1) * individualStringLength);
            for (int j = i * individualStringLength; j < end; j++) {
                final double step = (double) (j - i * individualStringLength + (i == 0 ? 0 : 1)) / (double) (end  - i * individualStringLength);
                result = result.append(color.interpolate(step, nextColor).colorize(string.charAt(j)));
            }
        }
        return result;
    }

    @NotNull
    public String toString() {
        return "GradientMetaColor{" +
                "colors=" + colors() +
                '}';
    }
}
