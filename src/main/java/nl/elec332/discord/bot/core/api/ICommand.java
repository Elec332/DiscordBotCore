package nl.elec332.discord.bot.core.api;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Collection;

/**
 * Created by Elec332 on 17/05/2021
 */
public interface ICommand<C> {

    String getName();

    String getDescription();

    Collection<String> getAliases();

    default void registerCommand(SlashCommandData commandData){
    }

    boolean executeCommand(SlashCommandInteraction interaction, InteractionHook messageHook, Member member, C config);

    default boolean isHidden() {
        return false;
    }

    default boolean supportsDMs() {
        return false;
    }

    default boolean replyWithHiddenMessages() {
        return false;
    }

    default boolean delayReply() {
        return true;
    }

}
