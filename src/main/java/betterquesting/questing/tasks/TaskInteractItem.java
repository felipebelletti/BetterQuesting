package betterquesting.questing.tasks;

import betterquesting.NbtBlockType;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.tasks.PanelTaskInteractItem;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskInteractItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.init.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskInteractItem implements ITask {
    private final Set<UUID> completeUsers = new TreeSet<>();
    private final TreeMap<UUID, Integer> userProgress = new TreeMap<>();

    public BigItemStack targetItem = new BigItemStack(Items.AIR);
    public final NbtBlockType targetBlock = new NbtBlockType(Blocks.AIR);
    public boolean partialMatch = true;
    public boolean ignoreNBT = false;
    public boolean useMainHand = true;
    public boolean useOffHand = true;
    public boolean onInteract = true;
    public boolean onHit = false;
    public int required = 1;

    @Override
    public String getUnlocalisedName() {
        return BetterQuesting.MODID_STD + ".task.interact_item";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskInteractItem.INSTANCE.getRegistryName();
    }

    public void onInteract(ParticipantInfo pInfo, DBEntry<IQuest> quest, EnumHand hand, ItemStack item, IBlockState state, BlockPos pos, boolean isHit) {
        if ((!onHit && isHit) || (!onInteract && !isHit)) return;
        if ((!useMainHand && hand == EnumHand.MAIN_HAND) || (!useOffHand && hand == EnumHand.OFF_HAND)) return;

        if (targetBlock.b != Blocks.AIR) {
            if (state.getBlock() == Blocks.AIR) return;
            TileEntity tile = state.getBlock().hasTileEntity(state) ? pInfo.PLAYER.world.getTileEntity(pos) : null;
            CompoundTag tags = tile == null ? null : tile.writeToNBT(new CompoundTag());

            int tmpMeta = (targetBlock.m < 0 || targetBlock.m == OreDictionary.WILDCARD_VALUE) ? OreDictionary.WILDCARD_VALUE : state.getBlock().getMetaFromState(state);
            boolean oreMatch = targetBlock.oreDict.length() > 0 && OreDictionary.getOres(targetBlock.oreDict).contains(new ItemStack(state.getBlock(), 1, tmpMeta));

            if ((!oreMatch && (state.getBlock() != targetBlock.b || (targetBlock.m >= 0 && state.getBlock().getMetaFromState(state) != targetBlock.m))) || !ItemComparison.CompareNBTTag(targetBlock.tags, tags, true)) {
                return;
            }
        }

        if (targetItem.getBaseStack().getItem() != Items.AIR) {
            if (targetItem.hasOreDict() && !ItemComparison.OreDictionaryMatch(targetItem.getOreIngredient(), targetItem.GetTagCompound(), item, !ignoreNBT, partialMatch)) {
                return;
            } else if (!ItemComparison.StackMatch(targetItem.getBaseStack(), item, !ignoreNBT, partialMatch)) {
                return;
            }
        }

        final List<Tuple<UUID, Integer>> progress = getBulkProgress(pInfo.ALL_UUIDS);

        progress.forEach((value) -> {
            if (isComplete(value.getFirst())) return;
            int np = Math.min(required, value.getSecond() + 1);
            setUserProgress(value.getFirst(), np);
            if (np >= required) setComplete(value.getFirst());
        });

        pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
    }

    @Override
    public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        final List<Tuple<UUID, Integer>> progress = getBulkProgress(pInfo.ALL_UUIDS);

        progress.forEach((value) -> {
            if (value.getSecond() >= required) setComplete(value.getFirst());
        });

        pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
    }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    @Override
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
            userProgress.clear();
        } else {
            completeUsers.remove(uuid);
            userProgress.remove(uuid);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskInteractItem(rect, this);
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public void readProgressFromNBT(CompoundTag nbt, boolean merge) {
        if (!merge) {
            completeUsers.clear();
            userProgress.clear();
        }

        ListTag cList = nbt.getTagList("completeUsers", 8);
        for (int i = 0; i < cList.tagCount(); i++) {
            try {
                completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
            }
        }

        ListTag pList = nbt.getTagList("userProgress", 10);
        for (int n = 0; n < pList.tagCount(); n++) {
            try {
                CompoundTag pTag = pList.getCompoundTagAt(n);
                UUID uuid = UUID.fromString(pTag.getString("uuid"));
                userProgress.put(uuid, pTag.getInteger("value"));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load user progress for task", e);
            }
        }
    }

    @Override
    public CompoundTag writeProgressToNBT(CompoundTag nbt, @Nullable List<UUID> users) {
        ListTag jArray = new ListTag();
        ListTag progArray = new ListTag();

        if (users != null) {
            users.forEach((uuid) -> {
                if (completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));

                Integer data = userProgress.get(uuid);
                if (data != null) {
                    CompoundTag pJson = new CompoundTag();
                    pJson.setString("uuid", uuid.toString());
                    pJson.setInteger("value", data);
                    progArray.appendTag(pJson);
                }
            });
        } else {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));

            userProgress.forEach((uuid, data) -> {
                CompoundTag pJson = new CompoundTag();
                pJson.setString("uuid", uuid.toString());
                pJson.setInteger("value", data);
                progArray.appendTag(pJson);
            });
        }

        nbt.setTag("completeUsers", jArray);
        nbt.setTag("userProgress", progArray);

        return nbt;
    }

    @Override
    public synchronized CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setTag("item", targetItem.writeToNBT(new CompoundTag()));
        nbt.setTag("block", targetBlock.writeToNBT(new CompoundTag()));
        nbt.setBoolean("ignoreNbt", ignoreNBT);
        nbt.setBoolean("partialMatch", partialMatch);
        nbt.setBoolean("allowMainHand", useMainHand);
        nbt.setBoolean("allowOffHand", useOffHand);
        nbt.setInteger("requiredUses", required);
        nbt.setBoolean("onInteract", onInteract);
        nbt.setBoolean("onHit", onHit);
        return nbt;
    }

    @Override
    public synchronized void readFromNBT(CompoundTag nbt) {
        targetItem = new BigItemStack(nbt.getCompoundTag("item"));
        targetBlock.readFromNBT(nbt.getCompoundTag("block"));
        ignoreNBT = nbt.getBoolean("ignoreNbt");
        partialMatch = nbt.getBoolean("partialMatch");
        useMainHand = nbt.getBoolean("allowMainHand");
        useOffHand = nbt.getBoolean("allowOffHand");
        required = nbt.getInteger("requiredUses");
        onInteract = nbt.getBoolean("onInteract");
        onHit = nbt.getBoolean("onHit");
    }

    private void setUserProgress(UUID uuid, Integer progress) {
        userProgress.put(uuid, progress);
    }

    public int getUsersProgress(UUID uuid) {
        Integer n = userProgress.get(uuid);
        return n == null ? 0 : n;
    }

    private List<Tuple<UUID, Integer>> getBulkProgress(@Nonnull List<UUID> uuids) {
        if (uuids.size() <= 0) return Collections.emptyList();
        List<Tuple<UUID, Integer>> list = new ArrayList<>();
        uuids.forEach((key) -> list.add(new Tuple<>(key, getUsersProgress(key))));
        return list;
    }

    @Override
    public List<String> getTextForSearch() {
        List<String> texts = new ArrayList<>();
        if (targetBlock.getItemStack() != null) {
            texts.add(targetBlock.getItemStack().getBaseStack().getDisplayName());
        }
        if (targetItem != null) {
            texts.add(targetItem.getBaseStack().getDisplayName());
        }
        return texts;
    }
}
