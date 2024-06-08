package betterquesting.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class AdvListenerManager {
    public static final AdvListenerManager INSTANCE = new AdvListenerManager();

    private final List<BqsAdvListener<?>> listenerList = new ArrayList<>();

    public void registerListener(final BqsAdvListener<?> listener) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.execute(() -> listenerList.add(listener));
    }

    public void updateAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (BqsAdvListener<?> advl : listenerList) {
                advl.unregisterSelf(player.getAdvancements());
                if (advl.verify()) advl.registerSelf(player.getAdvancements());
            }
        }

        listenerList.removeIf(advl -> !advl.verify());
    }
}
