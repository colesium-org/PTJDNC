package org.crayne.ptjdnc.command;

import com.jcabi.aspects.Cacheable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.crayne.ptjdnc.api.profile.GlobalNameStyleProfile;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class NameColorTabCompleter implements TabCompleter {

    @Cacheable
    @NotNull
    public Collection<String> colors() {
        return GlobalNameStyleProfile.INSTANCE.colorPalette().colorPalette().keySet();
    }

    @Cacheable
    @NotNull
    public List<String> accessibleDecorations(@NotNull final Player p) {
        return GlobalNameStyleProfile.INSTANCE
                .colorPalette()
                .decorationPalette()
                .keySet()
                .stream()
                .filter(d -> GlobalNameStyleProfile.INSTANCE.nameColorAccessible(p, d))
                .toList();
    }

    @NotNull
    private static List<NameDecoration> findDecorations(@NotNull final List<String> argsList) {
        return argsList.stream()
                .map(s -> GlobalNameStyleProfile.INSTANCE
                        .colorPalette()
                        .decorationPalette()
                        .get(s))
                .filter(Objects::nonNull)
                .toList();
    }

    private static boolean validateExistingArguments(@NotNull final List<String> argsList, @NotNull final List<String> availableNameColors) {
        for (int i = 0; i < argsList.size(); i++) {
            final String s = argsList.get(i);
            if (s.isBlank()) continue;

            if (s.equals("rgb") && availableNameColors.contains("rgb")) {
                i += 3;
                if (i >= argsList.size()) return true;
                continue;
            }
            if (s.equals("hex") && availableNameColors.contains("hex")) {
                i++;
                if (i >= argsList.size()) return true;
                continue;
            }
            if (!availableNameColors.contains(s)) return true;
        }
        return false;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String label, @NotNull final String @NotNull [] args) {
        if (!(commandSender instanceof final Player p)) return null;

        final List<String> argsList = Arrays.stream(args).toList().subList(0, args.length - 1);
        if (argsList.contains("reset")) return new ArrayList<>();

        final List<NameDecoration> decorations = findDecorations(argsList);
        final NameDecoration decoration = NameDecoration.combine(decorations);

        final GlobalNameStyleProfile globalNameStyleProfile = GlobalNameStyleProfile.INSTANCE;

        final List<String> availableNameColors = new ArrayList<>(globalNameStyleProfile.accessibleNameColors(p).keySet());
        if (argsList.isEmpty()) {
            availableNameColors.add(0, "reset");
            availableNameColors.addAll(accessibleDecorations(p));
        }
        final List<String> accessibleCustomNameColors = Stream.of("rgb", "hex").
                filter(s -> globalNameStyleProfile.nameColorAccessible(p, s))
                .toList();

        availableNameColors.addAll(accessibleCustomNameColors);
        if (validateExistingArguments(argsList, availableNameColors)) return new ArrayList<>();

        if (argsList.stream().anyMatch(colors()::contains) && !argsList.contains("gradient") && !argsList.contains("flag") && !argsList.contains("alternating"))
            availableNameColors.removeAll(colors());

        if (argsList.contains("gradient")) availableNameColors.removeAll(List.of("flag", "alternating"));
        if (argsList.contains("alternating")) availableNameColors.removeAll(List.of("flag", "gradient"));
        if (argsList.contains("flag")) availableNameColors.removeAll(List.of("gradient", "alternating"));

        availableNameColors.removeAll(decoration.colors());

        return availableNameColors;
    }

}
