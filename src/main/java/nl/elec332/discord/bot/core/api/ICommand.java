package nl.elec332.discord.bot.core.api;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Collection;

/**
 * Created by Elec332 on 17/05/2021
 */
public interface ICommand<C> {

    String getHelpText();

    String getArgs();

    Collection<String> getAliases();

    boolean executeCommand(MessageChannel channel, Message message, Member member, C config, String... args);

    default boolean isHidden() {
        return false;
    }

    default boolean canRunAsPrivateCommand() {
        return false;
    }

}
