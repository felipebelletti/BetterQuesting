package betterquesting.api2.client.gui;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;

@EventBusSubscriber
public class SceneController {
    private static IScene curScene = null;

    @Nullable
    public static IScene getActiveScene() {
        return curScene;
    }

    public static void setActiveScene(@Nullable IScene scene) {
        curScene = scene;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onGuiOpened(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof IScene) {
            curScene = (IScene) event.getScreen();
        }
    }
}
