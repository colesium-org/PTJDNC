package org.crayne.ptjdnc.api.profile;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.NameStyle;
import org.crayne.ptjdnc.api.config.palette.ColorPalette;
import org.crayne.ptjdnc.api.config.ptjd.PlaytimeJoindatePalette;
import org.crayne.ptjdnc.api.config.ptjd.PlaytimeJoindateRequirement;
import org.crayne.ptjdnc.api.config.whitelist.NameStyleWhitelist;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class NameStyleProfile {

    @NotNull
    private final UUID uuid;

    @NotNull
    private NameStyle nameStyle;

    @Nullable
    private Date joinDate;

    private double playTimeHours, joinDateDays;

    private long lastUpdated;

    @NotNull
    private final Map<String, ColorLike> accessibleNameColors = new LinkedHashMap<>();

    @NotNull
    private final List<Component> accessibleNameColorsStylized = new ArrayList<>();

    private static boolean allowOpBypassRestrictions;

    public NameStyleProfile(@NotNull final UUID uuid, @NotNull final NameStyle nameStyle) {
        this.uuid = uuid;
        this.nameStyle = nameStyle;
        forceUpdate();
    }

    public static boolean allowOpBypassRestrictions() {
        return allowOpBypassRestrictions;
    }

    public static void allowOpBypassRestrictions(final boolean allowOpBypassRestrictions) {
        NameStyleProfile.allowOpBypassRestrictions = allowOpBypassRestrictions;
    }

    private static long currentTimeMillisMonotonic() {
        return System.nanoTime() / 1000000;
    }

    public void forceUpdate() {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        forceUpdate(player);
    }

    public void forceUpdate(@NotNull final OfflinePlayer player) {
        this.joinDateDays = PlaytimeJoindateRequirement.joinDateInDays(player);
        this.joinDate = PlaytimeJoindateRequirement.joinDate(player);
        this.playTimeHours = PlaytimeJoindateRequirement.playTimeInHours(player);
        this.lastUpdated = currentTimeMillisMonotonic();
        loadAccessibleNameColors(player);
    }

    public void update() {
        if (currentTimeMillisMonotonic() - lastUpdated > 3600000) forceUpdate();
    }

    public double playtime() {
        update();
        return playTimeHours;
    }

    public double joinDateDays() {
        return joinDateDays;
    }

    @NotNull
    public NameStyle nameStyle() {
        return nameStyle;
    }

    @NotNull
    public UUID uuid() {
        return uuid;
    }

    @NotNull
    public Optional<Date> joinDate() {
        return Optional.ofNullable(joinDate);
    }

    public boolean playtimeJoindateInfoLoaded() {
        return joinDate().isPresent();
    }

    public void nameStyle(@NotNull final NameStyle nameStyle) {
        this.nameStyle = nameStyle;
    }

    private void loadAccessibleNameColors(@NotNull final OfflinePlayer player) {
        if (!playtimeJoindateInfoLoaded()) return;

        final GlobalNameStyleProfile globalNameStyleProfile = GlobalNameStyleProfile.INSTANCE;

        final PlaytimeJoindatePalette ptjdPalette = globalNameStyleProfile.ptjdPalette();
        final Set<NameStyleWhitelist> whitelists  = globalNameStyleProfile.whitelists();
        final ColorPalette colorPalette = globalNameStyleProfile.colorPalette();

        accessibleNameColors.clear();
        accessibleNameColorsStylized.clear();

        colorPalette
                .keys()
                .stream()
                .filter(colorName -> (player.isOp() && allowOpBypassRestrictions()) || nameColorAccessible(colorName, ptjdPalette, whitelists))
                .forEach(colorName -> {
                    accessibleNameColors.put(colorName, colorPalette.findColor(colorName).orElseThrow());
                    accessibleNameColorsStylized.add(colorPalette.colorsStylized().get(colorName));
                });
    }

    public boolean nameColorAccessible(@NotNull final String colorName,
                                           @NotNull final PlaytimeJoindatePalette ptjdPalette,
                                           @NotNull final Set<NameStyleWhitelist> whitelists) {

        return ptjdPalette.nameColorRequirement(colorName).orElseThrow().allows(joinDateDays, playTimeHours)
                || whitelists.stream().anyMatch(whitelist -> whitelist.nameColorAccessible(uuid, colorName));
    }

    public boolean nameColorAccessible(@NotNull final String colorName) {
        return nameColorAccessible(colorName, Bukkit.getOfflinePlayer(uuid));
    }

    public boolean nameColorAccessible(@NotNull final String colorName, @NotNull final OfflinePlayer player) {
        return (player.isOp() && allowOpBypassRestrictions()) || accessibleNameColors.containsKey(colorName);
    }

    @NotNull
    public Map<String, ColorLike> accessibleNameColors() {
        return accessibleNameColors;
    }

    @NotNull
    public List<Component> accessibleNameColorsStylized() {
        return accessibleNameColorsStylized;
    }

    @NotNull
    public Optional<ColorLike> findAvailableColor(@NotNull final String name) {
        return Optional.ofNullable(accessibleNameColors.get(name));
    }

    public boolean combiningModifiersAccessible() {
        return Stream.of("gradient", "flag", "alternating").anyMatch(this::nameColorAccessible);
    }

}
