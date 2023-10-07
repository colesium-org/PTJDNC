package org.crayne.metacolor.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.crayne.metacolor.MetaColorPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinEventListener implements Listener {

    @EventHandler
    public void joinEvent(@NotNull final PlayerJoinEvent ev) {
        MetaColorPlugin.plugin().updateNameColor(ev.getPlayer());
    }

}
