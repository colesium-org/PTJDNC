package org.crayne.ptjdnc.api.config.ptjd;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.crayne.ptjdnc.api.config.ConfigParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaytimeJoindateRequirement {

    private final int minimumHoursPlayed;
    private final int minimumDaysJoinedAgo;

    @Nullable
    private final Date latestPossibleJoinDate;

    public PlaytimeJoindateRequirement(final int minimumHoursPlayed, final int minimumDaysJoinedAgo) {
        this.minimumHoursPlayed = minimumHoursPlayed;
        this.minimumDaysJoinedAgo = minimumDaysJoinedAgo;
        this.latestPossibleJoinDate = null;
    }

    public PlaytimeJoindateRequirement(final int minimumHoursPlayed, @NotNull final Date latestPossibleJoinDate) {
        this.minimumHoursPlayed = minimumHoursPlayed;
        this.minimumDaysJoinedAgo = -1;
        this.latestPossibleJoinDate = latestPossibleJoinDate;
    }

    @NotNull
    private static final DateFormat parseDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    @NotNull
    public static PlaytimeJoindateRequirement parseRequirement(@NotNull final String s) {
        if (!s.contains(",")) throw new ConfigParseException("Expected comma at playtime + joindate requirement (" + s + ")");

        try {
            final int minimumHoursPlayed = Integer.parseInt(StringUtils.substringBefore(s, ","));
            final String joinDateRequirement = StringUtils.substringAfter(s, ",");
            final boolean minimumSpecified = !joinDateRequirement.startsWith("D");
            if (minimumSpecified) return new PlaytimeJoindateRequirement(minimumHoursPlayed, Integer.parseInt(joinDateRequirement));

            final Date latestPossibleJoinDate = parseDateFormat.parse(joinDateRequirement.substring(1));
            return new PlaytimeJoindateRequirement(minimumHoursPlayed, latestPossibleJoinDate);
        } catch (final Exception e) {
            throw new ConfigParseException(e);
        }
    }

    public static double joinDateInDays(@NotNull final OfflinePlayer player) {
        return (int) ((System.currentTimeMillis() - player.getFirstPlayed()) / 1000L) / (24.0d * 60.0d * 60.0d);
    }

    @NotNull
    public static Date joinDate(@NotNull final OfflinePlayer player) {
        return new Date(player.getFirstPlayed());
    }

    @NotNull
    public static String joinDateFormatted(@NotNull final OfflinePlayer player) {
        return parseDateFormat.format(joinDate(player));
    }

    public static double playTimeInHours(@NotNull final OfflinePlayer player) {
        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20.0d * 60.0d * 60.0d);
    }

    public boolean allows(final double joinDateDays, final double playtimeHours) {
        if (minimumDaysJoinedAgo == -1) return false;

        final boolean hasPlaytimeRequirement = playtimeHours >= minimumHoursPlayed;
        if (latestPossibleJoinDate != null) {
            final Date today = new Date();
            return hasPlaytimeRequirement && today.before(latestPossibleJoinDate);
        }
        return hasPlaytimeRequirement && joinDateDays >= minimumDaysJoinedAgo;
    }

}
