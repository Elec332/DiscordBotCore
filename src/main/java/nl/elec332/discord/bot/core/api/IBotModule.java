package nl.elec332.discord.bot.core.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import nl.elec332.discord.bot.core.api.util.ISpecialMessage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 17/05/2021
 */
public interface IBotModule<C> {

    default void onBotCreation(JDABuilder builder) {
    }

    default void onBotConnected(JDA jda) {
    }

    default void onBotJoinedGuild(Guild guild, Consumer<ICommand<C>> commandRegistryA, Consumer<CommandData> commandRegistryB) {
    }

    String getModuleName();

    boolean canRunCommand(SlashCommandInteraction interaction, C instance, ICommand<C> command);

    void registerCommands(Consumer<ICommand<C>> registry);

    default void registerSpecialMessages(BiConsumer<String, LongFunction<ISpecialMessage>> registry) {
    }

    C getInstanceFor(long serverId);

    default Stream<Member> getMessageListeners(String type, Guild server) {
        return Stream.empty();
    }

}
