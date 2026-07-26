package fr.maxlego08.essentials.commands.commands.weather;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import org.bukkit.World;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for smooth time transitions in worlds and for players
 */
class TimeTransition {

    static final Set<UUID> TIME_CHANGING_WORLDS = ConcurrentHashMap.newKeySet();
    private static final long WORLD_TIME_STEP = 150;

    /**
     * Smoothly transitions world time to target time.
     * <p>
     * Each step is scheduled on the target world's region scheduler (FoliaLib) so it
     * works on Folia/regionised servers and when changing the time of a world other
     * than the command sender's current world.
     */
    static void transitionWorldTime(EssentialsPlugin plugin, World world, long targetTime) {
        UUID worldId = world.getUID();
        TIME_CHANGING_WORLDS.add(worldId);

        long startTime = world.getFullTime();
        long diff = (targetTime - startTime + 24000) % 24000;

        scheduleStep(plugin, world, worldId, targetTime, diff, 0);
    }

    private static void scheduleStep(EssentialsPlugin plugin, World world, UUID worldId, long targetTime, long diff, long progressed) {
        plugin.getScheduler().runLater(() -> {
            if (progressed >= diff) {
                world.setFullTime(targetTime);
                TIME_CHANGING_WORLDS.remove(worldId);
                return;
            }

            world.setFullTime(world.getFullTime() + WORLD_TIME_STEP);
            scheduleStep(plugin, world, worldId, targetTime, diff, progressed + WORLD_TIME_STEP);
        }, 1L);
    }

    /**
     * Checks if world time is currently being changed
     */
    static boolean isWorldTimeChanging(UUID worldId) {
        return TIME_CHANGING_WORLDS.contains(worldId);
    }
}
