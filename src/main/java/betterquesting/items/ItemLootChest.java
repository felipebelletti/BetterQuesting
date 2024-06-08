package betterquesting.items;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.NetLootClaim;
import betterquesting.questing.rewards.loot.LootGroup;
import betterquesting.questing.rewards.loot.LootRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemLootChest extends Item {
    public ItemLootChest() {
        this.setMaxStackSize(1);
        this.setTranslationKey("bq_standard.loot_chest");
        this.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(Level world, Player player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (hand != EnumHand.MAIN_HAND) return new ActionResult<>(EnumActionResult.PASS, stack);

        if (stack.getItemDamage() == 104) {
            if (world.isRemote || !(player instanceof ServerPlayer)) {
                if (!player.capabilities.isCreativeMode) stack.shrink(1);
                return new ActionResult<>(EnumActionResult.PASS, stack);
            }

            CompoundTag tag = stack.getTagCompound();
            if (tag == null) tag = new CompoundTag();

            List<BigItemStack> lootItems = new ArrayList<>();
            String lootName = tag.getString("fixedLootName");
            ListTag lootList = tag.getTagList("fixedLootList", 10);

            for (int i = 0; i < lootList.tagCount(); i++) {
                lootItems.add(new BigItemStack(lootList.getCompoundTagAt(i)));
            }

            boolean invoChanged = false;
            for (BigItemStack s1 : lootItems) {
                for (ItemStack s2 : s1.getCombinedStacks()) {
                    if (!player.inventory.addItemStackToInventory(s2)) {
                        player.dropItem(s2, true, false);
                    } else if (!invoChanged) {
                        invoChanged = true;
                    }
                }
            }

            if (invoChanged) {
                player.inventory.markDirty();
                player.inventoryContainer.detectAndSendChanges();
            }

            NetLootClaim.sendReward((ServerPlayer) player, lootName, lootItems.toArray(new BigItemStack[0]));
        } else if (stack.getItemDamage() == 103) {
            if (world.isRemote || !(player instanceof ServerPlayer)) {
                if (!player.capabilities.isCreativeMode) stack.shrink(1);
                return new ActionResult<>(EnumActionResult.PASS, stack);
            }

            LootContext lootcontext = (new LootContext.Builder(((ServerPlayer) player).getServerWorld())).withLootedEntity(player).withPlayer(player).withLuck(player.getLuck()).build();
            String loottable = (stack.getTagCompound() != null && stack.getTagCompound().hasKey("loottable", 8)) ? stack.getTagCompound().getString("loottable") : "minecraft:chests/simple_dungeon";

            List<BigItemStack> loot = new ArrayList<>();
            for (ItemStack itemstack : player.world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(loottable)).generateLootForPools(player.getRNG(), lootcontext)) {
                loot.add(new BigItemStack(itemstack));
            }

            boolean invoChanged = false;
            for (BigItemStack s1 : loot) {
                for (ItemStack s2 : s1.getCombinedStacks()) {
                    if (!player.inventory.addItemStackToInventory(s2)) {
                        player.dropItem(s2, true, false);
                    } else if (!invoChanged) {
                        invoChanged = true;
                    }
                }
            }

            if (invoChanged) {
                player.inventory.markDirty();
                player.inventoryContainer.detectAndSendChanges();
            }

            NetLootClaim.sendReward((ServerPlayer) player, "Loot", loot.toArray(new BigItemStack[0]));
        } else if (stack.getItemDamage() >= 102) {
            if (QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(player)) {
                player.openGui(BetterQuesting.instance, 2, world, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
            return new ActionResult<>(EnumActionResult.PASS, stack);
        } else if (!world.isRemote) {
            float rarity = stack.getItemDamage() == 101 ? itemRand.nextFloat() : Mth.clamp(stack.getItemDamage(), 0, 100) / 100F;
            LootGroup group = LootRegistry.INSTANCE.getWeightedGroup(rarity, itemRand);
            List<BigItemStack> loot = new ArrayList<>();
            String title = "No Loot Setup";

            if (group != null) {
                title = group.name;
                List<BigItemStack> tmp = group.getRandomReward(itemRand);
                if (tmp != null) loot.addAll(tmp);
            }

            boolean invoChanged = false;
            for (BigItemStack s1 : loot) {
                for (ItemStack s2 : s1.getCombinedStacks()) {
                    if (!player.inventory.addItemStackToInventory(s2)) {
                        player.dropItem(s2, true, false);
                    } else if (!invoChanged) {
                        invoChanged = true;
                    }
                }
            }

            if (invoChanged) {
                player.inventory.markDirty();
                player.inventoryContainer.detectAndSendChanges();
            }

            if (player instanceof ServerPlayer) {
                NetLootClaim.sendReward((ServerPlayer) player, title, loot.toArray(new BigItemStack[0]));
            }
        }

        if (!player.capabilities.isCreativeMode) stack.shrink(1);

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private List<ItemStack> subItems = null;

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("rawtypes")
    public void getSubItems(@Nonnull CreativeModeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        if (tab != CreativeModeTabs.SEARCH && tab != this.getCreativeTab()) return;
        if (subItems != null) // CACHED ITEMS
        {
            list.addAll(subItems);
            return;
        }

        subItems = new ArrayList<>();

        // NORMAL RARITY
        CompoundTag tag = new CompoundTag();
        tag.setBoolean("hideLootInfo", true);
        for (int i = 0; i < 5; i++) {
            ItemStack tmp = new ItemStack(this, 1, 25 * i);
            tmp.setTagCompound(tag.copy());
            subItems.add(tmp);
        }

        // TRUE RANDOM
        ItemStack tmp = new ItemStack(this, 1, 101);
        tmp.setTagCompound(tag.copy());
        subItems.add(tmp);

        // EDIT
        subItems.add(new ItemStack(this, 1, 102));

        // VANILLA LOOT TABLE
        tag = new CompoundTag();
        tag.setBoolean("hideLootInfo", true);
        tag.setString("loottable", "minecraft:chests/simple_dungeon");
        ItemStack lootStack = new ItemStack(this, 1, 103);
        lootStack.setTagCompound(tag);
        subItems.add(lootStack);

        // FIXED ITEM SET
        tag = new CompoundTag();
        tag.setBoolean("hideLootInfo", true);
        ListTag tagList = new ListTag();
        tagList.appendTag(new BigItemStack(Blocks.STONE).writeToNBT(new CompoundTag()));
        ItemStack fixedLootStack = new ItemStack(this, 1, 104);
        tag.setTag("fixedLootList", tagList);
        tag.setString("fixedLootName", "Item Set");
        fixedLootStack.setTagCompound(tag);
        subItems.add(fixedLootStack);

        list.addAll(subItems);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.getItemDamage() == 102;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        CompoundTag tag = stack.getTagCompound();
        boolean hideTooltip = tag == null || !tag.getBoolean("hideLootInfo");
        if (hideTooltip && !QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return;

        if (stack.getItemDamage() == 104) {
            if (tag == null) return;
            tooltip.add(QuestTranslation.translate("bq_standard.tooltip.fixed_loot", tag.getString("fixedLootName")));
            tooltip.add(QuestTranslation.translate("bq_standard.tooltip.fixed_loot_size", tag.getTagList("fixedLootList", 10).tagCount()));
        } else if (stack.getItemDamage() == 103) {
            if (tag == null) return;
            tooltip.add(QuestTranslation.translate("bq_standard.tooltip.loot_table", tag.getString("loottable")));
        } else if (stack.getItemDamage() > 101) {
            tooltip.add(QuestTranslation.translate("betterquesting.btn.edit"));
        } else {
            if (stack.getItemDamage() == 101) {
                tooltip.add(QuestTranslation.translate("bq_standard.tooltip.loot_chest", "???"));
            } else {
                tooltip.add(QuestTranslation.translate("bq_standard.tooltip.loot_chest", Mth.clamp(stack.getItemDamage(), 0, 100) + "%"));
            }
        }
    }
}
