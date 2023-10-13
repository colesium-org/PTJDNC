package org.crayne.ptjdnc.api.profile;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.crayne.ptjdnc.NameColorPlugin;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.NameStyle;
import org.crayne.ptjdnc.api.color.RGB;
import org.crayne.ptjdnc.api.color.namecolor.SingletonNameColor;
import org.crayne.ptjdnc.api.config.palette.ColorPalette;
import org.crayne.ptjdnc.api.config.ptjd.PlaytimeJoindatePalette;
import org.crayne.ptjdnc.api.config.whitelist.NameStyleWhitelist;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.crayne.ptjdnc.command.NameColorCommand;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GlobalNameStyleProfile {

    @NotNull
    public static final GlobalNameStyleProfile INSTANCE = new GlobalNameStyleProfile();

    @NotNull
    private NameStyle defaultNameStyle = new NameStyle(new SingletonNameColor(RGB.of(255, 255, 255)), NameDecoration.none());

    @NotNull
    private final ColorPalette colorPalette = new ColorPalette();

    @NotNull
    private final PlaytimeJoindatePalette ptjdPalette = new PlaytimeJoindatePalette();

    @NotNull
    private final Set<NameStyleWhitelist> whitelists = new HashSet<>();

    @NotNull
    protected final Map<UUID, NameStyleProfile> profiles = new HashMap<>();

    @NotNull
    private final GlobalProfileFile nameStyleFile = new GlobalProfileFile(this);

    public void save() {
        try {
            nameStyleFile.save();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        try {
            nameStyleFile.load();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        colorPalette.load();
        ptjdPalette.load(colorPalette);
        whitelists.addAll(NameStyleWhitelist.loadWhitelists(colorPalette));

        updateDefaultNameColor();
        NameStyleProfile.allowOpBypassRestrictions(NameColorPlugin.config().getBoolean("allow_op_bypass_restrictions"));
    }

    @NotNull
    public Map<UUID, NameStyle> createNameStyleMap() {
        return profiles.entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().nameStyle()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    @NotNull
    protected File nameColorFile() {
        return new File(NameColorPlugin.plugin().getDataFolder(), "namecolors.nci");
    }

    @NotNull
    public PlaytimeJoindatePalette ptjdPalette() {
        return ptjdPalette;
    }

    @NotNull
    public Set<NameStyleWhitelist> whitelists() {
        return whitelists;
    }

    private void updateDefaultNameColor() {
        final String defaultNameColorSetting = NameColorPlugin.config().getString("default_namecolor");
        if (defaultNameColorSetting == null || defaultNameColorSetting.isBlank()) return;

        final NameColorCommand.NameColorCommandResult parsedDefaultNameColor
                = NameColorCommand.parseNameColor(defaultNameColorSetting.split(" "), null, defaultNameStyle);

        final Component errorMessage = parsedDefaultNameColor.message();
        if (errorMessage != null) {
            NameColorPlugin.plugin().getLogger().severe("Could not set default namecolor value. Defaulting to white instead.");
            Bukkit.getConsoleSender().sendMessage(errorMessage);
            return;
        }
        defaultNameStyle = parsedDefaultNameColor.toMetaNameColor();
    }

    public void updateNameStyle(@NotNull final Player player) {
        final NameStyle nameStyle = nameStyle(player);
        player.displayName(nameStyle.stylize(player.name()));
    }

    @NotNull
    public NameStyle defaultNameColor() {
        return defaultNameStyle;
    }

    @NotNull
    public Collection<Component> allNameColorsStylized() {
        return colorPalette.colorsStylized().values();
    }

    @NotNull
    public ColorPalette colorPalette() {
        return colorPalette;
    }

    @NotNull
    private Optional<NameStyleProfile> findExistingProfile(@NotNull final UUID uuid) {
        return Optional.ofNullable(profiles.get(uuid));
    }

    @NotNull
    private NameStyleProfile findProfileOrCreateDefault(@NotNull final UUID uuid) {
        final Optional<NameStyleProfile> existingNameStyleProfile = findExistingProfile(uuid);
        if (existingNameStyleProfile.isPresent()) return existingNameStyleProfile.get();

        final NameStyleProfile profile = new NameStyleProfile(uuid, defaultNameStyle);
        profiles.put(uuid, profile);
        return profile;
    }

    @NotNull
    public NameStyle nameStyle(@NotNull final UUID uuid) {
        return findProfileOrCreateDefault(uuid).nameStyle();
    }

    public void nameStyle(@NotNull final UUID uuid, @NotNull final NameStyle newNameStyle) {
        findProfileOrCreateDefault(uuid).nameStyle(newNameStyle);
    }

    @NotNull
    public NameStyle nameStyle(@NotNull final Player player) {
        return nameStyle(player.getUniqueId());
    }

    public void nameStyle(@NotNull final Player player, @NotNull final NameStyle newNameStyle) {
        nameStyle(player.getUniqueId(), newNameStyle);
    }

    public boolean nameColorAccessible(@NotNull final UUID uuid, @NotNull final String colorName) {
        return findProfileOrCreateDefault(uuid).nameColorAccessible(colorName);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean nameColorAccessible(@NotNull final Player player, @NotNull final String colorName) {
        return findProfileOrCreateDefault(player.getUniqueId()).nameColorAccessible(colorName, player);
    }

    @NotNull
    public Map<String, ColorLike> accessibleNameColors(@NotNull final UUID uuid) {
        return findProfileOrCreateDefault(uuid).accessibleNameColors();
    }

    @NotNull
    public Map<String, ColorLike> accessibleNameColors(@NotNull final Player player) {
        return accessibleNameColors(player.getUniqueId());
    }

    @NotNull
    public List<Component> accessibleNameColorsStylized(@NotNull final UUID uuid) {
        return findProfileOrCreateDefault(uuid).accessibleNameColorsStylized();
    }

    @NotNull
    public List<Component> accessibleNameColorsStylized(@NotNull final Player player) {
        return accessibleNameColorsStylized(player.getUniqueId());
    }

    public boolean combiningModifiersAccessible(@NotNull final UUID uuid) {
        return findProfileOrCreateDefault(uuid).combiningModifiersAccessible();
    }

    public boolean combiningModifiersAccessible(@NotNull final Player player) {
        return combiningModifiersAccessible(player.getUniqueId());
    }

    @NotNull
    public Optional<ColorLike> findAccessibleNameColor(@NotNull final UUID uuid, @NotNull final String colorName) {
        return findProfileOrCreateDefault(uuid).findAvailableColor(colorName);
    }

    @NotNull
    public Optional<ColorLike> findAccessibleNameColor(@NotNull final Player player, @NotNull final String colorName) {
        return findAccessibleNameColor(player.getUniqueId(), colorName);
    }

    public void forceUpdate(@NotNull final Player player) {
        forceUpdate(player.getUniqueId());
    }

    public void forceUpdate(@NotNull final UUID uuid) {
        findProfileOrCreateDefault(uuid).forceUpdate();
    }

}
