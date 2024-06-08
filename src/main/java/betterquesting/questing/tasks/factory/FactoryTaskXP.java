package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskXP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskXP implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskXP INSTANCE = new FactoryTaskXP();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":xp");
    }

    @Override
    public TaskXP createNew() {
        return new TaskXP();
    }

    @Override
    public TaskXP loadFromData(CompoundTag json) {
        TaskXP task = new TaskXP();
        task.readFromNBT(json);
        return task;
    }

}
