package nl.elec332.discord.bot.core.main;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import nl.elec332.discord.bot.core.api.IBotModule;
import nl.elec332.discord.bot.core.api.ICommand;
import nl.elec332.discord.bot.core.util.AsyncExecutor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 20/05/2020
 */
public class CommandHandler extends ListenerAdapter {

    CommandHandler(Map<IBotModule<?>, Set<ICommand<?>>> modules, Collection<String> helpNames, String invite) {
        this.modules = modules;
        this.invite = invite;
        this.helpNames = helpNames.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    private final Map<IBotModule<?>, Set<ICommand<?>>> modules;
    private final List<String> helpNames;
    private final String invite;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmdName = event.getName().toLowerCase(Locale.ROOT);
        if (helpNames.contains(cmdName)) {
            runHelpCommand(event.getInteraction());
        }
        for (Map.Entry<IBotModule<?>, Set<ICommand<?>>> e : this.modules.entrySet()) {
            IBotModule<?> module = e.getKey();
            Set<ICommand<?>> commands = e.getValue();
            for (ICommand<?> cmd : commands) {
                if (!cmdName.equals(cmd.getName().toLowerCase(Locale.ROOT)) && !cmd.getAliases().contains(event.getName())) {
                    continue;
                }
                executeCommand(event, module, cmd);
                return;
            }
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeCommand(SlashCommandInteractionEvent event, IBotModule module, ICommand command) {
        AsyncExecutor.executeAsync(() -> {
            try {
                if (!event.isFromGuild() && !command.supportsDMs()) {
                    event.reply("This command does not support PM's!").submit();
                    return;
                }
                Object cfg = !event.isFromGuild() ? null : module.getInstanceFor(Objects.requireNonNull(event.getGuild()).getIdLong());
                if ((cfg == null && event.isFromGuild()) || !module.canRunCommand(event, cfg, command)) {
                    return;
                }
                if (command.replyWithHiddenMessages()) {
                    event.getInteraction().getHook().setEphemeral(true);
                }
                if (command.delayReply()) {
                    event.deferReply(command.replyWithHiddenMessages()).queue();
                }
                command.executeCommand(event.getInteraction(), event.getHook(), event.getMember(), cfg);
            } catch (InsufficientPermissionException e) {
                event.reply("The bot has insufficient permissions to perform this command!\n Please re-invite the bot with the following link. (Settings will be saved)\n" + invite).setEphemeral(true).submit();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                event.reply("Failed to process command, (Type: " + e.getClass().getName() + ") message: " + e.getMessage()).setEphemeral(true).submit();
            }
        });
    }

    private void runHelpCommand(SlashCommandInteraction interaction) {
        interaction.reply("TODO").submit();
//        EmbedBuilder builder = new EmbedBuilder()
//                .setTitle("Help info")
//                .setDescription("All available commands & descriptions\n\n");
//
//        builder.addField("!" + String.join(", !", helpNames), " Shows info about all available commands.", false);
//
//        modules.forEach((m, c) -> {
//            builder.addBlankField(false);
//            builder.addField(m.getModuleName() + " commands", "", false);
//            for (ICommand<?> cmd : c) {
//                if (cmd.isHidden()) {
//                    continue;
//                }
//                String extra = " ";
//                if (!cmd.getAliases().isEmpty()) {
//                    extra = " (!" + String.join(", !", cmd.getAliases()) + ") ";
//                }
//                String argsDesc = cmd.getArgs().isEmpty() ? "" : "<" + cmd.getArgs() + ">";
//                builder.addField("!" + cmd.toString().toLowerCase(Locale.ROOT) + extra + argsDesc, cmd.getHelpText(), false);
//            }
//        });
//
//        channel.sendMessageEmbeds(builder.build()).submit();
    }

}
