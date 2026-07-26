package fr.maxlego08.essentials.api.utils.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder of the invsee menu.
 * <p>
 * The menu is a 54 slots inventory built like this:
 * <ul>
 *     <li>0-8: decoration</li>
 *     <li>9-35: the main inventory of the player (slots 9-35)</li>
 *     <li>37-40: helmet, chestplate, leggings and boots</li>
 *     <li>43: off hand</li>
 *     <li>45-53: the hot bar of the player (slots 0-8)</li>
 * </ul>
 * The menu is kept in sync with the target player every tick, see {@link #sync()}.
 */
public class PlayerInventoryHolder implements InventoryHolder {

    public static final int SIZE = 54;

    private static final int STORAGE_START = 9;
    private static final int STORAGE_END = 35;
    private static final int HOTBAR_START = 45;
    private static final int HELMET_SLOT = 37;
    private static final int CHESTPLATE_SLOT = 38;
    private static final int LEGGINGS_SLOT = 39;
    private static final int BOOTS_SLOT = 40;
    private static final int OFF_HAND_SLOT = 43;

    private static final Map<UUID, PlayerInventoryHolder> HOLDERS = new ConcurrentHashMap<>();

    private final Player player;
    private final Player viewer;
    private Inventory inventory;
    private boolean dirty;

    public PlayerInventoryHolder(Player player, Player viewer) {
        this.player = player;
        this.viewer = viewer;
    }

    /**
     * @return the menus currently opened.
     */
    public static Collection<PlayerInventoryHolder> getHolders() {
        return HOLDERS.values();
    }

    public static void unregister(Player viewer) {
        HOLDERS.remove(viewer.getUniqueId());
    }

    public static void unregisterAll() {
        HOLDERS.clear();
    }

    /**
     * @return true if the slot holds an item of the target player, false if it is only decoration.
     */
    public static boolean isPlayerSlot(int slot) {
        if (slot >= STORAGE_START && slot <= STORAGE_END) return true;
        if (slot >= HOTBAR_START && slot < SIZE) return true;
        return slot >= HELMET_SLOT && slot <= BOOTS_SLOT || slot == OFF_HAND_SLOT;
    }

    private static ItemStack normalize(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR ? null : itemStack;
    }

    public Player player() {
        return this.player;
    }

    public Player viewer() {
        return this.viewer;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, SIZE, "Inventory of " + this.player.getName());
            fill(this.inventory);
        }
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Starts to follow the target player, the menu will now be synchronized every tick.
     */
    public void register() {
        HOLDERS.put(this.viewer.getUniqueId(), this);
    }

    /**
     * Marks the menu as edited by the viewer, the next {@link #sync()} will give the items to the target player.
     * The edit cannot be applied right away because the click is not written in the inventory yet.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Synchronizes the menu and the target player.
     * <p>
     * When the viewer edited the menu the items are given to the target player, otherwise the inventory of the
     * target player is copied inside the menu. Doing both in the same task avoids an edit of the viewer being
     * reverted by a refresh happening on the same tick.
     *
     * @return true if the menu changed and must be sent again to the viewer.
     */
    public boolean sync() {

        if (this.inventory == null) return false;

        if (this.dirty) {
            this.dirty = false;
            apply(this.inventory);
            return false;
        }

        return refresh(this.inventory);
    }

    /**
     * Copies the inventory of the target player inside the menu and adds the decoration.
     */
    public void fill(Inventory menu) {

        refresh(menu);

        ItemStack decoration = createDecoration();
        for (int slot = 0; slot != SIZE; slot++) {
            if (!isPlayerSlot(slot)) menu.setItem(slot, decoration);
        }
    }

    /**
     * Copies the inventory of the target player inside the menu.
     *
     * @return true if at least one slot changed.
     */
    public boolean refresh(Inventory menu) {

        PlayerInventory playerInventory = this.player.getInventory();
        boolean changed = false;

        for (int slot = STORAGE_START; slot <= STORAGE_END; slot++) {
            changed |= update(menu, slot, playerInventory.getItem(slot));
        }

        for (int slot = 0; slot <= 8; slot++) {
            changed |= update(menu, HOTBAR_START + slot, playerInventory.getItem(slot));
        }

        changed |= update(menu, HELMET_SLOT, playerInventory.getHelmet());
        changed |= update(menu, CHESTPLATE_SLOT, playerInventory.getChestplate());
        changed |= update(menu, LEGGINGS_SLOT, playerInventory.getLeggings());
        changed |= update(menu, BOOTS_SLOT, playerInventory.getBoots());
        changed |= update(menu, OFF_HAND_SLOT, playerInventory.getItemInOffHand());

        return changed;
    }

    /**
     * Gives back to the target player the content of the menu.
     */
    public void apply(Inventory menu) {

        PlayerInventory playerInventory = this.player.getInventory();

        for (int slot = STORAGE_START; slot <= STORAGE_END; slot++) {
            playerInventory.setItem(slot, menu.getItem(slot));
        }

        for (int slot = 0; slot <= 8; slot++) {
            playerInventory.setItem(slot, menu.getItem(HOTBAR_START + slot));
        }

        playerInventory.setHelmet(menu.getItem(HELMET_SLOT));
        playerInventory.setChestplate(menu.getItem(CHESTPLATE_SLOT));
        playerInventory.setLeggings(menu.getItem(LEGGINGS_SLOT));
        playerInventory.setBoots(menu.getItem(BOOTS_SLOT));

        ItemStack offHand = menu.getItem(OFF_HAND_SLOT);
        playerInventory.setItemInOffHand(offHand == null ? new ItemStack(Material.AIR) : offHand);
    }

    private boolean update(Inventory menu, int slot, ItemStack itemStack) {
        ItemStack currentItemStack = normalize(menu.getItem(slot));
        ItemStack newItemStack = normalize(itemStack);
        if (Objects.equals(currentItemStack, newItemStack)) return false;
        menu.setItem(slot, newItemStack);
        return true;
    }

    private ItemStack createDecoration() {
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}
