package betterquesting.questing.rewards;

import betterquesting.NBTReplaceUtil;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.gui2.rewards.PanelRewardItem;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.factory.FactoryRewardItem;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class RewardItem implements IReward {
    public final List<BigItemStack> items = new ArrayList<>();

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryRewardItem.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.reward.item";
    }

    @Override
    public boolean canClaim(Player player, DBEntry<IQuest> quest) {
        return true;
    }

    @Override
    public void claimReward(Player player, DBEntry<IQuest> quest) {
        for (BigItemStack r : items) {
            BigItemStack stack = r.copy();

            for (ItemStack s : stack.getCombinedStacks()) {
                if (s.getTagCompound() != null) {
                    s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_NAME", player.getName()));
                    s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_UUID", QuestingAPI.getQuestingUUID(player).toString()));
                }

                if (!player.inventory.addItemStackToInventory(s)) {
                    player.dropItem(s, true, false);
                }
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        items.clear();
        ListTag rList = nbt.getTagList("rewards", 10);
        for (int i = 0; i < rList.tagCount(); i++) {
            try {
                BigItemStack item = JsonHelper.JsonToItemStack(rList.getCompoundTagAt(i));
                if (item != null) items.add(item);
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load reward item data", e);
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        ListTag rJson = new ListTag();
        for (BigItemStack stack : items) {
            rJson.appendTag(JsonHelper.ItemStackToJson(stack, new CompoundTag()));
        }
        nbt.setTag("rewards", rJson);
        return nbt;
    }

    @Override
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardItem(rect, this);
    }

    @Override
    public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest) {
        return null;
    }
}
