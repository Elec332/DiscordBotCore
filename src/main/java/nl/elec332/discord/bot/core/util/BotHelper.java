package nl.elec332.discord.bot.core.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.elec332.discord.bot.core.main.Main;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

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

        public static MessageReference read(ObjectInputStream ois) throws IOException {
            return new MessageReference(ois.readLong(), ois.readLong());
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
            try {
                return c.retrieveMessageById(this.messageId).complete();
            } catch (Exception e) {
                return null;
            }
        }

        public void write(ObjectOutputStream oos) throws IOException {
            oos.writeLong(this.channelId);
            oos.writeLong(this.messageId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageReference that = (MessageReference) o;
            return channelId == that.channelId && messageId == that.messageId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelId, messageId);
        }

        @Override
        public String toString() {
            return "MessageReference{" +
                    "channelId=" + channelId +
                    ", messageId=" + messageId +
                    '}';
        }
    }

}
