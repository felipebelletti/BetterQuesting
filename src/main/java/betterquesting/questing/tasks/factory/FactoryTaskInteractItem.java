package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskInteractItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskInteractItem implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskInteractItem INSTANCE = new FactoryTaskInteractItem();

    private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "interact_item");

    @Override
    public ResourceLocation getRegistryName() {
        return REG_ID;
    }

    @Override
    public TaskInteractItem createNew() {
        return new TaskInteractItem();
    }

    @Override
    public TaskInteractItem loadFromData(CompoundTag nbt) {
        TaskInteractItem task = new TaskInteractItem();
        task.readFromNBT(nbt);
        return task;
    }
}
