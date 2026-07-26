package fr.maxlego08.essentials.api.vault;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Bridge that lets an external plugin (e.g. a shop/sell plugin) sell items taken
 * from a player's vault.
 *
 * <p>zEssentials has no knowledge of any specific sell system: an external plugin
 * implements this interface and registers it through the Bukkit
 * {@link org.bukkit.plugin.ServicesManager}. zEssentials looks it up lazily when a
 * player middle-clicks a vault item (see {@code VaultManager#getVaultSellHook()}).
 */
public interface VaultSellHook {

    /**
     * Checks whether the given item has a sale value in the external system.
     *
     * @param item the item to check
     * @return {@code true} if the item can be sold, {@code false} otherwise
     */
    boolean canSell(ItemStack item);

    /**
     * Opens the external sell interface for the player, pre-filled with the given
     * entries. Each entry represents the full quantity withdrawn from a vault for a
     * single item type; the implementation is responsible for packaging quantities
     * that exceed the vanilla stack size.
     *
     * @param player  the player to open the sell interface for
     * @param entries the items (with their total quantities) to sell
     */
    void openSell(Player player, List<VaultSellEntry> entries);
}
