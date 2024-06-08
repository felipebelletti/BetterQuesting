package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.utils.DirtyPlayerMarker;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.handlers.NetQuestEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;

public class ToolboxToolReset implements IToolboxTool {
    private CanvasQuestLine gui;

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
    }

    @Override
    public void disableTool() {
    }

    @Override
    public void refresh(CanvasQuestLine gui) {
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {

        if (click != 0 || !gui.getTransform().contains(mx, my)) {
            return false;
        }

        PanelButtonQuest resetButton = gui.getButtonAt(mx, my);

        if (resetButton == null) {
            return false;
        }

        if (PanelToolController.selected.size() > 0 && !PanelToolController.selected.contains(resetButton)) {
            return false;
        }

        List<PanelButtonQuest> btnList = PanelToolController.selected.size() > 0 ? PanelToolController.selected : Collections.singletonList(resetButton);

        int[] questIDs = new int[btnList.size()];

        for (int i = 0; i < btnList.size(); i++) {
            questIDs[i] = btnList.get(i).getStoredValue().getID();
        }

        NBTTagCompound payload = new NBTTagCompound();
        payload.setIntArray("questIDs", questIDs);
        payload.setBoolean("state", false);
        payload.setInteger("action", 2);
        NetQuestEdit.sendEdit(payload);
        DirtyPlayerMarker.markDirty(Minecraft.getMinecraft().player.getUniqueID());

        return true;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        return false;
    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {
    }

    @Override
    public void drawOverlay(int mx, int my, float partialTick) {
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return null;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        return false;
    }

    @Override
    public boolean onKeyPressed(char c, int key) {
        if (PanelToolController.selected.size() <= 0 || key != Keyboard.KEY_RETURN) return false;

        List<PanelButtonQuest> btnList = PanelToolController.selected;
        int[] questIDs = new int[btnList.size()];

        for (int i = 0; i < btnList.size(); i++) {
            questIDs[i] = btnList.get(i).getStoredValue().getID();
        }

        NBTTagCompound payload = new NBTTagCompound();
        payload.setIntArray("questIDs", questIDs);
        payload.setBoolean("state", false);
        payload.setInteger("action", 2);
        NetQuestEdit.sendEdit(payload);
        DirtyPlayerMarker.markDirty(Minecraft.getMinecraft().player.getUniqueID());

        return true;
    }

    @Override
    public boolean clampScrolling() {
        return true;
    }

    @Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons) {
    }

    @Override
    public boolean useSelection() {
        return true;
    }
}
