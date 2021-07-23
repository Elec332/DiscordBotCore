package nl.elec332.discord.bot.core.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Elec332 on 22/05/2021
 */
public interface IBotConfigurator {

    void addProperties(Consumer<String> registry);

    void handleProperties(Function<String, String> propertyGetter, List<String> programArguments);

    default void addHelpCommandNames(Consumer<String> names) {
    }

}
