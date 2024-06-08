package betterquesting.api.placeholders;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPlaceholder extends Item {
    public static Item placeholder = new ItemPlaceholder();

    // Used solely for retaining info to missing items
    public ItemPlaceholder() {
        this.setTranslationKey("betterquesting.placeholder");
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!stack.hasTagCompound()) {
            tooltip.add("ERROR: Original information missing!");
            return;
        }

        tooltip.add("Original ID: " + stack.getTagCompound().getString("orig_id") + "/" + stack.getTagCompound().getInteger("orig_meta"));
    }

    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    @Override
    public void onUpdate(ItemStack stack, Level world, Entity entity, int slot, boolean held) {
        if (!stack.hasTagCompound() || !(entity instanceof Player) || world.getTotalWorldTime() % 100 != 0) // Process this only once a second
        {
            return;
        }

        Player player = (Player) entity;

        CompoundTag tags = stack.getTagCompound();
        Item i = Item.REGISTRY.getObject(new ResourceLocation(tags.getString("orig_id")));
        int m = stack.getItemDamage() > 0 ? stack.getItemDamage() : tags.getInteger("orig_meta");
        CompoundTag t = tags.hasKey("orig_tag") ? tags.getCompoundTag("orig_tag") : null;

        if (i != null) {
            ItemStack converted = new ItemStack(i, stack.getCount(), m);
            converted.setTagCompound(t);
            player.inventory.setInventorySlotContents(slot, converted);
        }
    }
}
