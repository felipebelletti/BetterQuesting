package betterquesting.client.renderer;

import betterquesting.api.placeholders.EntityPlaceholder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PlaceholderRenderFactory implements EntityRendererProvider<EntityPlaceholder> {

    @Override
    public EntityRenderer<EntityPlaceholder> create(Context manager) {
        return new EntityPlaceholderRenderer(manager);
    }

}
