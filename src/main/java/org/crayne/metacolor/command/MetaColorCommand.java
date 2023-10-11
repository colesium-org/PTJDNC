package org.crayne.metacolor.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.crayne.metacolor.MetaColorPlugin;
import org.crayne.metacolor.api.MetaColorLike;
import org.crayne.metacolor.api.MetaNameColor;
import org.crayne.metacolor.api.color.MetaColor;
import org.crayne.metacolor.api.color.RGB;
import org.crayne.metacolor.api.color.namecolor.AlternatingMetaColor;
import org.crayne.metacolor.api.color.namecolor.FlagMetaColor;
import org.crayne.metacolor.api.color.namecolor.GradientMetaColor;
import org.crayne.metacolor.api.color.namecolor.SingletonMetaColor;
import org.crayne.metacolor.api.parse.ptjd.MetaPTJDRequirement;
import org.crayne.metacolor.api.style.MetaDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class MetaColorCommand implements CommandExecutor {

    @NotNull
    private static final TextColor COLOR_RED = TextColor.color(255, 0, 0);

    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String label, @NotNull final String @NotNull [] args) {
        if (!(commandSender instanceof final Player p)) {
            commandSender.sendMessage(Component.text("Only players may use this command.").color(COLOR_RED));
            return false;
        }
        if (args.length == 0) {
            p.sendMessage(deserialize("nc_info_page", s -> s
                    .replace("%ncname%", displayName(p))
                    .replace("%pt%", (int) MetaPTJDRequirement.playTimeInHours(p) + "")
                    .replace("%jd%", MetaPTJDRequirement.joinDateFormatted(p))
                    .replace("%jdd%", (int) MetaPTJDRequirement.joinDateInDays(p) + "")
                    .replace("%nclist%", availableNamecolors(p))));
            return true;
        }
        final MetaNameColor oldNameColor = MetaColorPlugin.plugin().nameColor(p);
        final NameColorCommandResult nameColorParsed = parseNameColor(args, p, oldNameColor);
        final Component errorMessage = nameColorParsed.message();
        if (errorMessage != null) {
            p.sendMessage(errorMessage);
            return false;
        }
        final MetaNameColor newNameColor = nameColorParsed.toMetaNameColor();
        MetaColorPlugin.plugin().nameColor(p, newNameColor);
        MetaColorPlugin.plugin().updateNameColor(p);

        p.sendMessage(sendNameColorChanged(p));
        return true;
    }

    @NotNull
    private static Component sendNameColorChanged(@NotNull final Player p) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(
                commandText("nc_changed")
                        .replace("%ncname%", displayName(p)));
    }

    @NotNull
    public static MetaNameColor nameColorOfParsed(@NotNull final List<MetaColorLike> colorsParam, @NotNull final MetaDecoration decorationParam, @NotNull final MetaNameColor defaultNameColor) {
        final List<? extends MetaColorLike> colors = colorsParam.isEmpty() ? defaultNameColor.color().colors() : colorsParam;
        final MetaDecoration decoration = decorationParam.undecorated() ? defaultNameColor.decoration() : decorationParam;

        final MetaColor metaColor;
        if (!decoration.hasSpecialDecorators()) metaColor = new SingletonMetaColor(colors);
        else if (decoration.gradient()) metaColor = new GradientMetaColor(colors.stream().map(c -> (RGB) c).toList());
        else if (decoration.flag()) metaColor = new FlagMetaColor(colors);
        else if (decoration.alternating()) metaColor = new AlternatingMetaColor(colors);
        else metaColor = new SingletonMetaColor(colors);

        return new MetaNameColor(metaColor, decoration);
    }

    public record NameColorCommandResult(@Nullable Component message, @Nullable MetaColorLike nameColor) {

        @NotNull
        public static NameColorCommandResult successMessage(@NotNull final Player p) {
            return message(sendNameColorChanged(p));
        }

        @NotNull
        public static NameColorCommandResult message(@NotNull final Component component) {
            return new NameColorCommandResult(component, null);
        }

        @NotNull
        public static NameColorCommandResult success(@NotNull final MetaColorLike nameColor) {
            return new NameColorCommandResult(null, nameColor);
        }

        @NotNull
        public MetaNameColor toMetaNameColor() {
            final MetaNameColor newNameColor = (MetaNameColor) nameColor();
            // return value of parseNameColor always has a MetaNameColor as the namecolor, or null

            assert newNameColor != null; // error message and name color should never both be null

            return newNameColor;
        }

    }


    @NotNull
    private static NameColorCommandResult parseDefaultNameColor(@Nullable final Player p, @NotNull final String namecolorString) {
        final Optional<MetaColorLike> color = findColorByParameter(p, namecolorString);
        if (color.isEmpty()) return unknownNameColor(p, namecolorString);

        return NameColorCommandResult.success(color.get());
    }

    @NotNull
    private static NameColorCommandResult parseHexNameColor(@NotNull final String[] args, @Nullable final Player p,
                                                            @NotNull final AtomicInteger index) {
        if (p != null && !MetaColorPlugin.plugin().nameColorAccessible(p, "hex")) return unknownNameColor(p, "hex");

        final int i = index.get();
        if (i + 1 >= args.length) return deserializeMessage("nc_no_hex_specified", s -> s);
        final String hexCodeString = args[i + 1];
        index.incrementAndGet();

        try {
            return NameColorCommandResult.success(RGB.of(Color.decode(hexCodeString)));
        } catch (final NumberFormatException e) {
            return deserializeMessage("nc_invalid_hex_specified", s -> s);
        }
    }

    @NotNull
    private static NameColorCommandResult parseRGBNameColor(@NotNull final String[] args, @Nullable final Player p,
                                                            @NotNull final AtomicInteger index) {
        if (p != null && !MetaColorPlugin.plugin().nameColorAccessible(p, "rgb")) return unknownNameColor(p, "rgb");

        final int i = index.get();
        if (i + 3 >= args.length) return deserializeMessage("nc_no_rgb_specified", s -> s);
        final String redComponentString   = args[i + 1];
        final String greenComponentString = args[i + 2];
        final String blueComponentString  = args[i + 3];
        index.set(index.get() + 3);

        try {
            final int r = Integer.parseInt(redComponentString),
                      g = Integer.parseInt(greenComponentString),
                      b = Integer.parseInt(blueComponentString);

            return NameColorCommandResult.success(RGB.of(r, g, b));
        } catch (final NumberFormatException e) {
            return deserializeMessage("nc_invalid_component_specified", s -> s);
        }
    }

    @NotNull
    private static NameColorCommandResult parseSingleNameColor(@NotNull final String[] args, @Nullable final Player p,
                                                      @NotNull final AtomicInteger index, @NotNull final String namecolorString) {
        return switch (namecolorString) {
            case "hex" -> parseHexNameColor(args, p, index);
            case "rgb" -> parseRGBNameColor(args, p, index);
            default -> parseDefaultNameColor(p, namecolorString);
        };
    }

    @NotNull
    private static Optional<NameColorCommandResult> collectNameColorParameters(@NotNull final String[] args, @Nullable final Player p,
                                                   @NotNull final List<MetaColorLike> colors,
                                                   @NotNull final List<MetaDecoration> decorations) {
        if (args.length == 1 && args[0].equals("reset") && p != null) {
            MetaColorPlugin.plugin().nameColor(p, MetaColorPlugin.plugin().defaultNameColor());
            MetaColorPlugin.plugin().updateNameColor(p);

            return Optional.of(NameColorCommandResult.successMessage(p));
        }

        for (int i = 0; i < args.length; i++) {
            final String namecolorString = args[i];
            final AtomicInteger atomicIndex = new AtomicInteger(i);
            final NameColorCommandResult result = parseSingleNameColor(args, p, atomicIndex, namecolorString);
            i = atomicIndex.get();

            if (result.message != null) return Optional.of(result);
            assert result.nameColor != null;

            if (result.nameColor instanceof final MetaDecoration decoration) decorations.add(decoration);
            else colors.add(result.nameColor);
        }
        return Optional.empty();
    }

    @NotNull
    private static String invalidColorAmountConfigKey(@Nullable final Player p) {
        final String accessModifier = p == null || MetaColorPlugin.plugin().canUseColorCombinations(p) ? "" : "no";
        return "nc_invalid_color_amount_" + accessModifier + "access_special_deco";
    }

    @NotNull
    public static NameColorCommandResult parseNameColor(@NotNull final String[] args, @Nullable final Player p, @NotNull final MetaNameColor defaultNameColor) {
        final List<MetaColorLike> colors = new ArrayList<>();
        final List<MetaDecoration> decorations = new ArrayList<>();

        final Optional<NameColorCommandResult> earlyParseReturn = collectNameColorParameters(args, p, colors, decorations);
        if (earlyParseReturn.isPresent())
            return earlyParseReturn.get();

        final MetaDecoration decoration = MetaDecoration.combine(decorations);
        if (decoration.hasInvalidCombinations())
            return deserializeMessage("nc_invalid_deco_combos", s -> s);

        final int maxColorCombos = MetaColorPlugin.config().getInt("max_namecolor_combinations");

        if (!decoration.hasSpecialDecorators() && colors.size() > 1)
            return deserializeMessage(invalidColorAmountConfigKey(p), s -> s);

        if (colors.size() > maxColorCombos)
            return deserializeMessage("nc_invalid_color_amount",
                    s -> s.replace("%maxnc%", maxColorCombos + ""));

        return NameColorCommandResult.success(nameColorOfParsed(colors, decoration, defaultNameColor));
    }

    @NotNull
    private static NameColorCommandResult unknownNameColor(@Nullable final Player p, @NotNull final String unknownColor) {
        return deserializeMessage("nc_unknown_color", s -> s
                        .replace("%ncname%", p == null ? "Console" : displayName(p))
                        .replace("%nclist%", p == null
                                ? availableNamecolors(MetaColorPlugin.plugin().allNameColorsStylized())
                                : availableNamecolors(p))

                        .replace("%unknownnc%", unknownColor));
    }

    @NotNull
    private static Optional<MetaColorLike> findColorByParameter(@Nullable final Player p, @NotNull final String s) {
        return p == null
                ? MetaColorPlugin.plugin().colorPalette().findColor(s)
                : MetaColorPlugin.plugin().findAvailableColor(p, s);
    }

    @NotNull
    private static Component deserialize(@NotNull final String configKey, @NotNull final Function<String, String> stringMapper) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(stringMapper.apply(commandText(configKey)));
    }

    @NotNull
    private static NameColorCommandResult deserializeMessage(@NotNull final String configKey, @NotNull final Function<String, String> stringMapper) {
        return NameColorCommandResult.message(deserialize(configKey, stringMapper));
    }

    @NotNull
    private static String displayName(@NotNull final Player p) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(p.displayName());
    }

    @NotNull
    private static String availableNamecolors(@NotNull final Player p) {
        return availableNamecolors(MetaColorPlugin.plugin().accessibleNameColorsStylized(p));
    }

    @NotNull
    private static String availableNamecolors(@NotNull final List<Component> accessibleNameColorsStylized) {
        return String.join("&r ", accessibleNameColorsStylized
                .stream()
                .map(LegacyComponentSerializer.legacyAmpersand()::serialize)
                .toList());
    }

    @NotNull
    private static String commandText(@NotNull final String key) {
        return Optional.ofNullable(MetaColorPlugin.config().getString("namecolor_command_texts." + key)).orElseThrow();
    }

}
