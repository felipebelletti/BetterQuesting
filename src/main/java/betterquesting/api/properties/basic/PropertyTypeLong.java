package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeLong extends PropertyTypeBase<Long> {
    public PropertyTypeLong(ResourceLocation key, Long def) {
        super(key, def);
    }

    @Override
    public Long readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NBTPrimitive)) {
            return this.getDefault();
        }

        return ((NBTPrimitive) nbt).getLong();
    }

    @Override
    public Tag writeValue(Long value) {
        if (value == null) {
            return new NBTTagLong(this.getDefault());
        }

        return new NBTTagLong(value);
    }
}
