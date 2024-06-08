package betterquesting.api2.storage;

import net.minecraft.nbt.Tag;

// TODO: Replace usage with INBTSerializable?
public interface INBTSaveLoad<T extends Tag> {
    T writeToNBT(T nbt);

    void readFromNBT(T nbt);
}
