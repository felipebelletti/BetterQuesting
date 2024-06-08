package betterquesting.api2.client.gui.events;

import betterquesting.api.events.QuestEvent;
import betterquesting.api2.client.gui.events.types.PEventQuest;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

@EventBusSubscriber
public class CommonPanelEvents {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void OnQuestCompleted(QuestEvent event) {
        PEventBroadcaster.INSTANCE.postEvent(new PEventQuest(event.getQuestIDs()));
    }
}
