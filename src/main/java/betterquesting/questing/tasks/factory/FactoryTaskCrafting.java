package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskCrafting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskCrafting implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskCrafting INSTANCE = new FactoryTaskCrafting();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":crafting");
    }

    @Override
    public TaskCrafting createNew() {
        return new TaskCrafting();
    }

    @Override
    public TaskCrafting loadFromData(CompoundTag json) {
        TaskCrafting task = new TaskCrafting();
        task.readFromNBT(json);
        return task;
    }

}
