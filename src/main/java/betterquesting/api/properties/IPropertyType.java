package betterquesting.api.properties;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public interface IPropertyType<T> {
    ResourceLocation getKey();

    T getDefault();

    T readValue(Tag nbt);

    Tag writeValue(T value);
}
