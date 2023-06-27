package nl.elec332.discord.bot.core.api.util;

import nl.elec332.discord.bot.core.api.ICommand;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 22/05/2021
 */
public abstract class SimpleCommand<C> implements ICommand<C> {

    public SimpleCommand(String name, String description) {
        this.name = name;
        this.description = description;
        this.aliases = new HashSet<>();
        addAliases(this.aliases::add);
    }

    private final String name;
    private final String description;
    private final Set<String> aliases;


    void addAliases(Consumer<String> reg) {
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public final String toString() {
        return this.name;
    }

    @Override
    public final String getDescription() {
        return this.description;
    }

    @Override
    public final Collection<String> getAliases() {
        return this.aliases;
    }

}
