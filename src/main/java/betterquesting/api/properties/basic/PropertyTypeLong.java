package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeLong extends PropertyTypeBase<Long> {
    public PropertyTypeLong(ResourceLocation key, Long def) {
        super(key, def);
    }

    @Override
    public Long readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NumericTag)) {
            return this.getDefault();
        }

        return ((NumericTag) nbt).getAsLong();
    }

    @Override
    public Tag writeValue(Long value) {
        if (value == null) {
            return LongTag.valueOf(this.getDefault());
        }

        return LongTag.valueOf(value);
    }
}
