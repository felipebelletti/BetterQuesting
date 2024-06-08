package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskHunt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskHunt implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskHunt INSTANCE = new FactoryTaskHunt();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":hunt");
    }

    @Override
    public TaskHunt createNew() {
        return new TaskHunt();
    }

    @Override
    public TaskHunt loadFromData(CompoundTag json) {
        TaskHunt task = new TaskHunt();
        task.readFromNBT(json);
        return task;
    }

}
