package betterquesting.blocks;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TileSubmitStation extends BlockEntity implements IFluidHandler, Container, Tickable, WorldlyContainer {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    private final LazyOptional<IItemHandler> itemHandlerLazy = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IFluidHandler> fluidHandlerLazy = LazyOptional.of(() -> this);
    private NonNullList<ItemStack> itemStack = NonNullList.withSize(2, ItemStack.EMPTY);
    public UUID owner = null;
    public int questID = -1;
    public int taskID = -1;

    private DBEntry<IQuest> qCached;

    public TileSubmitStation(BlockPos pos, BlockState state) {
        super(BlockEntityType.SIGN, pos, state);

//        this.itemHandler = new ItemStackHandler(2) {
//            @Override
//            protected void onContentsChanged(int slot) {
//                setChanged();
//            }
//        };

//        this.fluidHandler = new FluidTank(Integer.MAX_VALUE) {
//            @Override
//            protected void onContentsChanged() {
//                setChanged();
//            }
//        };
//
//        this.itemHandlerLazyOptional = LazyOptional.of(() -> itemHandler);
//        this.fluidHandlerLazyOptional = LazyOptional.of(() -> fluidHandler);
    }

    public DBEntry<IQuest> getQuest() {
        if (questID < 0) return null;

        if (qCached == null) {
            IQuest tmp = QuestDatabase.INSTANCE.getValue(questID);
            if (tmp != null) qCached = new DBEntry<>(questID, tmp);
        }

        return qCached;
    }

    public ITask getRawTask() {
        DBEntry<IQuest> q = getQuest();
        if (q == null || taskID < 0) return null;
        return q.getValue().getTasks().getValue(taskID);
    }

    public IItemTask getItemTask() {
        ITask t = getRawTask();
        return t == null ? null : (t instanceof IItemTask ? (IItemTask) t : null);
    }

    public IFluidTask getFluidTask() {
        ITask t = getRawTask();
        return t == null ? null : (t instanceof IFluidTask ? (IFluidTask) t : null);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int idx) {
        if (idx < 0 || idx >= itemStack.size()) {
            return ItemStack.EMPTY;
        }

        return itemStack.get(idx);
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int idx, int amount) {
        return ContainerHelper.removeItem(itemStack, idx, amount);
    }

    @Override
    public void setItem(int idx, @Nonnull ItemStack stack) {
        if (idx < 0 || idx >= itemStack.size()) return;
        itemStack.set(idx, stack);
    }

//    @Override
    @Nonnull
    public Component getName() {
        return BetterQuesting.submitStation.getName();
    }

//    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return (owner == null || player.getUUID().equals(owner)) && player.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) < 256;
    }

    @Override
    public void startOpen(@Nonnull Player player) {
    }

    @Override
    public void stopOpen(@Nonnull Player player) {
    }

    @Override
    public boolean canPlaceItem(int idx, @Nonnull ItemStack stack) {
        if (idx != 0 || !isSetup()) return false;

        IItemTask t = getItemTask();

        return t != null && itemStack.get(idx).isEmpty() && !t.isComplete(owner) && t.canAcceptItem(owner, getQuest(), stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        IFluidTask task = getFluidTask();
        if (task == null || resource.isEmpty() || !isFluidValid(0, resource)) return 0;

        int amount = resource.getAmount();
        if (action.execute()) {
            FluidStack remainder = task.submitFluid(owner, getQuest(), resource);
            int consumed = amount - (remainder != null ? remainder.getAmount() : 0);
            if (task.isComplete(owner)) {
                reset();
            }
            return consumed;
        }
        return amount;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return null;
    }

//    @Override
//    public boolean canFillFluidType(FluidStack fluid) {
//        IFluidTask t = getFluidTask();
//
//        return t != null && !t.isComplete(owner) && t.canAcceptFluid(owner, getQuest(), new FluidStack(fluid, 1));
//    }

//    @Override
//    public void tick() {
//        if (level.isClientSide || !isSetup() || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) return;
//
//        long wtt = level.getGameTime();
//        if (wtt % 5 == 0 && owner != null) {
//            if (wtt % 20 == 0) qCached = null; // Reset and lookup quest again once every second
//            DBEntry<IQuest> q = getQuest();
//            IItemTask t = getItemTask();
//            MinecraftServer server = level.getServer();
//            ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(owner);
//            QuestCache qc = player == null ? null : player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE).orElse(null);
//
//            // Check quest & task is present. Check input is populated and output is clear.
//            if (q != null && t != null && !itemStack.get(0).isEmpty() && itemStack.get(1).isEmpty()) {
//                ItemStack inStack = itemStack.get(0).copy();
//                ItemStack beforeStack = itemStack.get(0).copy();
//
//                if (t.canAcceptItem(owner, getQuest(), inStack)) {
//                    // Even if this returns an invalid item for submission it will be moved next pass. Done this way for container items
//                    itemStack.set(0, t.submitItem(owner, getQuest(), inStack));
//
//                    // If the task was completed or partial progress submitted. Sync the new progress with the client
//                    if (t.isComplete(owner) || !itemStack.get(0).equals(beforeStack)) needsUpdate = true;
//                } else {
//                    itemStack.set(1, inStack);
//                    itemStack.set(0, ItemStack.EMPTY);
//                }
//            }
//
//            if (t != null && t.isComplete(owner)) {
//                reset();
//                level.getServer().getPlayerList().broadcast(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 128, level.dimension(), getUpdatePacket());
//                needsUpdate = true;
//            }
//
//            if (needsUpdate) {
//                if (q != null && qc != null) qc.markQuestDirty(q.getID()); // Let the cache take care of syncing
//                needsUpdate = false;
//            }
//        }
//    }

    @Override
    public void tick() {
        if (level.isClientSide || !isSetup()) return;

        long time = level.getGameTime();
        if (time % 20 == 0) {
            DBEntry<IQuest> quest = getQuest();
            IItemTask itemTask = getItemTask();
            if (quest != null && itemTask != null && !itemHandler.getStackInSlot(0).isEmpty()) {
                ItemStack stack = itemHandler.getStackInSlot(0).copy();
                if (itemTask.canAcceptItem(owner, getQuest(), stack)) {
                    itemHandler.setStackInSlot(0, itemTask.submitItem(owner, getQuest(), stack));
                    if (itemTask.isComplete(owner)) {
                        reset();
                    }
                } else {
                    itemHandler.setStackInSlot(1, stack);
                    itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                }
            }
        }
    }

    public void setupTask(UUID owner, IQuest quest, ITask task) {
        if (owner == null || quest == null || task == null) {
            reset();
            return;
        }
        this.questID = QuestDatabase.INSTANCE.getID(quest);
        this.taskID = quest.getTasks().getID(task);
        this.owner = owner;
        setChanged();
    }

    public boolean isSetup() {
        return owner != null && questID >= 0 && taskID >= 0;
    }

    public void reset() {
        owner = null;
        questID = -1;
        taskID = -1;
        this.setChanged();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public void load(@NotNull CompoundTag tags) {
        super.load(tags);
        itemStack = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tags, itemStack);

        try {
            owner = UUID.fromString(tags.getString("owner"));
        } catch (Exception e) {
            this.reset();
            return;
        }

        questID = tags.contains("questID") ? tags.getInt("questID") : -1;
        taskID = tags.contains("task") ? tags.getInt("task") : -1;

        if (!isSetup()) {
            this.reset();
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tags) {
        super.saveAdditional(tags);
        tags.putString("owner", owner != null ? owner.toString() : "");
        tags.putInt("questID", questID);
        tags.putInt("taskID", taskID);

        ContainerHelper.saveAllItems(tags, itemStack);
    }

    private static final int[] slotsForFace = new int[]{0, 1};

    @Override
    @Nonnull
    public int[] getSlotsForFace(@Nullable Direction side) {
        return slotsForFace;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @Nonnull ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @Nonnull ItemStack stack, @Nullable Direction side) {
        return slot == 1;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.removeItem(itemStack, index, itemStack.size());
    }

    @Override
    public void clearContent() {
        itemStack.clear();
    }

//    @Override
    @Nonnull
    public Component getDisplayName() {
        return BetterQuesting.submitStation.getName();
    }

    @Override
    @Nonnull
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerLazy.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerLazy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean isEmpty() {
        return itemStack.isEmpty();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return true;
    }
}
