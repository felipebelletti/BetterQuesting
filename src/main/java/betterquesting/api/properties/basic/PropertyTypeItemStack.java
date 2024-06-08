package betterquesting.api.properties.basic;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeItemStack extends PropertyTypeBase<BigItemStack> {
    public PropertyTypeItemStack(ResourceLocation key, BigItemStack def) {
        super(key, def);
    }

    @Override
    public BigItemStack readValue(NBTBase nbt) {
        if (nbt == null || nbt.getId() != 10) {
            return this.getDefault();
        }

        return JsonHelper.JsonToItemStack((CompoundTag) nbt);
    }

    @Override
    public NBTBase writeValue(BigItemStack value) {
        CompoundTag nbt = new CompoundTag();

        if (value == null || value.getBaseStack() == null) {
            getDefault().writeToNBT(nbt);
        } else {
            value.writeToNBT(nbt);
        }

        return nbt;
    }
}
