package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskOptionalRetrieval;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskOptionalRetrieval implements IFactoryData<ITask, CompoundTag> {

    public static final FactoryTaskOptionalRetrieval INSTANCE = new FactoryTaskOptionalRetrieval();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":optional_retrieval");
    }

    @Override
    public TaskOptionalRetrieval createNew() {
        return new TaskOptionalRetrieval();
    }

    @Override
    public TaskOptionalRetrieval loadFromData(CompoundTag json) {
        TaskOptionalRetrieval task = new TaskOptionalRetrieval();
        task.readFromNBT(json);
        return task;
    }
}
