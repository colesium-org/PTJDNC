package org.crayne.metacolor.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.crayne.metacolor.MetaColorPlugin;
import org.crayne.metacolor.api.parse.ptjd.MetaPTJDRequirement;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
        // TODO let players change their nc using /nc <color>
        // make sure that gradient, flag and alternating are never true simultaneously (only one can be true of the three)
        // toggle decorators like bold, italic, etc but make color switch instead (allow choosing multiple colors, gradient, flag or alternating are enabled)
        // TODO add a max amount of color combos
        return false;
    }

    @NotNull
    private static String displayName(@NotNull final Player p) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(p.displayName());
    }

    @NotNull
    private static String availableNamecolors(@NotNull final Player p) {
        return String.join("&r ", MetaColorPlugin.plugin()
                .accessibleNameColorsStylized(p)
                .stream()
                .map(LegacyComponentSerializer.legacyAmpersand()::serialize)
                .toList());
    }

    @NotNull
    private static String commandText(@NotNull final String key) {
        return Optional.ofNullable(MetaColorPlugin.config().getString("namecolor_command_texts." + key)).orElseThrow();
    }

}
