package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Iterator;

public class CanvasItemDatabase extends CanvasSearch<ItemStack, Item> {
    private final int btnId;

    public CanvasItemDatabase(IGuiRect rect, int buttonId) {
        super(rect);
        this.btnId = buttonId;
    }

    @Override
    protected Iterator<Item> getIterator() {
        return ForgeRegistries.ITEMS.iterator();
    }

    @Override
    protected void queryMatches(Item item, String query, final ArrayDeque<ItemStack> results) {
        if (item == null || ForgeRegistries.ITEMS.getKey(item) == null) {
            return;
        } else if (item == Items.AIR) {
            results.add(ItemStack.EMPTY);
            return;
        }

        try {
            ItemStack itemStack = new ItemStack(item);
            String itemName = item.getDescriptionId().toLowerCase();
            String translatedName = QuestTranslation.translate(item.getDescriptionId()).toLowerCase();
            String registryName = ForgeRegistries.ITEMS.getKey(item).toString().toLowerCase();

            if (itemName.contains(query) || translatedName.contains(query) || registryName.contains(query)) {
                results.add(itemStack);
            } else {
                try {
                    String displayName = itemStack.getHoverName().getString().toLowerCase();

                    if (displayName.contains(query)) {
                        results.add(itemStack);
                        return;
                    }
                } catch (Exception e) {
                    BetterQuesting.logger.error("An error occurred while searching itemstack " + itemStack.toString() + " from item \"" + ForgeRegistries.ITEMS.getKey(item) + "\" (" + item.getClass().getName() + ").\nNBT: " + itemStack.save(new CompoundTag()), e);
                }
            }
        } catch (Exception e) {
            BetterQuesting.logger.error("An error occurred while searching item \"" + ForgeRegistries.ITEMS.getKey(item) + "\" (" + item.getClass().getName() + ")", e);
        }
    }

    @Override
    public boolean addResult(ItemStack stack, int index, int cachedWidth) {
        if (stack == null) return false;

        int x = (index % (cachedWidth / 18)) * 18;
        int y = (index / (cachedWidth / 18)) * 18;

        this.addPanel(new PanelItemSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, new BigItemStack(stack)).setCallback(c -> {}));

        return true;
    }
}
