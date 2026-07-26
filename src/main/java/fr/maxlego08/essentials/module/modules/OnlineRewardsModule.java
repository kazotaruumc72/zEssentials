package fr.maxlego08.essentials.module.modules;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.configuration.NonLoadable;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.ZModule;
import fr.maxlego08.essentials.zutils.utils.TimerBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OnlineRewardsModule extends ZModule {

    private long durationSeconds;
    private String staffPermission;
    private boolean resetOnQuit;
    private boolean resetWhenBelow;

    @NonLoadable
    private final List<OnlineMilestone> milestones = new ArrayList<>();
    @NonLoadable
    private final Map<Long, WrappedTask> pendingTasks = new HashMap<>();
    @NonLoadable
    private final Set<Long> validatedMilestones = new HashSet<>();

    public OnlineRewardsModule(ZEssentialsPlugin plugin) {
        super(plugin, "online_rewards");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        this.pendingTasks.values().forEach(WrappedTask::cancel);
        this.pendingTasks.clear();
        this.validatedMilestones.clear();
        this.milestones.clear();

        for (Map<?, ?> rawMap : getConfiguration().getMapList("milestones")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;

            long players = ((Number) map.getOrDefault("players", 0)).longValue();

            @SuppressWarnings("unchecked")
            List<String> commands = map.get("commands") != null ? (List<String>) map.get("commands") : new ArrayList<>();

            if (players > 0) this.milestones.add(new OnlineMilestone(players, commands));
        }

        this.milestones.sort(Comparator.comparingLong(OnlineMilestone::players));

        // Handles /reload while players are already connected
        if (this.isEnable && !this.milestones.isEmpty()) {
            this.plugin.getScheduler().runNextTick(wrappedTask -> checkMilestones());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!isEnable()) return;

        checkMilestones();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!isEnable()) return;

        if (this.resetOnQuit && isCounted(event.getPlayer()) && !this.pendingTasks.isEmpty()) {
            this.pendingTasks.forEach((players, task) -> {
                task.cancel();
                broadcast(Message.ONLINE_REWARDS_CANCEL, "%players%", String.valueOf(players));
            });
            this.pendingTasks.clear();
        }

        // The quitting player is still online during the event, recount on the next tick
        this.plugin.getScheduler().runNextTick(wrappedTask -> checkMilestones());
    }

    private void checkMilestones() {
        long count = countedPlayers();

        // Milestones can be earned again once the player count drops below them
        if (this.resetWhenBelow) {
            this.validatedMilestones.removeIf(players -> count < players);
        }

        // Cancel countdowns whose milestone is no longer reached
        this.pendingTasks.entrySet().removeIf(entry -> {
            if (count >= entry.getKey()) return false;
            entry.getValue().cancel();
            broadcast(Message.ONLINE_REWARDS_CANCEL, "%players%", String.valueOf(entry.getKey()));
            return true;
        });

        for (OnlineMilestone milestone : this.milestones) {
            long players = milestone.players();
            if (count < players || this.validatedMilestones.contains(players) || this.pendingTasks.containsKey(players)) continue;

            broadcast(Message.ONLINE_REWARDS_START, "%players%", String.valueOf(players), "%duration%", TimerBuilder.getStringTime(this.durationSeconds * 1000));
            WrappedTask task = this.plugin.getScheduler().runLater(() -> validate(milestone), this.durationSeconds, TimeUnit.SECONDS);
            this.pendingTasks.put(players, task);
        }
    }

    private void validate(OnlineMilestone milestone) {
        this.pendingTasks.remove(milestone.players());
        this.validatedMilestones.add(milestone.players());

        String playersString = String.valueOf(milestone.players());
        for (Player player : Bukkit.getOnlinePlayers()) {
            milestone.commands().forEach(command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("%players%", playersString)));
        }
        broadcast(Message.ONLINE_REWARDS_VALIDATED, "%players%", playersString);
    }

    private long countedPlayers() {
        return Bukkit.getOnlinePlayers().stream().filter(this::isCounted).count();
    }

    private boolean isCounted(Player player) {
        return this.staffPermission == null || this.staffPermission.isEmpty() || !player.hasPermission(this.staffPermission);
    }

    /**
     * A reward milestone based on the number of players connected at the same time.
     *
     * @param players  the number of non-staff players that must stay connected
     * @param commands the console commands executed for each online player once the milestone is validated
     */
    private record OnlineMilestone(long players, List<String> commands) {
    }
}
