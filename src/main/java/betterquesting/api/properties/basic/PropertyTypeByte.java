package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeByte extends PropertyTypeBase<Byte> {
    public PropertyTypeByte(ResourceLocation key, Byte def) {
        super(key, def);
    }

    @Override
    public Byte readValue(Tag nbt) {
        if (nbt == null || !(nbt instanceof NumericTag)) {
            return this.getDefault();
        }

        return ((NumericTag) nbt).getAsByte();
    }

    @Override
    public Tag writeValue(Byte value) {
        if (value == null) {
            return ByteTag.valueOf(this.getDefault());
        }

        return ByteTag.valueOf(value);
    }
}
