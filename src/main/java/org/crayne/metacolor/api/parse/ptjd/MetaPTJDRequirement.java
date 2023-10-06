package org.crayne.metacolor.api.parse.ptjd;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.crayne.metacolor.api.parse.MetaParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MetaPTJDRequirement {

    private final int minimumHoursPlayed;
    private final int minimumDaysJoinedAgo;

    @Nullable
    private final Date latestPossibleJoinDate;

    public MetaPTJDRequirement(final int minimumHoursPlayed, final int minimumDaysJoinedAgo) {
        this.minimumHoursPlayed = minimumHoursPlayed;
        this.minimumDaysJoinedAgo = minimumDaysJoinedAgo;
        this.latestPossibleJoinDate = null;
    }

    public MetaPTJDRequirement(final int minimumHoursPlayed, @NotNull final Date latestPossibleJoinDate) {
        this.minimumHoursPlayed = minimumHoursPlayed;
        this.minimumDaysJoinedAgo = -1;
        this.latestPossibleJoinDate = latestPossibleJoinDate;
    }

    @NotNull
    private static final DateFormat parseDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    @NotNull
    public static MetaPTJDRequirement parseRequirement(@NotNull final String s) {
        if (!s.contains(",")) throw new MetaParseException("Expected comma at playtime + joindate requirement (" + s + ")");

        try {
            final int minimumHoursPlayed = Integer.parseInt(StringUtils.substringBefore(s, ","));
            final String joinDateRequirement = StringUtils.substringAfter(s, ",");
            final boolean minimumSpecified = !joinDateRequirement.startsWith("D");
            if (minimumSpecified) return new MetaPTJDRequirement(minimumHoursPlayed, Integer.parseInt(joinDateRequirement));

            final Date latestPossibleJoinDate = parseDateFormat.parse(joinDateRequirement.substring(1));
            return new MetaPTJDRequirement(minimumHoursPlayed, latestPossibleJoinDate);
        } catch (final Exception e) {
            throw new MetaParseException(e);
        }
    }

    public static double joinDateInDays(@NotNull final Player player) {
        return (int) ((System.currentTimeMillis() - player.getFirstPlayed()) / 1000L) / (24.0d * 60.0d * 60.0d);
    }

    public static Date joinDate(@NotNull final Player player) {
        return new Date(player.getFirstPlayed());
    }

    public static String joinDateFormatted(@NotNull final Player player) {
        return parseDateFormat.format(joinDate(player));
    }

    public static double playTimeInHours(@NotNull final Player player) {
        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20.0d * 60.0d * 60.0d);
    }

    public boolean allows(@NotNull final Player player) {
        if (minimumDaysJoinedAgo == -1) return false;

        final boolean hasPlaytimeRequirement = playTimeInHours(player) >= minimumHoursPlayed;
        if (latestPossibleJoinDate != null) {
            final Date today = new Date();
            return hasPlaytimeRequirement && today.before(latestPossibleJoinDate);
        }
        return hasPlaytimeRequirement && joinDateInDays(player) >= minimumDaysJoinedAgo;
    }

}
