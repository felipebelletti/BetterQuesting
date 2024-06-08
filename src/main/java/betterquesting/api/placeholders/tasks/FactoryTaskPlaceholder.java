package betterquesting.api.placeholders.tasks;

import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.ModReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskPlaceholder implements IFactoryData<TaskPlaceholder, CompoundTag> {
    public static final FactoryTaskPlaceholder INSTANCE = new FactoryTaskPlaceholder();

    private final ResourceLocation ID = new ResourceLocation(ModReference.MODID, "placeholder");

    private FactoryTaskPlaceholder() {
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public TaskPlaceholder createNew() {
        return new TaskPlaceholder();
    }

    @Override
    public TaskPlaceholder loadFromData(CompoundTag nbt) {
        TaskPlaceholder task = createNew();
        task.readFromNBT(nbt);
        return task;
    }
}
