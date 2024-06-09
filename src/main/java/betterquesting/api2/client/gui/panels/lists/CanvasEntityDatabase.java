package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CanvasEntityDatabase extends CanvasSearch<EntityType<?>, EntityType<?>> {
    private final int btnId;

    public CanvasEntityDatabase(IGuiRect rect, int buttonId) {
        super(rect);
        this.btnId = buttonId;
    }

    @Override
    protected Iterator<EntityType<?>> getIterator() {
        List<EntityType<?>> list = new ArrayList<>(ForgeRegistries.ENTITY_TYPES.getValues());
        list.sort((o1, o2) -> o1.getDescriptionId().compareToIgnoreCase(o2.getDescriptionId()));
        return list.iterator();
    }

    @Override
    protected void queryMatches(EntityType<?> entityType, String query, final ArrayDeque<EntityType<?>> results) {
        if (entityType == null || ForgeRegistries.ENTITY_TYPES.getKey(entityType) == null) {
            return;
        }

        String qlc = query.toLowerCase();
        String registryName = ForgeRegistries.ENTITY_TYPES.getKey(entityType).toString().toLowerCase();
        String entityName = entityType.getDescriptionId().toLowerCase();

        if (registryName.contains(qlc) || entityName.contains(qlc)) {
            results.add(entityType);
        }
    }

    @Override
    protected boolean addResult(EntityType<?> entityType, int index, int cachedWidth) {
        if (entityType == null) {
            return false;
        }

        String entityName = entityType.getDescriptionId();
        this.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, index * 16, cachedWidth, 16, 0), btnId, entityName, entityType));

        return true;
    }
}
