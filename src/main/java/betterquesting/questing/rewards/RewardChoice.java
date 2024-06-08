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
import betterquesting.client.gui2.rewards.PanelRewardChoice;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.factory.FactoryRewardChoice;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class RewardChoice implements IReward {
    /**
     * The selected reward index to be claimed.<br>
     * Should only ever be used client side. NEVER onHit server
     */
    public final List<BigItemStack> choices = new ArrayList<>();
    private final TreeMap<UUID, Integer> selected = new TreeMap<>();

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryRewardChoice.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.reward.choice";
    }

    public int getSelecton(UUID uuid) {
        if (!selected.containsKey(uuid)) {
            return -1;
        }

        return selected.get(uuid);
    }

    public void setSelection(UUID uuid, int value) {
        selected.put(uuid, value);
    }

    @Override
    public boolean canClaim(Player player, DBEntry<IQuest> quest) {
        if (!selected.containsKey(QuestingAPI.getQuestingUUID(player))) return false;

        int tmp = selected.get(QuestingAPI.getQuestingUUID(player));
        return choices.size() <= 0 || (tmp >= 0 && tmp < choices.size());
    }

    @Override
    public void claimReward(Player player, DBEntry<IQuest> quest) {
        UUID playerID = QuestingAPI.getQuestingUUID(player);

        if (choices.size() <= 0) {
            return;
        } else if (!selected.containsKey(playerID)) {
            return;
        }

        int tmp = selected.get(playerID);

        if (tmp < 0 || tmp >= choices.size()) {
            BetterQuesting.logger.log(Level.ERROR, "Choice reward was forcibly claimed with invalid choice", new IllegalStateException());
            return;
        }

        BigItemStack stack = choices.get(tmp);
        stack = stack == null ? null : stack.copy();

        if (stack == null || stack.stackSize <= 0) {
            BetterQuesting.logger.log(Level.WARN, "Claimed reward choice was null or was 0 in size!");
            return;
        }

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

    @Override
    public void readFromNBT(CompoundTag nbt) {
        choices.clear();
        ListTag cList = nbt.getTagList("choices", 10);
        for (int i = 0; i < cList.tagCount(); i++) {
            choices.add(JsonHelper.JsonToItemStack(cList.getCompoundTagAt(i)));
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        ListTag rJson = new ListTag();
        for (BigItemStack stack : choices) {
            rJson.appendTag(JsonHelper.ItemStackToJson(stack, new CompoundTag()));
        }
        nbt.setTag("choices", rJson);
        return nbt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardChoice(rect, quest, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest) {
        return null;
    }
}
