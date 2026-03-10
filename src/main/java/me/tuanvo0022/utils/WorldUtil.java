package me.tuanvo0022.utils;

import me.tuanvo0022.Main;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class WorldUtil {
    private static Main plugin;
    
    public WorldUtil(Main plugin) {
        this.plugin = plugin;
    }
    
    public static Location findAvailableLocation(Collection<Location> locations) {
        Location best = null;
        int lowestPlayers = Integer.MAX_VALUE;

        for (Location loc : locations) {
            World world = loc.getWorld();
            if (world == null) continue;

            int currentPlayers = world.getPlayers().size();

            if (currentPlayers < lowestPlayers) {
                best = loc;
                lowestPlayers = currentPlayers;
            }
        }

        return best;
    }
    
    public static boolean isInWorlds(Player player, Collection<Location> locations) {
        World playerWorld = player.getWorld();
        return locations.stream()
                .map(Location::getWorld)
                .filter(Objects::nonNull)
                .anyMatch(w -> w.equals(playerWorld));
    }

    public static Set<World> getWorlds(Collection<Location> locations) {
        return locations.stream()
                .map(Location::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}