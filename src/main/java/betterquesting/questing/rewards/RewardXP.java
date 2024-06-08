package betterquesting.questing.rewards;

import betterquesting.XPHelper;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.gui2.rewards.PanelRewardXP;
import betterquesting.questing.rewards.factory.FactoryRewardXP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RewardXP implements IReward {
    public int amount = 1;
    public boolean levels = true;

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryRewardXP.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.reward.xp";
    }

    @Override
    public boolean canClaim(Player player, DBEntry<IQuest> quest) {
        return true;
    }

    @Override
    public void claimReward(Player player, DBEntry<IQuest> quest) {
        XPHelper.addXP(player, !levels ? amount : XPHelper.getLevelXP(amount));
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        amount = nbt.getInteger("amount");
        levels = nbt.getBoolean("isLevels");
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setInteger("amount", amount);
        nbt.setBoolean("isLevels", levels);
        return nbt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardXP(rect, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest) {
        return null;
    }
}
