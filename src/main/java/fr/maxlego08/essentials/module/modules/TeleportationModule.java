package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.configuration.NonLoadable;
import fr.maxlego08.essentials.api.teleportation.TeleportPermission;
import fr.maxlego08.menu.api.sound.SoundOption;
import fr.maxlego08.menu.hooks.xseries.XSound;
import fr.maxlego08.menu.sound.ZSoundOption;
import fr.maxlego08.essentials.module.ZModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeleportationModule extends ZModule {

    private final List<TeleportPermission> teleportDelayPermissions = new ArrayList<>();
    private final List<TeleportPermission> teleportProtections = new ArrayList<>();
    private boolean teleportSafety;
    private boolean teleportToCenter;
    private int teleportDelay;
    private int teleportProtection;
    private long teleportTpaProtection;
    private int teleportTpaExpire;
    private boolean teleportDelayBypass;
    private boolean openConfirmInventoryForTpa;
    private boolean openConfirmInventoryForTpaHere;
    @NonLoadable
    private SoundOption countdownSound;
    @NonLoadable
    private SoundOption completeSound;

    public TeleportationModule(ZEssentialsPlugin plugin) {
        super(plugin, "teleportation");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        this.loadInventory("confirm_request_inventory");
        this.loadInventory("confirm_request_here_inventory");

        YamlConfiguration configuration = getConfiguration();
        this.countdownSound = loadSoundOption(configuration, "countdown-sound");
        this.completeSound = loadSoundOption(configuration, "complete-sound");
    }

    private SoundOption loadSoundOption(YamlConfiguration configuration, String path) {
        ConfigurationSection section = configuration.getConfigurationSection(path);
        if (section == null || !section.getBoolean("enabled", false)) return null;

        String soundName = section.getString("sound", "");
        if (soundName == null || soundName.isEmpty()) return null;

        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        Optional<XSound> xSound = XSound.of(soundName);

        return new ZSoundOption(xSound.orElse(null), "MASTER", soundName, pitch, volume, xSound.isEmpty());
    }

    public SoundOption getCountdownSound() {
        return countdownSound;
    }

    public SoundOption getCompleteSound() {
        return completeSound;
    }

    public boolean isTeleportSafety() {
        return teleportSafety;
    }

    public boolean isTeleportToCenter() {
        return teleportToCenter;
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public boolean isTeleportDelayBypass() {
        return teleportDelayBypass;
    }

    public List<TeleportPermission> getTeleportDelayPermissions() {
        return teleportDelayPermissions;
    }

    public long getTeleportTpaProtection() {
        return teleportTpaProtection;
    }

    public int getTeleportTpaExpire() {
        return teleportTpaExpire;
    }

    public boolean isOpenConfirmInventoryForTpa() {
        return openConfirmInventoryForTpa;
    }

    public boolean isOpenConfirmInventoryForTpaHere() {
        return openConfirmInventoryForTpaHere;
    }

    public int getTeleportDelay(Player player) {
        return this.teleportDelayPermissions.stream().filter(teleportPermission -> player.hasPermission(teleportPermission.permission())).mapToInt(TeleportPermission::delay).min().orElse(this.teleportDelay);
    }

    public int getTeleportProtectionDelay(Player player) {
        return this.teleportProtections.stream().filter(teleportPermission -> player.hasPermission(teleportPermission.permission())).mapToInt(TeleportPermission::delay).min().orElse(this.teleportProtection);
    }

    public void openConfirmInventory(Player player) {
        this.plugin.getInventoryManager().openInventory(player, this.plugin, "confirm_request_inventory");
    }

    public void openConfirmHereInventory(Player player) {
        this.plugin.getInventoryManager().openInventory(player, this.plugin, "confirm_request_here_inventory");
    }
}
