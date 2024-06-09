package betterquesting.client.gui2.inventory;

import betterquesting.blocks.TileSubmitStation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.NotNull;

public class ContainerSubmitStation extends AbstractContainerMenu {
    private final TileSubmitStation tile;

    public ContainerSubmitStation(int containerId, Inventory playerInventory, TileSubmitStation tile) {
        super(null, containerId); // Replace 'null' with the actual container type if needed
        this.tile = tile;

        // Slot for item submission
        this.addSlot(new Slot(tile, 0, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return tile.canPlaceItem(0, stack);
            }
        });

        // Slot for returned items
        this.addSlot(new Slot(tile, 1, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, j * 18, i * 18));
            }
        }

        // Player hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, i * 18, 58));
        }
    }

    public void moveInventorySlots(int x, int y) {
        int idx = 2;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                Slot s = this.slots.remove(idx);
                this.slots.add(idx, new Slot(s.container, s.index, j * 18 + x, i * 18 + y));
                idx++;
            }
        }

        for (int i = 0; i < 9; ++i) {
            Slot s = this.slots.remove(idx);
            this.slots.add(idx, new Slot(s.container, s.index, i * 18 + x, 58 + y));
            idx++;
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int idx) {
        if (idx < 0) return ItemStack.EMPTY;

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(idx);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (idx == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (slot.mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (idx < 28) {
                if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (idx < 37) {
                if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public void moveSubmitSlot(int x, int y) {
        Slot s = this.slots.remove(0);
        this.slots.add(0, new Slot(s.container, s.index, x, y));
    }

    public void moveReturnSlot(int x, int y) {
        Slot s = this.slots.remove(1);
        this.slots.add(1, new Slot(s.container, s.index, x, y));
    }

    @Override
    public boolean stillValid(Player player) {
        return tile.stillValid(player);
    }
}
