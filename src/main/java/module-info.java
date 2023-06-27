import nl.elec332.discord.bot.core.api.IBotConfigurator;
import nl.elec332.discord.bot.core.api.IBotModule;

/**
 * Created by Elec332 on 30/04/2021
 */
module nl.elec332.discord.bot.core {

    exports nl.elec332.discord.bot.core.api;
    exports nl.elec332.discord.bot.core.api.util;
    exports nl.elec332.discord.bot.core.util;

    requires transitive net.dv8tion.jda;
    requires java.desktop;
    requires com.fasterxml.jackson.core;

    requires kotlin.stdlib;

    requires static com.google.gson;

    uses IBotModule;
    uses IBotConfigurator;

}