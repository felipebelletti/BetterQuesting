package betterquesting.api.properties.basic;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

public class PropertyTypeEnum<E extends Enum<E>> extends PropertyTypeBase<E> {
    private final Class<E> eClazz;

    public PropertyTypeEnum(ResourceLocation key, E def) {
        super(key, def);

        eClazz = def.getDeclaringClass();
    }

    @Override
    public E readValue(Tag nbt) {
        if (nbt == null || nbt.getId() != 8) {
            return this.getDefault();
        }

        try {
            return Enum.valueOf(eClazz, ((StringTag) nbt).getAsString());
        } catch (Exception e) {
            return this.getDefault();
        }
    }

    @Override
    public Tag writeValue(E value) {
        if (value == null) {
            return StringTag.valueOf(this.getDefault().toString());
        }

        return StringTag.valueOf(value.toString());
    }
}
