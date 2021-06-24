package nl.elec332.discord.bot.core.util;

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

}
