package nl.elec332.discord.bot.core.api.util;

import net.dv8tion.jda.api.JDA;

/**
 * Created by Elec332 on 04/06/2021
 */
public interface JDAConsumer {

    void onJDAConnected(JDA jda);

    void onJDADisconnected();

}
