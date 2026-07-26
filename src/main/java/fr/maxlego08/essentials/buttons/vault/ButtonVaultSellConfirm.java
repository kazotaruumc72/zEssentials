package fr.maxlego08.essentials.buttons.vault;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.module.modules.vault.VaultModule;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

/**
 * Confirm button of the multi-vault sell dialog: sells the clicked item from every
 * selected vault and opens the external sell interface.
 */
public class ButtonVaultSellConfirm extends Button {

    private final EssentialsPlugin plugin;

    public ButtonVaultSellConfirm(Plugin plugin) {
        this.plugin = (EssentialsPlugin) plugin;
    }

    @Override
    public void onLeftClick(Player player, InventoryClickEvent event, InventoryEngine inventory, int slot) {
        super.onLeftClick(player, event, inventory, slot);
        if (this.plugin.getVaultManager() instanceof VaultModule vaultModule) {
            vaultModule.confirmSellDialog(player);
        }
    }
}
