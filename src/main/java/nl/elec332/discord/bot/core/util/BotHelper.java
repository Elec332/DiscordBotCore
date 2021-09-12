package nl.elec332.discord.bot.core.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.elec332.discord.bot.core.main.Main;

import java.io.File;

/**
 * Created by Elec332 on 08/06/2021
 */
public class BotHelper {

    public static File getFile(String name) {
        File ret = new File(Main.EXEC, name);
        if (!ret.exists()) {
            ret = new File(Main.ROOT, name);
            if (!ret.exists()) {
                return new File(Main.EXEC, name);
            }
        }
        return ret;
    }

    public static class MessageReference {

        public static MessageReference of(Message message) {
            return new MessageReference(message.getTextChannel().getIdLong(), message.getIdLong());
        }

        public MessageReference(long channelId, long messageId) {
            this.channelId = channelId;
            this.messageId = messageId;
        }

        private final long channelId;
        private final long messageId;

        public long getChannelId() {
            return this.channelId;
        }

        public long getMessageId() {
            return this.messageId;
        }

        public Message getMessage(JDA jda) {
            TextChannel c = jda.getTextChannelById(this.channelId);
            if (c == null) {
                return null;
            }
            return c.retrieveMessageById(this.messageId).submit().join();
        }

    }

}
