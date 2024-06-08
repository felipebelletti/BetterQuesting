package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeInteger extends PropertyTypeBase<Integer> {
    public PropertyTypeInteger(ResourceLocation key, Integer def) {
        super(key, def);
    }

    @Override
    public Integer readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NumericTag)) {
            return this.getDefault();
        }

        return ((NumericTag) nbt).getAsInt();
    }

    @Override
    public Tag writeValue(Integer value) {
        if (value == null) {
            return IntTag.valueOf(this.getDefault());
        }

        return IntTag.valueOf(value);
    }
}
