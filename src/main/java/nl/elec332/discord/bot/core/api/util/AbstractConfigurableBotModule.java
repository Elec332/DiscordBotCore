package nl.elec332.discord.bot.core.api.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import nl.elec332.discord.bot.core.api.ICommand;
import nl.elec332.discord.bot.core.api.IConfigurableBotModule;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elec332 on 22/05/2021
 */
public abstract class AbstractConfigurableBotModule<C> implements IConfigurableBotModule<C> {

    public AbstractConfigurableBotModule(String moduleName) {
        this.moduleName = moduleName;
        this.instances = new HashMap<>();
    }

    protected final String moduleName;
    protected final Map<Long, C> instances;
    private Runnable saveSettings;
    private JDA jda;

    @Override
    public void onBotConnected(JDA jda) {
        this.jda = jda;
        this.instances.values().forEach(c -> {
            if (c instanceof JDAConsumer && jda != null) {
                ((JDAConsumer) c).onJDAConnected(jda);
            }
        });
    }

    @Override
    public final void initialize(Runnable saveSettings) {
        this.saveSettings = saveSettings;
        initialize();
    }

    protected void initialize() {
    }

    protected void saveSettingsFile() {
        this.saveSettings.run();
    }

    @Override
    public final String getModuleName() {
        return this.moduleName;
    }

    @Override
    public boolean canRunCommand(SlashCommandInteraction interaction, C config, ICommand<C> command) {
        return true;
    }

    protected abstract C createInstance(long sid);

    @Override
    public final C getInstanceFor(long serverId) {
        return this.instances.computeIfAbsent(serverId, s -> {
            C ret = this.createInstance(s);
            if (ret instanceof JDAConsumer && jda != null) {
                ((JDAConsumer) ret).onJDAConnected(jda);
            }
            return ret;
        });
    }

    @Override
    public final void serialize(ObjectOutputStream oos) throws IOException {
        int size = instances.size();
        oos.writeInt(size);
        int i = 0;
        for (Map.Entry<Long, C> e : instances.entrySet()) {
            oos.writeLong(e.getKey());
            serializeInstance(e.getValue(), oos);
            i++;
            if (i > size) {
                throw new RuntimeException();
            }
        }
        serializeModule(oos);
    }

    protected abstract void serializeInstance(C cfg, ObjectOutputStream oos) throws IOException;

    protected void serializeModule(ObjectOutputStream oos) throws IOException {
    }

    @Override
    public final void deserialize(ObjectInputStream ois, int version) throws IOException {
        int size = ois.readInt();
        instances.values().forEach(c -> {
            if (c instanceof JDAConsumer) {
                ((JDAConsumer) c).onJDADisconnected();
            }
        });
        instances.clear();
        for (int i = 0; i < size; i++) {
            long l = ois.readLong();
            C c = deserializeInstance(ois, version, l);
            if (c instanceof JDAConsumer && jda != null) {
                ((JDAConsumer) c).onJDAConnected(jda);
            }
            instances.put(l, c);
        }
        deserializeModule(ois, version);
    }

    protected abstract C deserializeInstance(ObjectInputStream ois, int version, long sid) throws IOException;

    protected void deserializeModule(ObjectInputStream ois, int version) throws IOException {
    }

}