package fr.maxlego08.essentials.api.vault;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a quantity of a single item withdrawn from a vault to be sold.
 *
 * <p>The {@code quantity} can exceed the item's maximum stack size: it is the
 * total amount that was stored in the vault, packaged as a single sellable unit
 * by the {@link VaultSellHook} implementation.
 *
 * @param item     a prototype item stack (amount is irrelevant, use {@code quantity})
 * @param quantity the total amount withdrawn from the vault
 */
public record VaultSellEntry(ItemStack item, long quantity) {
}
