package fr.maxlego08.essentials.commands.commands.utils;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.WelcomeModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandWelcome extends VCommand {

    public CommandWelcome(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(WelcomeModule.class);
        this.setDescription(Message.DESCRIPTION_WELCOME);
        this.setPermission(Permission.ESSENTIALS_WELCOME);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        plugin.getModuleManager().getModule(WelcomeModule.class).welcome(player);

        return CommandResultType.SUCCESS;
    }
}
