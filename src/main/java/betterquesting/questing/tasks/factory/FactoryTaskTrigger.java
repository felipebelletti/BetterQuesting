package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskTrigger implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskTrigger INSTANCE = new FactoryTaskTrigger();

    private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "trigger");

    @Override
    public ResourceLocation getRegistryName() {
        return REG_ID;
    }

    @Override
    public TaskTrigger createNew() {
        return new TaskTrigger();
    }

    @Override
    public TaskTrigger loadFromData(CompoundTag nbt) {
        TaskTrigger task = new TaskTrigger();
        task.readFromNBT(nbt);
        return task;
    }
}
