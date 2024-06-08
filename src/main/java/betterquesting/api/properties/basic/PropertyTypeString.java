package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeString extends PropertyTypeBase<String> {
    public PropertyTypeString(ResourceLocation key, String def) {
        super(key, def);
    }

    @Override
    public String readValue(Tag nbt) {
        if (nbt == null || nbt.getId() != 8) {
            return this.getDefault();
        }

        return ((StringTag) nbt).getAsString();
    }

    @Override
    public Tag writeValue(String value) {
        if (value == null) {
            return StringTag.valueOf(this.getDefault());
        }

        return StringTag.valueOf(value);
    }
}
