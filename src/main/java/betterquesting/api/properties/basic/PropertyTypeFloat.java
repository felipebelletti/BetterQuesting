package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeFloat extends PropertyTypeBase<Float> {
    public PropertyTypeFloat(ResourceLocation key, Float def) {
        super(key, def);
    }

    @Override
    public Float readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NumericTag)) {
            return this.getDefault();
        }

        return ((NumericTag) nbt).getAsFloat();
    }

    @Override
    public Tag writeValue(Float value) {
        if (value == null) {
            return FloatTag.valueOf(this.getDefault());
        }

        return FloatTag.valueOf(value);
    }
}
