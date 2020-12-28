package com.willfp.ecoenchants.events.naturalexpgainevent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.HashSet;
import java.util.Set;

public class NaturalExpGainListeners implements Listener {
    private final Set<NaturalExpGainBuilder> events = new HashSet<>();

    @EventHandler
    public void playerChange(PlayerExpChangeEvent event) {
        NaturalExpGainBuilder builder = new NaturalExpGainBuilder(NaturalExpGainBuilder.BuildReason.PLAYER);
        builder.setEvent(event);

        NaturalExpGainBuilder toRemove = null;
        for (NaturalExpGainBuilder searchBuilder : events) {
            if(!searchBuilder.getLoc().getWorld().equals(event.getPlayer().getLocation().getWorld())) continue;
            if(searchBuilder.getReason().equals(NaturalExpGainBuilder.BuildReason.BOTTLE) && searchBuilder.getLoc().distanceSquared(event.getPlayer().getLocation()) > 52)
                toRemove = searchBuilder;
        }

        if(toRemove != null) {
            events.remove(toRemove);
            return;
        }

        builder.setEvent(event);
        builder.push();

        events.remove(builder);
    }

    @EventHandler
    public void onExpBottle(ExpBottleEvent event) {
        NaturalExpGainBuilder builtEvent = new NaturalExpGainBuilder(NaturalExpGainBuilder.BuildReason.BOTTLE);
        builtEvent.setLoc(event.getEntity().getLocation());

        events.add(builtEvent);
    }
}