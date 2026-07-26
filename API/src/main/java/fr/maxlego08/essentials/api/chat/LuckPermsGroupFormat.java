package fr.maxlego08.essentials.api.chat;

import fr.maxlego08.essentials.api.modules.Loadable;

public record LuckPermsGroupFormat(int priority, String group, String format) implements Loadable {
}