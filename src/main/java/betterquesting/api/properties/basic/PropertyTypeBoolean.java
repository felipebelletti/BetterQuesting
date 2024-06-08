package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeBoolean extends PropertyTypeBase<Boolean> {
    public PropertyTypeBoolean(ResourceLocation key, Boolean def) {
        super(key, def);
    }

    @Override
    public Boolean readValue(Tag nbt) {
        if (nbt == null || nbt.getId() < 1 || nbt.getId() > 6) {
            return this.getDefault();
        }

        try {
            return ((NumericTag) nbt).getAsByte() > 0;
        } catch (Exception e) {
            return this.getDefault();
        }
    }

    @Override
    public Tag writeValue(Boolean value) {
        if (value == null) {
            return ByteTag.valueOf(this.getDefault() ? (byte) 1 : (byte) 0);
        }

        return ByteTag.valueOf(value ? (byte) 1 : (byte) 0);
    }
}
