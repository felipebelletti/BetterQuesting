package betterquesting.api.utils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Purpose built container class for holding ItemStacks larger than 127. <br>
 * <b>For storage purposes only!</b>
 */
public class BigItemStack {
    private static final Ingredient NO_INGREDIENT = Ingredient.of(ItemStack.EMPTY);
    private final ItemStack baseStack;
    public int stackSize;
    private String oreDict = "";
    private Ingredient oreIng = NO_INGREDIENT;

    public BigItemStack(ItemStack stack) {
        baseStack = stack.copy();
        this.stackSize = baseStack.getCount();
        baseStack.setCount(1);
    }

    public BigItemStack(@Nonnull Block block) {
        this(block, 1);
    }

    public BigItemStack(@Nonnull Block block, int amount) {
        this(block, amount, 0);
    }

    public BigItemStack(@Nonnull Block block, int amount, int damage) {
        this(Item.byBlock(block), amount, damage);
    }

    public BigItemStack(@Nonnull Item item) {
        this(item, 1);
    }

    public BigItemStack(@Nonnull Item item, int amount) {
        this(item, amount, 0);
    }

    public BigItemStack(@Nonnull Item item, int amount, int damage) {
        baseStack = new ItemStack(item, 1);
        baseStack.setDamageValue(damage);
        this.stackSize = amount;
    }

    /**
     * @return ItemStack this BigItemStack is based on. Changing the base stack size does NOT affect the BigItemStack's size
     */
    public ItemStack getBaseStack() {
        return baseStack;
    }

    public boolean hasOreDict() {
        return !StringUtils.isEmpty(this.oreDict) && this.oreIng.getItems().length > 0;
    }

    @Nonnull
    public String getOreDict() {
        return this.oreDict;
    }

    @Nonnull
    public Ingredient getOreIngredient() {
        return this.oreIng;
    }

    public BigItemStack setOreDict(@Nonnull String ore) {
        this.oreDict = ore;
        if (ore.length() <= 0) {
            this.oreIng = NO_INGREDIENT;
        } else {
            TagKey<Item> tagKey = TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation(ore));
            this.oreIng = Ingredient.of(tagKey);
        }
        return this;
    }

    /**
     * Shortcut method to the CompoundTag in the base ItemStack
     */
    public CompoundTag GetTagCompound() {
        return baseStack.getTag();
    }

    /**
     * Shortcut method to the CompoundTag in the base ItemStack
     */
    public void SetTagCompound(CompoundTag tags) {
        baseStack.setTag(tags);
    }

    /**
     * Shortcut method to the CompoundTag in the base ItemStack
     */
    public boolean HasTagCompound() {
        return baseStack.hasTag();
    }

    /**
     * Breaks down this big stack into smaller ItemStacks for Minecraft to use (Individual stack size is dependent on the item)
     */
    public List<ItemStack> getCombinedStacks() {
        List<ItemStack> list = new ArrayList<>();
        int tmp1 = Math.max(1, stackSize); // Guarantees this method will return at least 1 item

        while (tmp1 > 0) {
            int size = Math.min(tmp1, baseStack.getMaxStackSize());
            ItemStack stack = baseStack.copy();
            stack.setCount(size);
            list.add(stack);
            tmp1 -= size;
        }

        return list;
    }

    public BigItemStack copy() {
        BigItemStack stack = new BigItemStack(baseStack.copy());
        stack.stackSize = this.stackSize;
        stack.oreDict = this.oreDict;
        stack.oreIng = this.oreIng;
        return stack;
    }

    @Override
    public boolean equals(Object stack) {
        if (stack instanceof ItemStack) {
            return ItemStack.isSameItemSameTags(baseStack, (ItemStack) stack);
        } else {
            return super.equals(stack);
        }
    }

    public BigItemStack(@Nonnull CompoundTag tags) // Can load normal ItemStack NBTs. Does NOT deal with placeholders
    {
        CompoundTag itemNBT = tags.copy();
        itemNBT.putInt("Count", 1);
        this.stackSize = tags.getInt("Count");
        this.setOreDict(tags.getString("OreDict"));
        this.baseStack = ItemStack.of(itemNBT);
    }

    public CompoundTag writeToNBT(CompoundTag tags) {
        baseStack.save(tags);
        tags.putInt("Count", stackSize);
        tags.putString("OreDict", oreDict);
        return tags;
    }
}
