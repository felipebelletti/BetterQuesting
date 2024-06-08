package betterquesting.importers.hqm.converters.items;

import betterquesting.api.utils.BigItemStack;
import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.CompoundTag;

public class HQMItemBag implements HQMItem {
    @Override
    public BigItemStack convertItem(int damage, int amount, CompoundTag tags) {
        return new BigItemStack(BetterQuesting.lootChest, amount, damage * 25);
    }
}
