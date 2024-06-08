package betterquesting.items;

import betterquesting.core.BetterQuesting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

public class ItemGuideBook extends Item {
    public ItemGuideBook() {
        this.setTranslationKey("betterquesting.guide");
        this.setCreativeTab(BetterQuesting.tabQuesting);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(Level world, Player player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote && hand == EnumHand.MAIN_HAND) {
            player.openGui(BetterQuesting.instance, 1, world, 0, 0, 0);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
