package fr.maxlego08.essentials.listener;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.utils.inventory.EnderChestHolder;
import fr.maxlego08.essentials.api.utils.inventory.PlayerInventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class InvseeListener implements Listener {

    public InvseeListener(EssentialsPlugin plugin) {
        plugin.getScheduler().runTimer(this::sync, 1L, 1L);
    }

    /**
     * Keeps the opened invsee menus in sync with the players they are watching.
     */
    private void sync() {

        Iterator<PlayerInventoryHolder> iterator = PlayerInventoryHolder.getHolders().iterator();
        while (iterator.hasNext()) {

            PlayerInventoryHolder holder = iterator.next();
            Player viewer = holder.viewer();
            Player target = holder.player();

            if (viewer == null || !viewer.isOnline() || target == null || !target.isOnline()) {
                iterator.remove();
                if (viewer != null && viewer.isOnline()) viewer.closeInventory();
                continue;
            }

            if (holder.sync()) viewer.updateInventory();
        }
    }

    /**
     * The decoration slots do not belong to the target player, an item dropped there would be lost.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDecorationClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlayerInventoryHolder)) return;
        if (event.getClickedInventory() != event.getInventory()) return;
        if (!PlayerInventoryHolder.isPlayerSlot(event.getSlot())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof PlayerInventoryHolder holder) holder.markDirty();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PlayerInventoryHolder holder) holder.markDirty();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof EnderChestHolder enderChestHolder) {
            var targetPlayer = enderChestHolder.player();
            if (targetPlayer == null || !targetPlayer.isOnline()) return;
            int slot = 0;
            for (ItemStack content : event.getInventory().getContents()) {
                targetPlayer.getEnderChest().setItem(slot++, content);
            }
            targetPlayer.saveData();
        } else if (event.getInventory().getHolder() instanceof PlayerInventoryHolder playerInventoryHolder) {
            PlayerInventoryHolder.unregister(playerInventoryHolder.viewer());
            var targetPlayer = playerInventoryHolder.player();
            if (targetPlayer == null || !targetPlayer.isOnline()) return;
            playerInventoryHolder.apply(event.getInventory());
            targetPlayer.saveData();
        }
    }
}
