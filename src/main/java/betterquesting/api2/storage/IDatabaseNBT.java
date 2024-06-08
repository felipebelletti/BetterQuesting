package betterquesting.api2.storage;

import net.minecraft.nbt.Tag;

public interface IDatabaseNBT<T, E extends Tag, K extends Tag> extends IDatabase<T>, INBTPartial<E, Integer>, INBTProgress<K> {
}
