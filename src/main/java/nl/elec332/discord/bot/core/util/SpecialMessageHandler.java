package nl.elec332.discord.bot.core.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import nl.elec332.discord.bot.core.api.util.SimpleCommand;

import java.util.Locale;
import java.util.function.BiPredicate;

/**
 * Created by Elec332 on 12/09/2021
 */
public class SpecialMessageHandler {

    public static void removeMessage(Message message, Member member) {
        nl.elec332.discord.bot.core.main.SpecialMessageHandler.removeMessage(message, member);
    }

    public static void removeMessage(Guild guild, long id, Member member) {
        nl.elec332.discord.bot.core.main.SpecialMessageHandler.removeMessage(guild, id, member);
    }

    public static void postSpecialMessage(String type, TextChannel channel, String args) {
        nl.elec332.discord.bot.core.main.SpecialMessageHandler.postSpecialMessage(type, channel, args);
    }

    public static void repostMessage(TextChannel channel, long id) {
        nl.elec332.discord.bot.core.main.SpecialMessageHandler.repostMessage(channel, id);
    }

    //todo
//    public static <T> SimpleCommand<T> fakeReactionCommand(String name) {
//        return new SimpleCommand<T>(name, "") {
//
//            @Override
//            public boolean executeCommand(MessageChannel channel, Message message, Member member, T config, String args) {
//                if (!member.hasPermission(Permission.ADMINISTRATOR)) {
//                    return true;
//                }
//                Message react = message.getReferencedMessage();
//                if (react != null && !message.getEmotes().isEmpty()) {
//                    String s = String.join(" ", args);
//                    s = s.substring(s.indexOf("\"") + 1);
//                    s = s.substring(0, s.indexOf("\""));
//                    String name = s.toLowerCase(Locale.ROOT);
//                    message.getGuild().findMembers(m -> m.getUser().getAsTag().toLowerCase(Locale.ROOT).equals(name) || m.getEffectiveName().toLowerCase(Locale.ROOT).equals(name)).onSuccess(ls -> {
//                        if (ls.size() == 1) {
//                            nl.elec332.discord.bot.core.main.SpecialMessageHandler.addLateReaction(react, ls.get(0), message.getEmotes());
//                        }
//                    });
//                }
//                message.delete().queue();
//                return true;
//            }
//
//            @Override
//            public boolean isHidden() {
//                return true;
//            }
//
//        };
//    }
//
//    public static <T> SimpleCommand<T> repostSpecialMessageCommand(String name, BiPredicate<Member, T> memberCheck) {
//        return new SimpleCommand<>(name, "Reposts special message", "Message ID") {
//
//            @Override
//            public boolean executeCommand(MessageChannel channel, Message message, Member member, T config, String args) {
//                if (!memberCheck.test(member, config)) {
//                    return true;
//                }
//                if (args.isEmpty()) {
//                    channel.sendMessage("Needs ID argument").submit();
//                    return true;
//                }
//                SpecialMessageHandler.repostMessage((TextChannel) channel, Long.parseUnsignedLong(args));
//                message.delete().queue();
//                return true;
//            }
//
//        };
//    }
//
//    public static <T> SimpleCommand<T> postSpecialMessageCommand(String name, String type, BiPredicate<Member, T> memberCheck) {
//        return postSpecialMessageCommand(name, type, memberCheck, "");
//    }
//
//    public static <T> SimpleCommand<T> postSpecialMessageCommand(String name, String type, BiPredicate<Member, T> memberCheck, String helpText, String... args) {
//        return new SimpleCommand<T>(name, helpText, args) {
//
//            @Override
//            public boolean executeCommand(MessageChannel channel, Message message, Member member, T config, String strings) {
//                if (!memberCheck.test(member, config)) {
//                    return true;
//                }
//                SpecialMessageHandler.postSpecialMessage(type, (TextChannel) channel, strings);
//                message.delete().queue();
//                return true;
//            }
//
//        };
//    }
//
//    public static <T> SimpleCommand<T> deleteSpecialMessageCommand(String name, BiPredicate<Member, T> memberCheck) {
//        return new SimpleCommand<T>(name, "Deletes special message from database") {
//
//            @Override
//            public boolean executeCommand(MessageChannel channel, Message message, Member member, T config, String strings) {
//                if (!memberCheck.test(member, config)) {
//                    return true;
//                }
//                Message m = message.getReferencedMessage();
//                if (m == null) {
//                    member.getUser().openPrivateChannel().flatMap(p -> p.sendMessage("You must reference the message to be deleted.")).queue();
//                    return true;
//                }
//                nl.elec332.discord.bot.core.main.SpecialMessageHandler.removeMessage(m, member);
//                message.delete().queue();
//                return true;
//            }
//
//        };
//    }

}
