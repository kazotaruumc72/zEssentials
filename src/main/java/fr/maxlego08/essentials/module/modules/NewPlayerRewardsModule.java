package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.configuration.NonLoadable;
import fr.maxlego08.essentials.api.event.events.user.UserFirstJoinEvent;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.ZModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NewPlayerRewardsModule extends ZModule {

    @NonLoadable
    private final List<RewardTier> tiers = new ArrayList<>();

    public NewPlayerRewardsModule(ZEssentialsPlugin plugin) {
        super(plugin, "new_player_rewards");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        this.tiers.clear();

        for (Map<?, ?> rawMap : getConfiguration().getMapList("tiers")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;

            long until = ((Number) map.getOrDefault("until", 0)).longValue();
            String economy = (String) map.get("economy");
            BigDecimal amount = new BigDecimal(String.valueOf(map.getOrDefault("amount", "0")));

            @SuppressWarnings("unchecked")
            List<String> commands = map.get("commands") != null ? (List<String>) map.get("commands") : new ArrayList<>();

            this.tiers.add(new RewardTier(until, economy, amount, commands));
        }

        this.tiers.sort(Comparator.comparingLong(RewardTier::until));
    }

    @EventHandler
    public void onFirstJoin(UserFirstJoinEvent event) {
        if (!isEnable()) return;

        User user = event.getUser();
        Player player = user.getPlayer();
        if (player == null) return;

        long rank = getStorage().totalUsers();

        Optional<RewardTier> optional = this.tiers.stream().filter(tier -> rank <= tier.until()).findFirst();
        if (optional.isEmpty()) return;

        RewardTier tier = optional.get();
        String rankString = String.valueOf(rank);

        if (tier.economy() != null && tier.amount().compareTo(BigDecimal.ZERO) > 0) {
            this.plugin.getEconomyManager().getEconomy(tier.economy()).ifPresentOrElse(economy -> {
                user.deposit(economy, tier.amount(), "New player reward (player #" + rank + ")");
                message(player, Message.NEW_PLAYER_REWARDS_RECEIVED, "%rank%", rankString, "%reward%", this.plugin.getEconomyManager().format(economy, tier.amount()));
            }, () -> this.plugin.getLogger().severe("Economy " + tier.economy() + " was not found ! Check the new_player_rewards module configuration."));
        }

        if (!tier.commands().isEmpty()) {
            this.plugin.getScheduler().runNextTick(wrappedTask -> tier.commands().forEach(command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("%rank%", rankString))));
        }
    }

    /**
     * A reward tier for new players.
     *
     * @param until    the tier applies to new players whose rank is lower or equal to this value
     * @param economy  the name of the economy given to the new player, can be null
     * @param amount   the amount of the economy given to the new player
     * @param commands the console commands executed for the new player
     */
    private record RewardTier(long until, String economy, BigDecimal amount, List<String> commands) {
    }
}
