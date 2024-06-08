package betterquesting.api.placeholders;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * In charge of safely converting to or from placeholder objects
 */
public class PlaceholderConverter {
    public static Entity convertEntity(Entity orig, Level world, CompoundTag nbt) {
        Entity entity = orig;

        if (orig == null) {
            entity = new EntityPlaceholder(world);
            ((EntityPlaceholder) entity).SetOriginalTags(nbt);
        } else if (orig instanceof EntityPlaceholder) {
            EntityPlaceholder p = (EntityPlaceholder) orig;
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(p.GetOriginalTags().getString("id")));
            if (entityType != null) {
                Entity tmp = entityType.create(world);
                if (tmp != null) {
                    tmp.load(nbt);
                }
                entity = tmp != null ? tmp : p;
            }
        }

        return entity;
    }

    public static BigItemStack convertItem(Item item, String name, int count, int damage, String oreDict, CompoundTag nbt) {
        if (item == null) {
            BigItemStack stack = new BigItemStack(ItemPlaceholder.placeholder, count, damage).setOreDict(oreDict);
            stack.SetTagCompound(new CompoundTag());
            stack.GetTagCompound().putString("orig_id", name);
            stack.GetTagCompound().putInt("orig_meta", damage);
            if (nbt != null) stack.GetTagCompound().put("orig_tag", nbt);
            return stack;
        } else if (item == ItemPlaceholder.placeholder) {
            if (nbt != null) {
                Item restored = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("orig_id")));

                if (restored != null) {
                    BigItemStack stack = new BigItemStack(restored, count, nbt.contains("orig_meta") ? nbt.getInt("orig_meta") : damage).setOreDict(oreDict);
                    if (nbt.contains("orig_tag")) stack.SetTagCompound(nbt.getCompound("orig_tag"));

                    return stack;
                } else if (damage > 0 && !nbt.contains("orig_meta")) {
                    nbt.putInt("orig_meta", damage);
                    damage = 0;
                }
            }
        }

        BigItemStack stack = new BigItemStack(item, count, damage).setOreDict(oreDict);
        if (nbt != null) stack.SetTagCompound(nbt);

        return stack;
    }

    public static FluidStack convertFluid(Fluid fluid, String name, int amount, CompoundTag nbt) {
        if (fluid == null) {
            FluidStack stack = new FluidStack(FluidPlaceholder.STILL.get(), amount);
            CompoundTag orig = new CompoundTag();
            orig.putString("orig_id", name);
            if (nbt != null) orig.put("orig_tag", nbt);
            stack.setTag(orig);
            return stack;
        } else if (fluid == FluidPlaceholder.STILL.get() && nbt != null) {
            Fluid restored = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("orig_id")));

            if (restored != null) {
                FluidStack stack = new FluidStack(restored, amount);
                if (nbt.contains("orig_tag")) stack.setTag(nbt.getCompound("orig_tag"));
                return stack;
            }
        }

        FluidStack stack = new FluidStack(fluid, amount);
        if (nbt != null) stack.setTag(nbt);

        return stack;
    }
}
