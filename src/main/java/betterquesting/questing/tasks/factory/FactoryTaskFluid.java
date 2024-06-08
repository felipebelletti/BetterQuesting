package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskFluid implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskFluid INSTANCE = new FactoryTaskFluid();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":fluid");
    }

    @Override
    public TaskFluid createNew() {
        return new TaskFluid();
    }

    @Override
    public TaskFluid loadFromData(CompoundTag json) {
        TaskFluid task = new TaskFluid();
        task.readFromNBT(json);
        return task;
    }

}
