package nl.elec332.discord.bot.core.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Elec332 on 29/08/2021
 */
public class AsyncExecutor {

    public static void executeAsync(Runnable runnable) {
        CompletableFuture.runAsync(runnable, ForkJoinPool.commonPool());
    }

}
