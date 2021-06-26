package nl.elec332.discord.bot.core.util;

import com.google.gson.Gson;
import nl.elec332.discord.bot.core.api.util.AbstractConfigurableBotModule;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.LongConsumer;

/**
 * Created by Elec332 on 22/05/2021
 */
public abstract class AbstractGSONModule<C extends Serializable> extends AbstractConfigurableBotModule<C> {

    public AbstractGSONModule(String moduleName) {
        super(moduleName);
        this.gson = createGson();
    }

    private final Gson gson;

    protected abstract Gson createGson();

    protected abstract Class<C> getInstanceType();

    @Override
    protected C createInstance(long sid) {
        C ret;
        try {
            ret = getInstanceType().getConstructor(long.class).newInstance(sid);
        } catch (Exception e) {
            try {
                ret = getInstanceType().getConstructor().newInstance();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
        if (ret instanceof LongConsumer) {
            ((LongConsumer) ret).accept(sid);
        }
        return ret;
    }

    @Override
    protected final void serializeInstance(C cfg, ObjectOutputStream oos) throws IOException {
        oos.writeUTF(gson.toJson(cfg));
    }

    @Override
    protected final C deserializeInstance(ObjectInputStream ois, int version, long sid) throws IOException {
        C ret = gson.fromJson(ois.readUTF(), getInstanceType());
        if (ret instanceof LongConsumer) {
            ((LongConsumer) ret).accept(sid);
        }
        return ret;
    }

}
