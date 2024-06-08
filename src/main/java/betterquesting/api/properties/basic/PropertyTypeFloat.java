package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeFloat extends PropertyTypeBase<Float> {
    public PropertyTypeFloat(ResourceLocation key, Float def) {
        super(key, def);
    }

    @Override
    public Float readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NBTPrimitive)) {
            return this.getDefault();
        }

        return ((NBTPrimitive) nbt).getFloat();
    }

    @Override
    public Tag writeValue(Float value) {
        if (value == null) {
            return new NBTTagFloat(this.getDefault());
        }

        return new NBTTagFloat(value);
    }
}
