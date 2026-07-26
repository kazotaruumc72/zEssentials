package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.configuration.NonLoadable;
import fr.maxlego08.essentials.api.economy.Economy;
import fr.maxlego08.essentials.api.event.events.user.UserFirstJoinEvent;
import fr.maxlego08.essentials.api.event.events.user.UserQuitEvent;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.ZModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WelcomeModule extends ZModule {

    @NonLoadable
    private final Map<UUID, WelcomeSession> sessions = new ConcurrentHashMap<>();
    private long durationSeconds;
    private String rewardEconomy;
    private BigDecimal rewardAmount;
    private String rewardReason;
    private List<String> consoleCommands;

    public WelcomeModule(ZEssentialsPlugin plugin) {
        super(plugin, "welcome");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        this.sessions.clear();
    }

    @EventHandler
    public void onFirstJoin(UserFirstJoinEvent event) {
        if (!isEnable()) return;

        User user = event.getUser();
        long expiresAt = System.currentTimeMillis() + this.durationSeconds * 1000;
        this.sessions.put(user.getUniqueId(), new WelcomeSession(user.getName(), expiresAt, ConcurrentHashMap.newKeySet()));
    }

    @EventHandler
    public void onQuit(UserQuitEvent event) {
        this.sessions.remove(event.getUser().getUniqueId());
    }

    public void welcome(Player player) {

        long now = System.currentTimeMillis();
        this.sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);

        // The most recent new player the executor can still welcome
        Optional<Map.Entry<UUID, WelcomeSession>> optional = this.sessions.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(player.getUniqueId()))
                .filter(entry -> !entry.getValue().welcomedBy().contains(player.getUniqueId()))
                .max(Comparator.comparingLong(entry -> entry.getValue().expiresAt()));

        if (optional.isEmpty()) {

            if (this.sessions.containsKey(player.getUniqueId())) {
                message(player, Message.WELCOME_YOURSELF);
            } else if (this.sessions.values().stream().anyMatch(session -> session.welcomedBy().contains(player.getUniqueId()))) {
                message(player, Message.WELCOME_ALREADY);
            } else {
                message(player, Message.WELCOME_NO_PLAYER);
            }
            return;
        }

        WelcomeSession session = optional.get().getValue();
        session.welcomedBy().add(player.getUniqueId());
        String targetName = session.playerName();

        // Reward the player who welcomed the new player
        this.plugin.getEconomyManager().getEconomy(this.rewardEconomy).ifPresentOrElse(economy -> {
            User user = getUser(player);
            if (user == null) return;
            user.deposit(economy, this.rewardAmount, this.rewardReason.replace("%target%", targetName));
            message(player, Message.WELCOME_REWARD, "%amount%", this.plugin.getEconomyManager().format(economy, this.rewardAmount), "%target%", targetName);
        }, () -> this.plugin.getLogger().severe("Economy " + this.rewardEconomy + " was not found ! Check the welcome module configuration."));

        this.plugin.getScheduler().runNextTick(wrappedTask -> this.consoleCommands.forEach(command ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("%target%", targetName))));

        Bukkit.getOnlinePlayers().forEach(onlinePlayer ->
                message(onlinePlayer, Message.WELCOME_BROADCAST, "%player%", player.getName(), "%target%", targetName));
    }

    /**
     * A welcome window opened when a player joins the server for the first time.
     *
     * @param playerName the name of the new player
     * @param expiresAt  the timestamp in milliseconds when the window closes
     * @param welcomedBy the players who already welcomed the new player
     */
    private record WelcomeSession(String playerName, long expiresAt, Set<UUID> welcomedBy) {
    }
}
