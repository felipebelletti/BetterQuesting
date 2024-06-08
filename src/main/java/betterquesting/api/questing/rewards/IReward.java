package betterquesting.api.questing.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;

public interface IReward extends INBTSaveLoad<CompoundTag> {
    String getUnlocalisedName();

    ResourceLocation getFactoryID();

    boolean canClaim(Player player, DBEntry<IQuest> quest);

    void claimReward(Player player, DBEntry<IQuest> quest);

    @OnlyIn(Dist.CLIENT)
    IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest);

    @Nullable
    @OnlyIn(Dist.CLIENT)
    Screen getRewardEditor(Screen parent, DBEntry<IQuest> quest);
}
