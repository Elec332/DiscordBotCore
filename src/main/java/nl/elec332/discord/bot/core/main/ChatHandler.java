package nl.elec332.discord.bot.core.main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.elec332.discord.bot.core.api.IBotModule;
import nl.elec332.discord.bot.core.api.ICommand;
import nl.elec332.discord.bot.core.util.AsyncExecutor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 21-10-2020
 */
public class ChatHandler extends ListenerAdapter {

    ChatHandler(Map<IBotModule<?>, Set<ICommand<?>>> modules, Collection<String> helpNames) {
        this.modules = modules;
        this.helpNames = helpNames.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    private final Map<IBotModule<?>, Set<ICommand<?>>> modules;
    private final List<String> helpNames;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String msg = message.getContentDisplay();

        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            Guild guild = null;
            MessageChannel textChannel;
            Member member = event.getMember();

            if (event.isFromGuild()) {
                guild = event.getGuild();
                textChannel = event.getTextChannel();
            } else {
                textChannel = event.getPrivateChannel();
            }

            if (!msg.startsWith("!")) {
                return;
            }
            msg = msg.substring(1);

            String command = msg.split(" ")[0];
            String args = msg.replace(command, "").trim();
            command = command.toLowerCase(Locale.ROOT);

            if (member == null && guild != null || event.isWebhookMessage()) {
                textChannel.sendMessage("You cannot send commands from a webhook!").submit();
                return;
            }
            if (guild != null) {
                System.out.printf("(%s {%s})[%s {%s}]<%s>: %s\n", guild.getName(), guild.getId(), textChannel.getName(), textChannel.getId(), member.getEffectiveName(), msg);
            } else {
                System.out.printf("(Private channel)[%s {%s}]<%s|%s>: %s\n", textChannel.getName(), textChannel.getId(), event.getAuthor().getId(), event.getAuthor().getName(), msg);
            }
            processCommand(textChannel, message, command, args, member, guild == null ? -1 : guild.getIdLong());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processCommand(MessageChannel channel, Message message, String command, String args, Member member, long serverId) {
        for (String s : helpNames) {
            if (command.equals(s)) {
                executeHelpCommand(channel);
                return;
            }
        }
        try {
            for (Map.Entry<IBotModule<?>, Set<ICommand<?>>> e : this.modules.entrySet()) {
                IBotModule module = e.getKey();
                Set<ICommand<?>> commands = e.getValue();
                Object cfg = serverId < 0 ? null : module.getInstanceFor(serverId);
                AsyncExecutor.executeAsync(() -> {
                    for (ICommand cmd : commands) {
                        if (!command.equals(cmd.toString().toLowerCase(Locale.ROOT)) && !cmd.getAliases().contains(command)) {
                            continue;
                        }
                        if (cfg == null && ! cmd.canRunAsPrivateCommand()) {
                            continue;
                        }
                        if (!module.canRunCommand(channel, member, cfg, cmd)) {
                            continue;
                        }
                        if (cmd.executeCommand(channel, message, member, cfg, args)) {
                            return;
                        }
                    }
                });
            }
        } catch (InsufficientPermissionException e) {
            channel.sendMessage("The bot has insufficient permissions to perform this command!\n Please re-invite the bot with the following link. (Settings will be saved)\n" + Main.INVITE_URL).submit();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            channel.sendMessage("Failed to process command, (Type: " + e.getClass().getName() + ") message: " + e.getMessage()).submit();
        }
    }

    private void executeHelpCommand(MessageChannel channel) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Help info")
                .setDescription("All available commands & descriptions\n\n");

        builder.addField("!" + String.join(", !", helpNames), " Shows info about all available commands.", false);

        modules.forEach((m, c) -> {
            builder.addBlankField(false);
            builder.addField(m.getModuleName() + " commands", "", false);
            for (ICommand<?> cmd : c) {
                if (cmd.isHidden()) {
                    continue;
                }
                String extra = " ";
                if (!cmd.getAliases().isEmpty()) {
                    extra = " (!" + String.join(", !", cmd.getAliases()) + ") ";
                }
                String argsDesc = cmd.getArgs().isEmpty() ? "" : "<" + cmd.getArgs() + ">";
                builder.addField("!" + cmd.toString().toLowerCase(Locale.ROOT) + extra + argsDesc, cmd.getHelpText(), false);
            }
        });

        channel.sendMessageEmbeds(builder.build()).submit();
    }

}
