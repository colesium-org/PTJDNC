package org.crayne.ptjdnc.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerOpStatusChangeEvent extends PlayerEvent {

    private final boolean previousOpStatus, newOpStatus;

    public PlayerOpStatusChangeEvent(@NotNull final Player who, final boolean previousOpStatus, final boolean newOpStatus, final boolean async) {
        super(who, async);
        this.previousOpStatus = previousOpStatus;
        this.newOpStatus = newOpStatus;
    }

    public boolean newOpStatus() {
        return newOpStatus;
    }

    public boolean previousOpStatus() {
        return previousOpStatus;
    }

    @NotNull
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
