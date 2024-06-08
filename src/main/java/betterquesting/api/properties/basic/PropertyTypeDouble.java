package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeDouble extends PropertyTypeBase<Double> {
    public PropertyTypeDouble(ResourceLocation key, Double def) {
        super(key, def);
    }

    @Override
    public Double readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NumericTag)) {
            return this.getDefault();
        }

        return ((NumericTag) nbt).getAsDouble();
    }

    @Override
    public Tag writeValue(Double value) {
        if (value == null) {
            return DoubleTag.valueOf(this.getDefault());
        }

        return DoubleTag.valueOf(value);
    }
}
