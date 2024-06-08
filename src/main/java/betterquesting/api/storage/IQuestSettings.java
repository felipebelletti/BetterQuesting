package betterquesting.api.storage;

import betterquesting.api.properties.IPropertyContainer;
import net.minecraft.world.entity.player.Player;


public interface IQuestSettings extends IPropertyContainer {
    boolean canUserEdit(Player player);

    void reset();
}
