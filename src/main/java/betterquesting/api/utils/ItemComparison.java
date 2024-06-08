package betterquesting.api.utils;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for comparing ItemStacks in quests
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemComparison {
    /**
     * Check whether two stacks match with optional NBT and Ore Dictionary checks
     */
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT) {
        // Some quick checks
        if (stack1 == stack2) {
            return true;
        } else if (stack1 == null || stack2 == null) {
            return false;
        }
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }
        if (!(stack1.getDamageValue() == stack2.getDamageValue() || stack1.getItem().canBeDepleted() || stack1.getDamageValue() == ItemStack.EMPTY.getDamageValue())) {
            return false;
        }

        if (nbtCheck) {
            if (!partialNBT && !ItemStack.isSameItemSameTags(stack1, stack2)) {
                return false;
            }
            return CompareNBTTag(stack1.getTag(), stack2.getTag(), partialNBT);
        }
        return true;
    }

    public static boolean CompareNBTTag(Tag tag1, Tag tag2, boolean partial) {
        if (isEmptyNBT(tag1) != isEmptyNBT(tag2)) // One is null, the other is not
        {
            return false;
        } else if (isEmptyNBT(tag1)) // The opposing tag will always be null at this point if the other already is
        {
            return true;
        } else if (!(tag1 instanceof NumericTag && tag2 instanceof NumericTag) && tag1.getId() != tag2.getId())
            return false; // Incompatible tag types (and not a numbers we can cast)

        if (tag1 instanceof CompoundTag && tag2 instanceof CompoundTag) {
            return CompareNBTTagCompound((CompoundTag) tag1, (CompoundTag) tag2, partial);
        } else if (tag1 instanceof ListTag && tag2 instanceof ListTag) {
            ListTag list1 = (ListTag) tag1;
            ListTag list2 = (ListTag) tag2;

            if (list1.size() > list2.size() || (!partial && list1.size() != list2.size())) {
                return false; // Sample is missing requested tags or is not exact
            }

            topLoop:
            for (int i = 0; i < list1.size(); i++) {
                Tag lt1 = list1.get(i);

                for (int j = 0; j < list2.size(); j++) {
                    if (CompareNBTTag(lt1, list2.get(j), partial)) {
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested tag in list
            }
        } else if (tag1 instanceof IntArrayTag && tag2 instanceof IntArrayTag) {
            IntArrayTag list1 = (IntArrayTag) tag1;
            IntArrayTag list2 = (IntArrayTag) tag2;

            if (list1.getAsIntArray().length > list2.getAsIntArray().length || (!partial && list1.getAsIntArray().length != list2.getAsIntArray().length)) {
                return false; // Sample is missing requested tags or is not exact
            }

            List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control

            topLoop:
            for (int i : list1.getAsIntArray()) {
                for (int j = 0; j < list2.getAsIntArray().length; j++) {
                    if (!usedIdxs.contains(j) && i == list2.getAsIntArray()[j]) {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested integer in list
            }

            return true;
        } else if (tag1 instanceof ByteArrayTag && tag2 instanceof ByteArrayTag) {
            ByteArrayTag list1 = (ByteArrayTag) tag1;
            ByteArrayTag list2 = (ByteArrayTag) tag2;

            if (list1.getAsByteArray().length > list2.getAsByteArray().length || (!partial && list1.getAsByteArray().length != list2.getAsByteArray().length)) {
                return false; // Sample is missing requested tags or is not exact for non-partial match
            }

            List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control

            topLoop:
            for (byte b : list1.getAsByteArray()) {
                for (int j = 0; j < list2.getAsByteArray().length; j++) {
                    if (!usedIdxs.contains(j) && b == list2.getAsByteArray()[j]) {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested integer in list
            }
        } else if (tag1 instanceof LongArrayTag && tag2 instanceof LongArrayTag) {
            LongArrayTag list1 = (LongArrayTag) tag1;
            LongArrayTag list2 = (LongArrayTag) tag2;

            final long[] la1 = list1.getAsLongArray();
            final long[] la2 = list2.getAsLongArray();

            if (la1.length > la2.length || (!partial && la1.length != la2.length)) {
                return false; // Sample is missing requested tags or is not exact for non-partial match
            }

            List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control

            topLoop:
            for (long l : la1) {
                for (int j = 0; j < la2.length; j++) {
                    if (!usedIdxs.contains(j) && l == la2[j]) {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested integer in list
            }
        } else if (tag1 instanceof StringTag && tag2 instanceof StringTag) {
            return tag1.equals(tag2);
        } else if (tag1 instanceof NumericTag && tag2 instanceof NumericTag) // Standardize numbers to not care about format
        {
            Number num1 = NBTConverter.getNumber(tag1);
            Number num2 = NBTConverter.getNumber(tag2);

            // Check if floating point precision needs to be preserved in comparison
            if (tag1 instanceof FloatTag || tag1 instanceof DoubleTag || tag2 instanceof FloatTag || tag2 instanceof DoubleTag) {
                return num1.doubleValue() == num2.doubleValue();
            } else {
                return num1.longValue() == num2.longValue();
            }
        } else {
            return tag1.equals(tag2);
        }

        return true;
    }

    private static boolean CompareNBTTagCompound(CompoundTag reqTags, CompoundTag sample, boolean partial) {
        if (isEmptyNBT(reqTags) != isEmptyNBT(sample)) // One is null, the other is not
        {
            return false;
        } else if (isEmptyNBT(reqTags)) // The opposing tag will always be null at this point if the other already is
        {
            return true;
        }

        for (String key : reqTags.getAllKeys()) {
            if (!sample.contains(key)) {
                return false;
            } else if (!CompareNBTTag(reqTags.get(key), sample.get(key), partial)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isEmptyNBT(Tag tag) {
        return tag == null || tag.toString().isEmpty();
    }

    @Deprecated
    public static boolean OreDictionaryMatch(String name, ItemStack stack) {
        return stack != null && !StringUtils.isEmpty(name) && Ingredient.of(TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation(name))).test(stack);
    }

    @Deprecated
    public static boolean OreDictionaryMatch(String name, CompoundTag tags, ItemStack stack, boolean nbtCheck, boolean partialNBT) {
        if (!nbtCheck) return stack != null && !StringUtils.isEmpty(name) && Ingredient.of(TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation(name))).test(stack);
        return OreDictionaryMatch(Ingredient.of(TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation(name))), tags, stack, nbtCheck, partialNBT);
    }

    /**
     * Check if the item stack is part of the ore dictionary listing with the given ore ingredient while also comparing NBT tags
     */
    public static boolean OreDictionaryMatch(Ingredient ore, CompoundTag tags, ItemStack stack, boolean nbtCheck, boolean partialNBT) {
        if (stack == null || ore == null) return false;
        return ore.test(stack) && (!nbtCheck || CompareNBTTagCompound(stack.getTag(), tags, partialNBT));
    }

    /**
     * Check if the two stacks match directly or through ore dictionary listings
     */
    @Deprecated
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2) {
        return AllMatch(stack1, stack2, false, false);
    }

    /**
     * Check if the two stacks match directly or through ore dictionary listings
     */
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT) {
        if (stack1 == stack2) return true; // Both null or same instance
        if (stack1 == null) return false; // One is null the other is not
        if (nbtCheck && !CompareNBTTagCompound(stack1.getTag(), stack2.getTag(), partialNBT))
            return false; // NBT check failed
        if (StackMatch(stack1, stack2, false, false))
            return true; // Stacks are compatible (NBT was already checked at this point)

        // Final Ore Dictionary test...
        ResourceLocation key1 = ForgeRegistries.ITEMS.getKey(stack1.getItem());
        ResourceLocation key2 = ForgeRegistries.ITEMS.getKey(stack2.getItem());

        if (key1 != null && key2 != null) {
            TagKey<Item> tag1 = TagKey.create(ForgeRegistries.Keys.ITEMS, key1);
            TagKey<Item> tag2 = TagKey.create(ForgeRegistries.Keys.ITEMS, key2);

            if (tag1 != null && tag2 != null && tag1 == tag2) {
                return true; // Shared tag found
            }
        }

        return false; // No shared ore dictionary types
    }
}
