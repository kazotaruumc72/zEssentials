package fr.maxlego08.essentials.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

public class LuckPermsHook {

    private final LuckPerms api;

    public LuckPermsHook() {
        this.api = LuckPermsProvider.get();
    }

    public String getPrefix(Player player) {
        User user = this.api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        QueryOptions options = this.api.getContextManager().getQueryOptions(player);
        String prefix = user.getCachedData().getMetaData(options).getPrefix();
        return prefix != null ? prefix : "";
    }

    public String getSuffix(Player player) {
        User user = this.api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        QueryOptions options = this.api.getContextManager().getQueryOptions(player);
        String suffix = user.getCachedData().getMetaData(options).getSuffix();
        return suffix != null ? suffix : "";
    }

    public String getPrimaryGroup(Player player) {
        User user = this.api.getUserManager().getUser(player.getUniqueId());
        return user != null ? user.getPrimaryGroup() : "default";
    }
}