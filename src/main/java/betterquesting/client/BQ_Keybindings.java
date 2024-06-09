package betterquesting.client;

import betterquesting.core.ModReference;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import betterquesting.core.ModReference;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ModReference.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BQ_Keybindings {
    public static int keyCode = GLFW.GLFW_KEY_GRAVE_ACCENT;
    public static KeyMapping openQuests = new KeyMapping("key.betterquesting.quests", keyCode, ModReference.NAME);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openQuests);
    }
}
