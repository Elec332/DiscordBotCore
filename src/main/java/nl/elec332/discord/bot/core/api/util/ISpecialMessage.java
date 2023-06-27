package nl.elec332.discord.bot.core.api.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 10/09/2021
 */
public interface ISpecialMessage {

    void init(String args);

    default void onReactionMissed(MessageReaction reaction, Member member, Runnable markDirty) {
        onReactionAdded(reaction, member, markDirty);
    }

    default void onReactionAdded(GenericMessageReactionEvent reaction, Runnable markDirty) {
        Member member = reaction.retrieveMember().complete();
        onReactionAdded(reaction.getReaction(), member, markDirty);
    }

    void onReactionAdded(MessageReaction reaction, Member member, Runnable markDirty);

    void onReactionRemoved(GenericMessageReactionEvent reaction, Runnable markDirty);

    void onMessageQuoted(MessageReceivedEvent userReaction, Runnable markDirty);

    default void onMessagePosted(Message message, long instanceId, boolean sameGuild, Supplier<String> idString) {
        updateMessage(message, instanceId, sameGuild, idString);
    }

    void updateMessage(Message message, long instanceId, boolean sameGuild, Supplier<String> idString);

    String getListenerMessage();

    int serialize(ObjectOutputStream oos) throws IOException;

    void deserialize(ObjectInputStream ois, int version) throws IOException;

}
