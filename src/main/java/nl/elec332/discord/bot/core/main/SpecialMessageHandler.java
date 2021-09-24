package nl.elec332.discord.bot.core.main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.elec332.discord.bot.core.api.IBotModule;
import nl.elec332.discord.bot.core.api.util.ISpecialMessage;
import nl.elec332.discord.bot.core.util.AsyncExecutor;
import nl.elec332.discord.bot.core.util.BotHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 10/09/2021
 */
public class SpecialMessageHandler extends ListenerAdapter {

    SpecialMessageHandler(Collection<IBotModule<?>> modules, JDA jda) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        this.jda = jda;
        instance = this;
        this.modules = modules.stream()
                .collect(Collectors.toMap(k -> k, k -> {
                    Map<String, LongFunction<ISpecialMessage>> ret = new HashMap<>();
                    k.registerSpecialMessages((name, factory) -> {
                        if (this.allMessages.containsKey(name)) {
                            System.out.println("Duplicate name: " + name + "  This message will not be registered!");
                            return;
                        }
                        this.allMessages.put(name, factory);
                        ret.put(name, factory);
                        this.nameToModule.put(name, k);
                    });
                    return ret;
                }));
        load();
    }

    private static final int MAX = 8;
    public static SpecialMessageHandler instance = null;

    private final JDA jda;
    private final Map<IBotModule<?>, Map<String, LongFunction<ISpecialMessage>>> modules;
    private final Map<String, LongFunction<ISpecialMessage>> allMessages = new HashMap<>();
    private final Map<String, IBotModule<?>> nameToModule = new HashMap<>();

    private final Map<Long, List<WrappedMessage>> activeMessages = new HashMap<>();

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        Message message = event.retrieveMessage().complete();
        AsyncExecutor.executeAsync(() -> getMessage(message).ifPresent(m -> {
            m.message.onReactionRemoved(event, this::save);
            m.updateMessages(jda);
        }));
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        Message message = event.retrieveMessage().complete();
        AsyncExecutor.executeAsync(() -> getMessage(message).ifPresent(m -> {
            m.message.onReactionAdded(event, this::save);
            m.updateMessages(jda);
        }));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage().getReferencedMessage();
        if (message == null) {
            return;
        }
        synchronized (activeMessages) {
            activeMessages.values().stream()
                    .flatMap(Collection::stream)
                    .filter(w -> w.locations.contains(BotHelper.MessageReference.of(message)))
                    .findFirst()
                    .ifPresent(w -> {
                        AsyncExecutor.executeAsync(() -> {
                            w.message.onMessageQuoted(event, this::save);
                            w.updateMessages(jda);
                        });
                    });
        }
    }

    private Optional<WrappedMessage> getMessage(Message msg) {
        return getMessage(msg.getGuild(), m -> m.locations.contains(BotHelper.MessageReference.of(msg)));
    }

    private Optional<WrappedMessage> getMessage(Guild guild, long id) {
        return getMessage(guild, m -> m.id == id);
    }

    private Optional<WrappedMessage> getMessage(Guild guild, Predicate<WrappedMessage> test) {
        synchronized (activeMessages) {
            return Optional.ofNullable(this.activeMessages.get(guild.getIdLong()))
                    .flatMap(messages -> messages.stream()
                            .filter(test)
                            .findFirst());
        }
    }

    private void load() {
        File f = BotHelper.getFile("messages.smf");
        if (f.exists()) {
            try {
                load(f);
            } catch (Exception e) {
                e.printStackTrace();
                load(new File(f.getAbsolutePath() + ".back"));
            }
            System.out.println(this.activeMessages);
        }
    }

    private void load(File file) {
        try {
            synchronized (activeMessages) {
                activeMessages.clear();
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                while (ois.readBoolean()) {
                    long l = ois.readLong();
                    List<WrappedMessage> list = new ArrayList<>();
                    activeMessages.put(l, list);
                    int len = ois.readInt();
                    for (int i = 0; i < len; i++) {
                        String type = ois.readUTF();
                        long id = ois.readLong();
                        int locations = ois.readInt();
                        List<BotHelper.MessageReference> ls = new ArrayList<>();
                        for (int j = 0; j < locations; j++) {
                            ls.add(BotHelper.MessageReference.read(ois));
                        }
                        int ver = ois.readInt();
                        int ms = ois.readInt();
                        byte[] data = new byte[ms];
                        ois.readFully(data); // -1/10 wouldn't trust OOS.read() to read all bytes again...
                        LongFunction<ISpecialMessage> msup = allMessages.get(type);
                        if (msup == null) {
                            continue;
                        }
                        ObjectInputStream mis = new ObjectInputStream(new ByteArrayInputStream(data));
                        ISpecialMessage m = msup.apply(l);
                        m.deserialize(mis, ver);
                        mis.close();
                        list.add(new WrappedMessage(l, ls, id, type, m));
                    }
                }
                ois.close();
            }
        } catch (Exception en) {
            en.printStackTrace();
            throw new RuntimeException("Failed to load messages from file: " + file, en);
        }
        synchronized (activeMessages) {
            for (Collection<WrappedMessage> c : activeMessages.values()) {
                for (WrappedMessage m : c) {
                    try {
                        m.locations.stream().map(r -> r.getMessage(jda)).filter(Objects::nonNull).forEach(msg -> {
                            msg.getReactions().forEach(r -> {
                                if (r.isSelf() && r.getCount() > 1) {
                                    r.retrieveUsers().forEach(u -> m.message.onReactionMissed(new MessageReaction(msg.getChannel(), r.getReactionEmote(), msg.getIdLong(), false, 1), msg.getGuild().getMember(u), this::save));
                                }
                            });
                        });
                        m.updateMessages(jda);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void addLateReaction(Message message, Member member, Collection<Emote> emotes) {
        long id = message.getIdLong();
        BotHelper.MessageReference ref = BotHelper.MessageReference.of(message);
        synchronized (instance.activeMessages) {
            for (Collection<WrappedMessage> c : instance.activeMessages.values()) {
                for (WrappedMessage m : c) {
                    if (m.locations.contains(ref)) {
                        for (Emote emote : emotes) {
                            m.message.onReactionMissed(new MessageReaction(message.getChannel(), MessageReaction.ReactionEmote.fromCustom(emote), id, false, 1), member, instance::save);
                        }
                        m.updateMessages(message.getJDA());
                    }
                }
            }
        }
    }

    private synchronized void save() {
        synchronized (this) {
            try {
                File f = BotHelper.getFile("messages.smf");
                File back = new File(f.getAbsolutePath() + ".back");
                if (f.exists()) {
                    Files.move(f.toPath(), back.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                synchronized (activeMessages) {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                    for (Map.Entry<Long, List<WrappedMessage>> e : activeMessages.entrySet()) {
                        oos.writeBoolean(true);
                        oos.writeLong(e.getKey());
                        List<WrappedMessage> messages = e.getValue();
                        oos.writeInt(messages.size());
                        for (WrappedMessage message : messages) {
                            oos.writeUTF(message.type);
                            oos.writeLong(message.id);
                            oos.writeInt(message.locations.size());
                            for (BotHelper.MessageReference location : message.locations) {
                                location.write(oos);
                            }
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutputStream oos2 = new ObjectOutputStream(bos);
                            oos.writeInt(message.message.serialize(oos2));
                            oos2.close();
                            byte[] data = bos.toByteArray();
                            oos.writeInt(data.length);
                            oos.write(data);
                        }
                        oos.flush();
                    }
                    oos.writeBoolean(false);
                    oos.flush();
                    oos.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class WrappedMessage {

        private WrappedMessage(String type, Message message, ISpecialMessage message1) {
            this.locations = new ArrayList<>();
            this.locations.add(BotHelper.MessageReference.of(message));
            this.id = message.getIdLong();
            this.type = type;
            this.message = message1;
            this.guild = message.getGuild().getIdLong();
        }

        private WrappedMessage(long guild, List<BotHelper.MessageReference> locations, long id, String type, ISpecialMessage message) {
            this.guild = guild;
            this.locations = locations;
            this.id = id;
            this.type = type;
            this.message = message;
        }

        private final transient long guild;
        private final List<BotHelper.MessageReference> locations;
        private final long id;
        private final String type;
        private final ISpecialMessage message;

        boolean exists(JDA jda) {
            for (BotHelper.MessageReference r : locations) {
                if (r.getMessageId() == id) {
                    return r.getMessage(jda) != null;
                }
            }
            return false;
        }

        void updateMessages(JDA jda) {
            locations.stream().map(r -> r.getMessage(jda)).filter(Objects::nonNull).forEach(msg -> {
                message.updateMessage(msg, id, msg.getGuild().getIdLong() == guild);
            });
        }

    }

    public void postSpecialMessage_(String type, TextChannel channel, String args) {
        LongFunction<ISpecialMessage> ms = allMessages.get(type);
        IBotModule<?> module = nameToModule.get(type);
        if (ms == null || module == null) {
            channel.sendMessage("No such message type: " + type).submit();
            return;
        }
        JDA jda = channel.getJDA();
        Guild guild = channel.getGuild();
        long id = guild.getIdLong();
        ISpecialMessage message = ms.apply(channel.getGuild().getIdLong());
        String msg = message.getListenerMessage();
        synchronized (activeMessages) {
            List<WrappedMessage> messages = activeMessages.computeIfAbsent(id, l -> new ArrayList<>());
            if (messages.size() > MAX) {
                messages.removeIf(m -> !m.exists(jda));
                if (messages.size() > MAX) {
                    channel.sendMessage("You have over " + MAX + "special messages active in this server. Deactivate or delete the to be able to post a new one.").submit();
                }
            }
            message.init(args);
            Message m = channel.sendMessage(".").submit().join();
            message.onMessagePosted(m, m.getIdLong(), true);
            WrappedMessage w = new WrappedMessage(type, m, message);
            messages.add(w);
        }
        save();
        module.getMessageListeners(type, guild)
                .filter(Objects::nonNull)
                .filter(member -> member.hasPermission(channel, Permission.MESSAGE_READ))
                .map(Member::getUser)
                .forEach(user -> user.openPrivateChannel()
                        .map(c -> c.sendMessage(msg).submit())
                        .submit());
    }

    private void removeMessage_(Message message) {
        removeMessage(message.getGuild(), m -> m.locations.contains(BotHelper.MessageReference.of(message)));
    }

    private void removeMessage_(Guild guild, long id) {
        removeMessage(guild, m -> m.id == id);
    }

    private void removeMessage(Guild guild, Predicate<WrappedMessage> test) {
        synchronized (activeMessages) {
            List<WrappedMessage> messages = this.activeMessages.get(guild.getIdLong());
            if (messages != null) {
                messages.removeIf(test);
            }
        }
    }

    public static void repostMessage(TextChannel channel, long id) {
        boolean[] b = {false};
        synchronized (instance.activeMessages) {
            instance.activeMessages.values().stream().flatMap(List::stream).filter(w -> w.id == id).findFirst().ifPresent(w -> {
                Message m = channel.sendMessage(".").submit().join();
                w.message.onMessagePosted(m, w.id, channel.getGuild().getIdLong() == w.guild);
                w.locations.add(BotHelper.MessageReference.of(m));
                b[0] = true;
                instance.save();
            });
        }
        if (!b[0]) {
            channel.sendMessage("Failed to find message with ID: " + id).submit();
        }
    }

    public static void removeMessage(Message message) {
        instance.removeMessage_(message);
    }

    public static void removeMessage(Guild guild, long id) {
        instance.removeMessage_(guild, id);
    }

    public static void postSpecialMessage(String type, TextChannel channel, String args) {
        instance.postSpecialMessage_(type, channel, args);
    }

}
