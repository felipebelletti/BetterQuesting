package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelEntityPreview;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.client.themes.BQSTextures;
import betterquesting.questing.tasks.TaskInteractEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class PanelTaskInteractEntity extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final TaskInteractEntity task;

    public PanelTaskInteractEntity(IGuiRect rect, TaskInteractEntity task) {
        super(rect);
        this.initialRect = rect;
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PanelItemSlot itemSlot = new PanelItemSlot(new GuiTransform(GuiAlign.TOP_LEFT, 0, 0, 32, 32, 0), -1, task.targetItem, false, true);
        this.addPanel(itemSlot);

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 32, 8, 16, 16, 0), PresetIcon.ICON_RIGHT.getTexture()));

        UUID playerID = QuestingAPI.getQuestingUUID(Minecraft.getInstance().player);
        int prog = task.getUsersProgress(playerID);
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_LEFT, 0, 34, 32, 14, 0), prog + "/" + task.required).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 0, 48, 24, 24, 0), BQSTextures.HAND_LEFT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 24, 48, 24, 24, 0), BQSTextures.HAND_RIGHT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 0, 72, 24, 24, 0), BQSTextures.ATK_SYMB.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 24, 72, 24, 24, 0), BQSTextures.USE_SYMB.getTexture()));

        IGuiTexture txTick = new GuiTextureColored(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00));
        IGuiTexture txCross = new GuiTextureColored(PresetIcon.ICON_CROSS.getTexture(), new GuiColorStatic(0xFFFF0000));

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 16, 64, 8, 8, 0), task.useOffHand ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 40, 64, 8, 8, 0), task.useMainHand ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 16, 88, 8, 8, 0), task.onHit ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 40, 88, 8, 8, 0), task.onInteract ? txTick : txCross));


        ResourceLocation targetRes = new ResourceLocation(task.entityID);
        Entity target;

        if (EntityList.isRegistered(targetRes)) {
            target = EntityList.createEntityByIDFromName(targetRes, Minecraft.getInstance().world);
            if (target != null) target.readFromNBT(task.entityTags);
        } else {
            target = null;
        }

        if (target != null)
            this.addPanel(new PanelEntityPreview(new GuiTransform(GuiAlign.TOP_LEFT, 48, 0, initialRect.getWidth() - 48, 96, 0), target).setRotationDriven(new ValueFuncIO<>(() -> 15F), new ValueFuncIO<>(() -> (float) (Minecraft.getSystemTime() % 30000L / 30000D * 360D))));
        recalculateSizes();
    }
}
