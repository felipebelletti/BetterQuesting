package betterquesting.api.placeholders;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class EntityPlaceholder extends Entity {
    private final ItemEntity eItem;
    private CompoundTag original = new CompoundTag();

    public EntityPlaceholder(Level world) {
        super(world);
        eItem = new ItemEntity(world);
        eItem.setItem(new ItemStack(ItemPlaceholder.placeholder));
    }

    public EntityPlaceholder SetOriginalTags(CompoundTag tags) {
        this.original = tags;
        return this;
    }

    public CompoundTag GetOriginalTags() {
        return this.original;
    }

    public ItemEntity GetItemEntity() {
        return eItem;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(CompoundTag tags) {
        original = tags.getCompoundTag("original");
    }

    @Override
    protected void writeEntityToNBT(CompoundTag tags) {
        tags.setTag("original", this.original);
    }
}
