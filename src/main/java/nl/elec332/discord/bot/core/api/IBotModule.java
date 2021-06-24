package nl.elec332.discord.bot.core.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.function.Consumer;

/**
 * Created by Elec332 on 17/05/2021
 */
public interface IBotModule<C> {

    default void onBotConnected(JDA jda) {
    }

    String getModuleName();

    boolean canRunCommand(MessageChannel channel, Member member, C instance, ICommand<C> command);

    void registerCommands(Consumer<ICommand<C>> registry);

    C getInstanceFor(long serverId);

}
