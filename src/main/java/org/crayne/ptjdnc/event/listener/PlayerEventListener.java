package org.crayne.ptjdnc.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.crayne.ptjdnc.NameColorPlugin;
import org.crayne.ptjdnc.api.profile.GlobalNameStyleProfile;
import org.crayne.ptjdnc.event.PlayerOpStatusChangeEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerEventListener implements Listener {

    @EventHandler
    public void joinEvent(@NotNull final PlayerJoinEvent ev) {
        final Player player = ev.getPlayer();
        GlobalNameStyleProfile.INSTANCE.forceUpdate(player);
        GlobalNameStyleProfile.INSTANCE.updateNameStyle(player);
    }

    @EventHandler
    public void quitEvent(@NotNull final PlayerQuitEvent ev) {
        NameColorPlugin.plugin().removeTrackedPlayer(ev.getPlayer().getUniqueId());
    }

    @EventHandler
    public void opEvent(@NotNull final PlayerOpStatusChangeEvent ev) {
        GlobalNameStyleProfile.INSTANCE.forceUpdate(ev.getPlayer());
    }

}
