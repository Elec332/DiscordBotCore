package nl.elec332.discord.bot.core.api.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Elec332 on 22/05/2021
 */
public interface ISimpleInstance {

    void serialize(ObjectOutputStream oos) throws IOException;

    void deserialize(ObjectInputStream ois, int version) throws IOException;

}
