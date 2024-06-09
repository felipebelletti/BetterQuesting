package betterquesting.api.placeholders;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class EntityPlaceholder extends Entity {
    public static final EntityType<EntityPlaceholder> TYPE = EntityType.Builder.<EntityPlaceholder>of(EntityPlaceholder::new, MobCategory.MISC)
            .sized(0.6F, 1.8F)
            .build("entity_placeholder");

    private final ItemEntity eItem;
    private CompoundTag original = new CompoundTag();

    public EntityPlaceholder(EntityType<? extends EntityPlaceholder> type, Level world) {
        super(type, world);
        eItem = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(ItemPlaceholder.placeholder));
    }

    public EntityPlaceholder setOriginalTags(CompoundTag tags) {
        this.original = tags;
        return this;
    }

    public CompoundTag getOriginalTags() {
        return this.original;
    }

    public ItemEntity getItemEntity() {
        return eItem;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tags) {
        original = tags.getCompound("original");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tags) {
        tags.put("original", this.original);
    }

    @Override
    public void tick() {
        // Custom logic for ticking, if needed
    }
}
