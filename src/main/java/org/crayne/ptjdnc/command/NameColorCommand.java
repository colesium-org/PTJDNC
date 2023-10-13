package org.crayne.ptjdnc.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.crayne.ptjdnc.NameColorPlugin;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.NameStyle;
import org.crayne.ptjdnc.api.color.NameColor;
import org.crayne.ptjdnc.api.color.RGB;
import org.crayne.ptjdnc.api.color.namecolor.AlternatingNameColor;
import org.crayne.ptjdnc.api.color.namecolor.FlagNameColor;
import org.crayne.ptjdnc.api.color.namecolor.GradientNameColor;
import org.crayne.ptjdnc.api.color.namecolor.SingletonNameColor;
import org.crayne.ptjdnc.api.config.ptjd.PlaytimeJoindateRequirement;
import org.crayne.ptjdnc.api.profile.GlobalNameStyleProfile;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class NameColorCommand implements CommandExecutor {

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
                    .replace("%pt%", (int) PlaytimeJoindateRequirement.playTimeInHours(p) + "")
                    .replace("%jd%", PlaytimeJoindateRequirement.joinDateFormatted(p))
                    .replace("%jdd%", (int) PlaytimeJoindateRequirement.joinDateInDays(p) + "")
                    .replace("%nclist%", availableNamecolors(p))));
            return true;
        }
        final GlobalNameStyleProfile globalNameStyleProfile = GlobalNameStyleProfile.INSTANCE;

        final NameStyle oldNameStyle = globalNameStyleProfile.nameStyle(p);
        final NameColorCommandResult nameColorParsed = parseNameColor(args, p, oldNameStyle);
        final Component errorMessage = nameColorParsed.message();
        if (errorMessage != null) {
            p.sendMessage(errorMessage);
            return false;
        }
        final NameStyle newNameStyle = nameColorParsed.toMetaNameColor();
        globalNameStyleProfile.nameStyle(p, newNameStyle);
        globalNameStyleProfile.updateNameStyle(p);

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
    public static NameStyle nameColorOfParsed(@NotNull final List<ColorLike> colorsParam, @NotNull final NameDecoration decorationParam, @NotNull final NameStyle defaultNameStyle) {
        final List<? extends ColorLike> colors = colorsParam.isEmpty() ? defaultNameStyle.color().colors() : colorsParam;
        final NameDecoration decoration = decorationParam.undecorated() ? defaultNameStyle.decoration() : decorationParam;

        final NameColor nameColor;
        if (!decoration.hasSpecialDecorators()) nameColor = new SingletonNameColor(colors);
        else if (decoration.gradient()) nameColor = new GradientNameColor(colors.stream().map(c -> (RGB) c).toList());
        else if (decoration.flag()) nameColor = new FlagNameColor(colors);
        else if (decoration.alternating()) nameColor = new AlternatingNameColor(colors);
        else nameColor = new SingletonNameColor(colors);

        return new NameStyle(nameColor, decoration);
    }

    public record NameColorCommandResult(@Nullable Component message, @Nullable ColorLike nameColor) {

        @NotNull
        public static NameColorCommandResult successMessage(@NotNull final Player p) {
            return message(sendNameColorChanged(p));
        }

        @NotNull
        public static NameColorCommandResult message(@NotNull final Component component) {
            return new NameColorCommandResult(component, null);
        }

        @NotNull
        public static NameColorCommandResult success(@NotNull final ColorLike nameColor) {
            return new NameColorCommandResult(null, nameColor);
        }

        @NotNull
        public NameStyle toMetaNameColor() {
            final NameStyle newNameStyle = (NameStyle) nameColor();
            // return value of parseNameColor always has a NameStyle as the namecolor, or null

            assert newNameStyle != null; // error message and name color should never both be null

            return newNameStyle;
        }

    }


    @NotNull
    private static NameColorCommandResult parseDefaultNameColor(@Nullable final Player p, @NotNull final String namecolorString) {
        final Optional<ColorLike> color = findColorByParameter(p, namecolorString);
        if (color.isEmpty()) return unknownNameColor(p, namecolorString);

        return NameColorCommandResult.success(color.get());
    }

    @NotNull
    private static NameColorCommandResult parseHexNameColor(@NotNull final String[] args, @Nullable final Player p,
                                                            @NotNull final AtomicInteger index) {

        final GlobalNameStyleProfile globalNameStyleProfile = GlobalNameStyleProfile.INSTANCE;
        if (p != null && !globalNameStyleProfile.nameColorAccessible(p, "hex")) return unknownNameColor(p, "hex");

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

        final GlobalNameStyleProfile globalNameStyleProfile = GlobalNameStyleProfile.INSTANCE;
        if (p != null && !globalNameStyleProfile.nameColorAccessible(p, "rgb")) return unknownNameColor(p, "rgb");

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
                                                   @NotNull final List<ColorLike> colors,
                                                   @NotNull final List<NameDecoration> decorations) {
        if (args.length == 1 && args[0].equals("reset") && p != null) {
            final GlobalNameStyleProfile globalNameStyleProfile = GlobalNameStyleProfile.INSTANCE;
            globalNameStyleProfile.nameStyle(p, globalNameStyleProfile.defaultNameColor());
            globalNameStyleProfile.updateNameStyle(p);

            return Optional.of(NameColorCommandResult.successMessage(p));
        }

        for (int i = 0; i < args.length; i++) {
            final String namecolorString = args[i];
            final AtomicInteger atomicIndex = new AtomicInteger(i);
            final NameColorCommandResult result = parseSingleNameColor(args, p, atomicIndex, namecolorString);
            i = atomicIndex.get();

            if (result.message != null) return Optional.of(result);
            assert result.nameColor != null;

            if (result.nameColor instanceof final NameDecoration decoration) decorations.add(decoration);
            else colors.add(result.nameColor);
        }
        return Optional.empty();
    }

    @NotNull
    private static String invalidColorAmountConfigKey(@Nullable final Player p) {
        final String accessModifier = p == null || GlobalNameStyleProfile.INSTANCE.combiningModifiersAccessible(p) ? "" : "no";
        return "nc_invalid_color_amount_" + accessModifier + "access_special_deco";
    }

    @NotNull
    public static NameColorCommandResult parseNameColor(@NotNull final String[] args, @Nullable final Player p, @NotNull final NameStyle defaultNameStyle) {
        final List<ColorLike> colors = new ArrayList<>();
        final List<NameDecoration> decorations = new ArrayList<>();

        final Optional<NameColorCommandResult> earlyParseReturn = collectNameColorParameters(args, p, colors, decorations);
        if (earlyParseReturn.isPresent())
            return earlyParseReturn.get();

        final NameDecoration decoration = NameDecoration.combine(decorations);
        if (decoration.hasInvalidCombinations())
            return deserializeMessage("nc_invalid_deco_combos", s -> s);

        final int maxColorCombos = NameColorPlugin.config().getInt("max_namecolor_combinations");

        if (!decoration.hasSpecialDecorators() && colors.size() > 1)
            return deserializeMessage(invalidColorAmountConfigKey(p), s -> s);

        if (colors.size() > maxColorCombos)
            return deserializeMessage("nc_invalid_color_amount",
                    s -> s.replace("%maxnc%", maxColorCombos + ""));

        return NameColorCommandResult.success(nameColorOfParsed(colors, decoration, defaultNameStyle));
    }

    @NotNull
    private static NameColorCommandResult unknownNameColor(@Nullable final Player p, @NotNull final String unknownColor) {
        return deserializeMessage("nc_unknown_color", s -> s
                        .replace("%ncname%", p == null ? "Console" : displayName(p))
                        .replace("%nclist%", p == null
                                ? availableNamecolors(GlobalNameStyleProfile.INSTANCE.allNameColorsStylized())
                                : availableNamecolors(p))

                        .replace("%unknownnc%", unknownColor));
    }

    @NotNull
    private static Optional<ColorLike> findColorByParameter(@Nullable final Player p, @NotNull final String s) {
        return p == null
                ? GlobalNameStyleProfile.INSTANCE.colorPalette().findColor(s)
                : GlobalNameStyleProfile.INSTANCE.findAccessibleNameColor(p, s);
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
        return availableNamecolors(GlobalNameStyleProfile.INSTANCE.accessibleNameColorsStylized(p));
    }

    @NotNull
    private static String availableNamecolors(@NotNull final Collection<Component> accessibleNameColorsStylized) {
        return String.join("&r ", accessibleNameColorsStylized
                .stream()
                .map(LegacyComponentSerializer.legacyAmpersand()::serialize)
                .toList());
    }

    @NotNull
    private static String commandText(@NotNull final String key) {
        return Optional.ofNullable(NameColorPlugin.config().getString("namecolor_command_texts." + key)).orElseThrow();
    }

}
