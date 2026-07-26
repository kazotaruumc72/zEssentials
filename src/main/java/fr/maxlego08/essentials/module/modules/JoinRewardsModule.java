package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.configuration.NonLoadable;
import fr.maxlego08.essentials.api.event.events.user.UserFirstJoinEvent;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.ZModule;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.requirement.Action;
import fr.maxlego08.menu.api.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinRewardsModule extends ZModule {

    @NonLoadable
    private final List<JoinRewardMilestone> milestones = new ArrayList<>();

    public JoinRewardsModule(ZEssentialsPlugin plugin) {
        super(plugin, "join_rewards");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        this.milestones.clear();

        var buttonManager = this.plugin.getButtonManager();
        var configuration = getConfiguration();
        var file = new File(getFolder(), "config.yml");

        for (Map<?, ?> rawMap : configuration.getMapList("milestones")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;

            long playerCount = ((Number) map.getOrDefault("players", 0)).longValue();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> winnerRaw = (List<Map<String, Object>>) map.get("winner-actions");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allRaw = (List<Map<String, Object>>) map.get("all-players-actions");

            List<Action> winnerActions = winnerRaw != null
                    ? buttonManager.loadActions(winnerRaw, "milestones.winner-actions", file)
                    : new ArrayList<>();

            List<Action> allPlayersActions = allRaw != null
                    ? buttonManager.loadActions(allRaw, "milestones.all-players-actions", file)
                    : new ArrayList<>();

            this.milestones.add(new JoinRewardMilestone(playerCount, winnerActions, allPlayersActions));
        }
    }

    @EventHandler
    public void onFirstJoin(UserFirstJoinEvent event) {
        if (!isEnable()) return;

        User user = event.getUser();
        Player player = user.getPlayer();
        if (player == null) return;

        long totalUsers = this.plugin.getStorageManager().getStorage().totalUsers();

        for (JoinRewardMilestone milestone : this.milestones) {
            if (totalUsers != milestone.players()) continue;

            InventoryEngine fakeInventory = this.plugin.getInventoryManager().getFakeInventory();
            String playerName = player.getName();
            String countStr = String.valueOf(totalUsers);

            // Reward for the milestone player (option 1)
            if (!milestone.winnerActions().isEmpty()) {
                milestone.winnerActions().forEach(action ->
                        action.preExecute(player, null, fakeInventory, new Placeholders()));
                message(player, Message.JOIN_REWARDS_MILESTONE_WINNER,
                        "%player%", playerName, "%count%", countStr);
            }

            // Reward for all currently connected players (option 2)
            if (!milestone.allPlayersActions().isEmpty()) {
                Bukkit.getOnlinePlayers().forEach(online ->
                        milestone.allPlayersActions().forEach(action ->
                                action.preExecute(online, null, fakeInventory, new Placeholders())));
                Bukkit.getOnlinePlayers().forEach(online ->
                        message(online, Message.JOIN_REWARDS_MILESTONE_ALL,
                                "%player%", playerName, "%count%", countStr));
            }
        }
    }

    private record JoinRewardMilestone(long players, List<Action> winnerActions, List<Action> allPlayersActions) {}
}
