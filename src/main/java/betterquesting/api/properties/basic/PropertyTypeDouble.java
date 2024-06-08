package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeDouble extends PropertyTypeBase<Double> {
    public PropertyTypeDouble(ResourceLocation key, Double def) {
        super(key, def);
    }

    @Override
    public Double readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NBTPrimitive)) {
            return this.getDefault();
        }

        return ((NBTPrimitive) nbt).getDouble();
    }

    @Override
    public Tag writeValue(Double value) {
        if (value == null) {
            return new NBTTagDouble(this.getDefault());
        }

        return new NBTTagDouble(value);
    }
}
