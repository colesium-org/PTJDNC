package org.crayne.metacolor.command;

import com.jcabi.aspects.Cacheable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.crayne.metacolor.MetaColorPlugin;
import org.crayne.metacolor.api.style.MetaDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaColorTabCompleter implements TabCompleter {

    @Cacheable
    @NotNull
    public Collection<String> colors() {
        return MetaColorPlugin.plugin().colorPalette().colorPalette().keySet();
    }

    @Cacheable
    @NotNull
    public List<String> accessibleDecorations(@NotNull final Player p) {
        return MetaColorPlugin.plugin()
                .colorPalette()
                .decorationPalette()
                .keySet()
                .stream()
                .filter(d -> MetaColorPlugin.plugin().nameColorAccessible(p, d))
                .toList();
    }

    @Nullable
    public List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String label, @NotNull final String @NotNull [] args) {
        if (!(commandSender instanceof final Player p)) return null;

        final List<String> argsList = Arrays.stream(args).toList().subList(0, args.length - 1);
        if (argsList.contains("reset")) return new ArrayList<>();
        final List<MetaDecoration> decorations = argsList.stream()
                .map(s -> MetaColorPlugin.plugin()
                        .colorPalette()
                        .decorationPalette()
                        .get(s))
                .filter(Objects::nonNull)
                .toList();

        final MetaDecoration decoration = MetaDecoration.combine(decorations);

        final List<String> availableNameColors = new ArrayList<>(MetaColorPlugin.plugin().accessibleNameColors(p));
        if (argsList.isEmpty()) {
            availableNameColors.add(0, "reset");
            availableNameColors.addAll(accessibleDecorations(p));
        }
        if (argsList.stream().filter(s -> !s.isBlank()).anyMatch(s -> !availableNameColors.contains(s))) return new ArrayList<>();

        if (argsList.stream().anyMatch(colors()::contains) && !argsList.contains("gradient") && !argsList.contains("flag") && !argsList.contains("alternating"))
            availableNameColors.removeAll(colors());

        if (argsList.contains("gradient")) availableNameColors.removeAll(List.of("flag", "alternating"));
        if (argsList.contains("alternating")) availableNameColors.removeAll(List.of("flag", "gradient"));
        if (argsList.contains("flag")) availableNameColors.removeAll(List.of("gradient", "alternating"));

        availableNameColors.removeAll(decoration.colors());

        return availableNameColors;
    }

}
