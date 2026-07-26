package fr.maxlego08.essentials.buttons.vault;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.vault.Vault;
import fr.maxlego08.essentials.api.vault.VaultItem;
import fr.maxlego08.essentials.module.modules.vault.VaultModule;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders one toggle per candidate vault in the multi-vault sell dialog. Clicking a
 * toggle flips whether that vault's items are included in the sale; the state lives in
 * the player's {@link VaultModule.SellDialogSession}.
 */
public class ButtonVaultSellSelection extends Button {

    private final EssentialsPlugin plugin;

    public ButtonVaultSellSelection(Plugin plugin) {
        this.plugin = (EssentialsPlugin) plugin;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryEngine inventory) {
        if (!(this.plugin.getVaultManager() instanceof VaultModule vaultModule)) return;

        VaultModule.SellDialogSession session = vaultModule.getSellDialog(player.getUniqueId());
        if (session == null) return;

        List<Integer> vaultIds = session.getVaultIds();
        for (int index = 0; index < this.slots.size(); index++) {
            int slot = this.slots.get(index);
            if (index >= vaultIds.size()) {
                inventory.addItem(slot, new ItemStack(Material.AIR)).setClick(event -> event.setCancelled(true));
                continue;
            }
            int vaultId = vaultIds.get(index);
            renderToggle(player, inventory, slot, vaultModule, session, vaultId);
        }
    }

    private void renderToggle(Player player, InventoryEngine inventory, int slot, VaultModule vaultModule, VaultModule.SellDialogSession session, int vaultId) {
        long quantity = quantity(vaultModule, player, session, vaultId);
        ItemStack itemStack = buildToggle(session.getPrototype(), vaultId, quantity, session.isSelected(vaultId));
        inventory.addItem(slot, itemStack).setClick(event -> {
            event.setCancelled(true);
            vaultModule.toggleSellDialog(player.getUniqueId(), vaultId);
            inventory.getSpigotInventory().setItem(slot,
                    buildToggle(session.getPrototype(), vaultId, quantity(vaultModule, player, session, vaultId), session.isSelected(vaultId)));
        });
    }

    private long quantity(VaultModule vaultModule, Player player, VaultModule.SellDialogSession session, int vaultId) {
        Vault vault = vaultModule.getPlayerVaults(player).getVaults().get(vaultId);
        if (vault == null) return 0;
        return vault.find(session.getPrototype()).map(VaultItem::getQuantity).orElse(0L);
    }

    private ItemStack buildToggle(ItemStack prototype, int vaultId, long quantity, boolean selected) {
        ItemStack itemStack = prototype.clone();
        itemStack.setAmount(Math.max(1, Math.min(itemStack.getMaxStackSize(), (int) Math.min(quantity, 64))));

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.displayName(line((selected ? "✔ " : "✘ ") + "Vault " + vaultId,
                    selected ? NamedTextColor.GREEN : NamedTextColor.RED));
            List<Component> lore = new ArrayList<>();
            lore.add(line("Quantite: " + quantity, NamedTextColor.GRAY));
            lore.add(line(selected ? "Selectionne (sera vendu)" : "Non selectionne",
                    selected ? NamedTextColor.GREEN : NamedTextColor.RED));
            lore.add(line("Clic pour basculer", NamedTextColor.DARK_GRAY));
            meta.lore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private Component line(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
