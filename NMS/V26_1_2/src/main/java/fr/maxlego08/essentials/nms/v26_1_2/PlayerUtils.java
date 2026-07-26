package fr.maxlego08.essentials.nms.v26_1_2;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.nms.PlayerUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerUtils implements PlayerUtil {

    private final EssentialsPlugin plugin;

    public PlayerUtils(EssentialsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean openEnderChest(Player player, OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean openPlayerInventory(Player player, OfflinePlayer offlinePlayer) {
        return false;
    }

}