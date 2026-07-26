package fr.maxlego08.essentials.module.modules.economy;

import java.math.BigDecimal;

/**
 * Holds the state of the dynamic balance placeholder for a player and an economy.
 * The instance is immutable and replaced on every money change, so it can be read
 * safely from any thread while placeholders are being evaluated.
 *
 * @param startBalance the balance displayed by the placeholder while the player is chaining money changes
 * @param lastChangeAt the timestamp in milliseconds of the last money change
 */
public record BalanceAnimation(BigDecimal startBalance, long lastChangeAt) {
}
