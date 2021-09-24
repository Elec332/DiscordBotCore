package nl.elec332.discord.bot.core.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import nl.elec332.discord.bot.core.api.util.ISpecialMessage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 17/05/2021
 */
public interface IBotModule<C> {

    default void onBotConnected(JDA jda) {
    }

    String getModuleName();

    boolean canRunCommand(MessageChannel channel, Member member, C instance, ICommand<C> command);

    void registerCommands(Consumer<ICommand<C>> registry);

    default void registerSpecialMessages(BiConsumer<String, LongFunction<ISpecialMessage>> registry) {
    }

    C getInstanceFor(long serverId);

    default Stream<Member> getMessageListeners(String type, Guild server) {
        return Stream.empty();
    }

}
