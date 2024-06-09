package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelEntityPreview;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.tasks.TaskTame;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.resources.ResourceLocation;

public class PanelTaskTame extends CanvasEmpty {
    private final TaskTame task;

    public PanelTaskTame(IGuiRect rect, TaskTame task) {
        super(rect);
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        ResourceLocation targetRes = new ResourceLocation(task.idName);
        Entity target;

        if (EntityList.isRegistered(targetRes)) {
            target = EntityList.createEntityByIDFromName(targetRes, Minecraft.getInstance().world);
            if (target != null) target.readFromNBT(task.targetTags);
        } else {
            target = null;
        }

        int progress = task.getUsersProgress(QuestingAPI.getQuestingUUID(Minecraft.getInstance().player));
        String tnm = target != null ? target.getName() : task.idName;

        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("bq_standard.gui.tame", tnm) + " " + progress + "/" + task.required).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));

        if (target != null)
            this.addPanel(new PanelEntityPreview(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 0, 0), 0), target).setRotationDriven(new ValueFuncIO<>(() -> 15F), new ValueFuncIO<>(() -> (float) (net.minecraft.Util.getMillis() % 30000L / 30000D * 360D))));
    }
}
