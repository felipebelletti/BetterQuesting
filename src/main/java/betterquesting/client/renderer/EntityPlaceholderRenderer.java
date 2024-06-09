package betterquesting.client.renderer;

import betterquesting.api.placeholders.EntityPlaceholder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EntityPlaceholderRenderer extends EntityRenderer<EntityPlaceholder> {
    protected EntityPlaceholderRenderer(Context renderManager) {
        super(renderManager);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityPlaceholder entity) {
//        return null;
        return new ResourceLocation("betterquesting", "assets/betterquesting/textures/items/placeholder.png");
    }

    // @todo: for now I have no idea how this works
//    @Override
//    public void doRender(EntityPlaceholder entity, double x, double y, double z, float yaw, float partialTick) {
//        ItemEntity item = entity.getItemEntity();
////        this.entityRenderDispatcher.render(item, x, y + 1D, z, yaw, partialTick, false); ???
////        this.renderManager.renderEntity(item, x, y + 1D, z, yaw, partialTick, false);
//    }
}
