package org.crayne.metacolor.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.tuple.Triple;
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

import java.util.*;

public class MetaColorCommand implements CommandExecutor {

    @NotNull
    private static final TextColor COLOR_RED = TextColor.color(255, 0, 0);

    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String label, @NotNull final String @NotNull [] args) {
        if (!(commandSender instanceof final Player p)) {
            commandSender.sendMessage(Component.text("Only players may use this command.").color(COLOR_RED));
            return false;
        }
        if (args.length == 0) {
            final Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    commandText("nc_info_page")
                            .replace("%ncname%", displayName(p))
                            .replace("%pt%", (int) MetaPTJDRequirement.playTimeInHours(p) + "")
                            .replace("%jd%", MetaPTJDRequirement.joinDateFormatted(p))
                            .replace("%jdd%", (int) MetaPTJDRequirement.joinDateInDays(p) + "")
                            .replace("%nclist%", availableNamecolors(p)));
            p.sendMessage(message);
            return true;
        }
        final Triple<Optional<Component>, List<MetaColorLike>, MetaDecoration> nameColorParsed = parseNameColor(args, p);
        final Optional<Component> errorMessage = nameColorParsed.getLeft();
        if (errorMessage.isPresent()) {
            p.sendMessage(errorMessage.get());
            return false;
        }
        final MetaNameColor oldNameColor = MetaColorPlugin.plugin().nameColor(p);
        final MetaNameColor newNameColor = nameColorOfParsed(nameColorParsed.getMiddle(), nameColorParsed.getRight(), oldNameColor);

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

    @NotNull
    public static Triple<Optional<Component>, List<MetaColorLike>, MetaDecoration> parseNameColor(@NotNull final String[] args, @Nullable final Player p) {
        if (args.length == 1 && args[0].equals("reset") && p != null) {
            MetaColorPlugin.plugin().nameColor(p, MetaColorPlugin.plugin().defaultNameColor());
            MetaColorPlugin.plugin().updateNameColor(p);

            return Triple.of(Optional.of(sendNameColorChanged(p)), null, null);
        }
        final List<Map.Entry<String, Optional<MetaColorLike>>> colorsChosen = Arrays.stream(args)
                .map(s -> Map.entry(s, p == null
                        ? MetaColorPlugin.plugin().colorPalette().findColor(s)
                        : MetaColorPlugin.plugin().findAvailableColor(p, s)))
                .toList();

        final Optional<Map.Entry<String, Optional<MetaColorLike>>> firstUnknown
                = colorsChosen.stream().filter(e -> e.getValue().isEmpty()).findFirst();

        if (firstUnknown.isPresent()) {
            final Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    commandText("nc_unknown_color")
                            .replace("%ncname%", p == null ? "Console" : displayName(p))
                            .replace("%nclist%", p == null
                                    ? availableNamecolors(MetaColorPlugin.plugin().allNameColorsStylized())
                                    : availableNamecolors(p))

                            .replace("%unknownnc%", firstUnknown.get().getKey()));
            return Triple.of(Optional.of(message), null, null);
        }
        final List<MetaColorLike> colorsChosenUnwrapped = colorsChosen.stream()
                .map(Map.Entry::getValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        final List<MetaColorLike> colors = colorsChosenUnwrapped
                .stream()
                .filter(c -> c instanceof RGB)
                .toList();

        final List<MetaDecoration> decorations = colorsChosenUnwrapped
                .stream()
                .filter(c -> c instanceof MetaDecoration)
                .map(c -> (MetaDecoration) c)
                .toList();

        final MetaDecoration decoration = MetaDecoration.combine(decorations);

        if (decoration.hasInvalidCombinations()) {
            final Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(commandText("nc_invalid_deco_combos"));
            return Triple.of(Optional.of(message), null, null);
        }
        final int maxColorCombos = MetaColorPlugin.config().getInt("max_namecolor_combinations");
        if (!decoration.hasSpecialDecorators() && colors.size() > 1) {
            final Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    commandText("nc_invalid_color_amount_"
                            + (p == null || MetaColorPlugin.plugin().canUseColorCombinations(p) ? "" : "no")
                            + "access_special_deco"));
            return Triple.of(Optional.of(message), null, null);
        }
        if (colors.size() > maxColorCombos) {
            final Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    commandText("nc_invalid_color_amount")
                            .replace("%maxnc%", maxColorCombos + ""));

            return Triple.of(Optional.of(message), null, null);
        }
        return Triple.of(Optional.empty(), colors, decoration);
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
