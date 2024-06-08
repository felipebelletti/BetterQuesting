package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskAdvancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskAdvancement implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskAdvancement INSTANCE = new FactoryTaskAdvancement();

    private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "advancement");

    @Override
    public ResourceLocation getRegistryName() {
        return REG_ID;
    }

    @Override
    public TaskAdvancement createNew() {
        return new TaskAdvancement();
    }

    @Override
    public TaskAdvancement loadFromData(CompoundTag nbt) {
        TaskAdvancement task = new TaskAdvancement();
        task.readFromNBT(nbt);
        return task;
    }
}
