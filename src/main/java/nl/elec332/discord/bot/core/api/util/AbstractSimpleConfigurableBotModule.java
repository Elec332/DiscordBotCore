package nl.elec332.discord.bot.core.api.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Elec332 on 22/05/2021
 */
public abstract class AbstractSimpleConfigurableBotModule<C extends ISimpleInstance> extends AbstractConfigurableBotModule<C> {

    public AbstractSimpleConfigurableBotModule(String moduleName) {
        super(moduleName);
    }

    @Override
    protected final void serializeInstance(C cfg, ObjectOutputStream oos) throws IOException {
        cfg.serialize(oos);
    }

    @Override
    protected final C deserializeInstance(ObjectInputStream ois, int version, long sid) throws IOException {
        C ret = createInstance(sid);
        ret.deserialize(ois, version);
        return ret;
    }

}
