package nl.elec332.discord.bot.core.main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import nl.elec332.discord.bot.core.api.IBotConfigurator;
import nl.elec332.discord.bot.core.api.IBotModule;
import nl.elec332.discord.bot.core.api.ICommand;
import nl.elec332.discord.bot.core.api.IConfigurableBotModule;
import nl.elec332.discord.bot.core.util.BotHelper;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Elec332 on 21-10-2020
 */
public class Main {

//    public static final String INVITE_URL = "https://discord.com/api/oauth2/authorize?client_id=827934989764788259&permissions=313408&scope=bot%20applications.commands";
//    public static final String INVITE_URL = "https://discord.com/api/oauth2/authorize?client_id=827934989764788259&permissions=319371463744&scope=bot%20applications.commands";

    private static final String TOKEN_PROP = "discordToken";
    private static final String INVITE_PROP = "inviteURL";

    public static final File ROOT;
    public static final File EXEC;
    private static String TOKEN;

    //Start bot and load server mappings from file
    public static void main(String... args) throws Exception {
        Set<String> props = new HashSet<>();
        Set<IBotConfigurator> pls = ServiceLoader.load(IBotConfigurator.class).stream()
                .map(ServiceLoader.Provider::get)
                .peek(pl -> pl.addProperties(props::add))
                .collect(Collectors.toSet());
        Function<String, String> propGetter = loadProperties(props);
        props.clear();
        List<String> argz = Arrays.asList(args);
        pls.forEach(pl -> pl.handleProperties(propGetter, Collections.unmodifiableList(argz)));
        pls.forEach(pl -> pl.addHelpCommandNames(props::add));

        Collection<IBotModule<?>> modules = ServiceLoader.load(IBotModule.class).stream()
                .map(ServiceLoader.Provider::get)
                .map(a -> (IBotModule<?>) a) //Thank the compiler for this one
                .collect(Collectors.toList());

        Set<String> commandNames = new HashSet<>();
        Map<IBotModule<?>, Set<ICommand<?>>> commands = modules.stream()
                .collect(Collectors.toMap(k -> k, k -> {
                    Set<ICommand<?>> ret = new HashSet<>();
                    k.registerCommands(c -> {
                        String name = c.getName().toLowerCase();
                        if (commandNames.contains(name)) {
                            throw new RuntimeException("Double command-name detected: " + name);
                        }
                        commandNames.add(name);
                        ret.add(c);
                    });
                    return ret;
                }));

        modules.forEach(m_ -> {
            if (m_ instanceof IConfigurableBotModule) {
                IConfigurableBotModule<?> m = (IConfigurableBotModule<?>) m_;
                File f = BotHelper.getFile(m.getModuleName().toLowerCase(Locale.ROOT) + ".dmc");
                if (f.exists()) {
                    try {
                        synchronized (m) {
                            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f)));
                            int ver = ois.readInt();
                            m.deserialize(ois, ver);
                            ois.close();
                        }
                    } catch (Exception en) {
                        try {
                            synchronized (m) {
                                ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f.getAbsolutePath() + ".back")));
                                int ver = ois.readInt();
                                m.deserialize(ois, ver);
                                ois.close();
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to load settings from module: " + m.getModuleName(), en);
                        }
                    }
                }
                Runnable save = () -> {
                    try {
                        synchronized (m) {
                            File back = new File(f.getAbsolutePath() + ".back");
                            if (f.exists()) {
                                Files.move(f.toPath(), back.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(f)));
                            oos.writeInt(m.getFileVersion());
                            m.serialize(oos);
                            oos.close();
                            if (back.exists() && !back.delete()) {
                                throw new IOException();
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed save settings from module: " + m.getModuleName(), e);
                    }
                };
                synchronized (m) {
                    m.initialize(save);
                }
            }
        });

        try {
            JDABuilder builder = JDABuilder.createDefault("nope").enableIntents(GatewayIntent.GUILD_MEMBERS);
            pls.forEach(c -> c.onBotCreation(builder));
            modules.forEach(m -> m.onBotCreation(builder));
            builder.setToken(TOKEN);
            builder.addEventListeners(new CommandHandler(Collections.unmodifiableMap(commands), props, propGetter.apply(INVITE_PROP)));

            builder.addEventListeners(new ListenerAdapter() {

                void register(Guild guild) {
                    Set<ICommand<?>> cmd = new HashSet<>();
                    Set<CommandData> cd = new HashSet<>();
                    modules.forEach(m -> m.onBotJoinedGuild(guild, cmd::add, cd::add));
                    guild.updateCommands()
                            .addCommands(cd)
                            .addCommands(cmd.stream().map(c -> {
                                SlashCommandData cdd = Commands.slash(c.getName(), c.getDescription());
                                c.registerCommand(cdd);
                                cdd.setName(c.getName());
                                return cdd;
                            }).collect(Collectors.toSet()))
                            .queue();
                }

                @Override
                public void onGuildJoin(GuildJoinEvent event) {
                   register(event.getGuild());
                }

                @Override
                public void onGuildReady(GuildReadyEvent event) {
                    register(event.getGuild());
                }

            });

            JDA jda = builder.build();
            jda.awaitReady();

            jda.updateCommands()
                    .addCommands(props.stream().map(h -> Commands.slash(h, "Get information about the available commands.")).collect(Collectors.toSet()))
                    .addCommands(commands.values().stream().flatMap(Collection::stream).map(c -> {
                        SlashCommandData cdd = Commands.slash(c.getName(), c.getDescription());
                        c.registerCommand(cdd);
                        cdd.setName(c.getName());
                        return cdd;
                    }).collect(Collectors.toSet()))
                    .queue();

            pls.forEach(c -> c.onJDAReady(jda));
            modules.forEach(m -> m.onBotConnected(jda));

            jda.addEventListener(new SpecialMessageHandler(Collections.unmodifiableCollection(modules), jda));
            System.out.println("Finished Building JDA!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Function<String, String> loadProperties(Collection<String> extraProps) throws IOException {
        File f = BotHelper.getFile("bot.properties");
        System.out.println("Reading properties file: " + f);
        Properties appProps = new Properties();
        if (!f.exists()) {
            appProps.put(TOKEN_PROP, "");
            appProps.put(INVITE_PROP, "");
            for (String s : extraProps) {
                appProps.put(s, "");
            }
            appProps.store(new FileOutputStream(f), "Discord bot settings");
            appProps = new Properties();
        }
        appProps.load(new FileInputStream(f));
        TOKEN = appProps.getProperty(TOKEN_PROP);
        boolean tag = false;
        for (String s : extraProps) {
            if (!appProps.containsKey(s)) {
                appProps.put(s, "");
                tag = true;
            }
        }
        if (tag) {
            appProps.store(new FileOutputStream(f), "Discord bot settings");
        }
        appProps.remove(TOKEN_PROP);
        Properties finalAppProps = appProps;
        return finalAppProps::getProperty;
    }

    //Load bot properties & tokens
    static {
        try {
            ROOT = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            EXEC = new File(new File("").getAbsolutePath());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load property file locations: ", e);
        }
    }

}
