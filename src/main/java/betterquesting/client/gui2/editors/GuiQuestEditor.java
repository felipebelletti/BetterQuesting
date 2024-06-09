package betterquesting.client.gui2.editors;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;


import java.util.Collections;

public class GuiQuestEditor extends GuiScreenCanvas implements IPEventListener, IVolatileScreen, INeedsRefresh {
    private final int questID;
    private IQuest quest;

    private PanelTextBox pnTitle;
    private PanelTextField<String> flName;
    private PanelTextField<String> flDesc;

    private PanelButton btnLogic;
    private PanelButton btnVis;

    public GuiQuestEditor(Screen parent, int questID) {
        super(parent);
        this.questID = questID;
    }

    @Override
    public void refreshGui() {
        quest = QuestDatabase.INSTANCE.getValue(questID);

        if (quest == null) {
            Minecraft.getInstance().setScreen(this.parent);
        } else {
            pnTitle.setText(QuestTranslation.translate("betterquesting.title.edit_quest", QuestTranslation.translate(quest.getProperty(NativeProps.NAME))));
            if (!flName.isFocused())
                flName.setText(quest.getProperty(NativeProps.NAME));
            if (!flDesc.isFocused())
                flDesc.setText(quest.getProperty(NativeProps.DESC));
            btnLogic.setText(QuestTranslation.translate("betterquesting.btn.logic") + ": " + quest.getProperty(NativeProps.LOGIC_TASK));
            btnVis.setText(QuestTranslation.translate("betterquesting.btn.show") + ": " + quest.getProperty(NativeProps.VISIBILITY));
            btnVis.setTooltip(Collections.singletonList(QuestTranslation.translate(String.format("betterquesting.btn.show.%s", quest.getProperty(NativeProps.VISIBILITY).toString().toLowerCase()))));
        }
    }

    @Override
    public void initPanel() {
        super.initPanel();

        quest = QuestDatabase.INSTANCE.getValue(questID);

        if (quest == null) {
            Minecraft.getInstance().setScreen(this.parent);
            return;
        }

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        org.lwjgl.glfw.GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_REPEAT, org.lwjgl.glfw.GLFW.GLFW_TRUE);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        pnTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.edit_quest", QuestTranslation.translate(quest.getProperty(NativeProps.NAME)))).setAlignment(1);
        pnTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(pnTitle);

        // === TEXT FIELDS ===

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));

        PanelTextBox pnName = new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -100, -60, 200, 12, 0), QuestTranslation.translate("betterquesting.gui.name"));
        pnName.setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(pnName);

        flName = new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -100, -48, 200, 16, 0), quest.getProperty(NativeProps.NAME), FieldFilterString.INSTANCE);
        flName.setMaxLength(Integer.MAX_VALUE);
        cvBackground.addPanel(flName);

        PanelTextBox pnDesc = new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -100, -28, 200, 12, 0), QuestTranslation.translate("betterquesting.gui.description"));
        pnDesc.setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(pnDesc);

        flDesc = new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -100, -16, 184, 16, 0), quest.getProperty(NativeProps.DESC), FieldFilterString.INSTANCE);
        flDesc.setMaxLength(Integer.MAX_VALUE);
        cvBackground.addPanel(flDesc);

        // === BUTTONS ===
        // NOTE: Toggle Main has been removed due to quest frames becoming much more flexible. Can still be toggled in advanced NBT tags.

        PanelButton btnDesc = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, 84, -16, 16, 16, 0), 7, "Aa");
        cvBackground.addPanel(btnDesc);

        PanelButton btnTsk = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 16, 100, 16, 0), 1, QuestTranslation.translate("betterquesting.btn.tasks"));
        cvBackground.addPanel(btnTsk);

        PanelButton btnRew = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, 0, 16, 100, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.rewards"));
        cvBackground.addPanel(btnRew);

        PanelButton btnReq = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 32, 100, 16, 0), 3, QuestTranslation.translate("betterquesting.btn.requirements"));
        cvBackground.addPanel(btnReq);

        PanelButton btnIco = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, 0, 32, 100, 16, 0), 8, QuestTranslation.translate("betterquesting.btn.icon"));
        cvBackground.addPanel(btnIco);

        btnVis = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 48, 100, 16, 0), 5, QuestTranslation.translate("betterquesting.btn.show") + ": " + quest.getProperty(NativeProps.VISIBILITY));
        btnVis.setTooltip(Collections.singletonList(QuestTranslation.translate(String.format("betterquesting.btn.show.%s", quest.getProperty(NativeProps.VISIBILITY).toString().toLowerCase()))));
        cvBackground.addPanel(btnVis);

        btnLogic = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, 0, 48, 100, 16, 0), 6, QuestTranslation.translate("betterquesting.btn.logic") + ": " + quest.getProperty(NativeProps.LOGIC_TASK));
        cvBackground.addPanel(btnLogic);

        PanelButton btnAdv = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 64, 200, 16, 0), 4, QuestTranslation.translate("betterquesting.btn.advanced"));
        cvBackground.addPanel(btnAdv);
    }

    @Override
    public boolean onMouseClick(int mx, int my, int button) {
        boolean result = super.onMouseClick(mx, my, button);
        boolean flag = false;

        if (!quest.getProperty(NativeProps.NAME).equals(flName.getValue())) {
            quest.setProperty(NativeProps.NAME, flName.getValue());
            flag = true;
        }

        if (!quest.getProperty(NativeProps.DESC).equals(flDesc.getValue())) {
            quest.setProperty(NativeProps.DESC, flDesc.getValue());
            flag = true;
        }

        if (flag) {
            sendChanges(questID);
        }

        return result;
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    private void onButtonPress(PEventButton event) {
        IPanelButton btn = event.getButton();

        switch (btn.getButtonID()) {
            case 0: // Exit
            {
                Minecraft.getInstance().setScreen(this.parent);
                break;
            }
            case 1: // Edit tasks
            {
                Minecraft.getInstance().setScreen(new betterquesting.client.gui2.editors.GuiTaskEditor(this, quest));
                break;
            }
            case 2: // Edit rewards
            {
                Minecraft.getInstance().setScreen(new betterquesting.client.gui2.editors.GuiRewardEditor(this, quest));
                break;
            }
            case 3: // Requirements
            {
                Minecraft.getInstance().setScreen(new betterquesting.client.gui2.editors.GuiPrerequisiteEditor(this, quest));
                break;
            }
            case 4: // Advanced
            {
                Minecraft.getInstance().setScreen(new GuiNbtEditor(this, quest.writeToNBT(new CompoundTag()), value -> {
                    quest.readFromNBT(value);
                    sendChanges(questID);
                }));
                break;
            }
            case 5: // Visibility
            {
                EnumQuestVisibility[] visList = EnumQuestVisibility.values();
                EnumQuestVisibility vis = quest.getProperty(NativeProps.VISIBILITY);
                vis = visList[(vis.ordinal() + 1) % visList.length];
                quest.setProperty(NativeProps.VISIBILITY, vis);
                ((PanelButton) btn).setText(QuestTranslation.translate("betterquesting.btn.show") + ": " + vis);
                sendChanges(questID);
                break;
            }
            case 6: // Logic
            {
                EnumLogic[] logicList = EnumLogic.values();
                EnumLogic logic = quest.getProperty(NativeProps.LOGIC_TASK);
                logic = logicList[(logic.ordinal() + 1) % logicList.length];
                quest.setProperty(NativeProps.LOGIC_TASK, logic);
                ((PanelButton) btn).setText(QuestTranslation.translate("betterquesting.btn.logic") + ": " + logic);
                sendChanges(questID);
                break;
            }
            case 7: // Description Editor
            {
                Minecraft.getInstance().setScreen(new GuiQuestDescEditor(this, questID, quest));
                break;
            }
            case 8: {
                Minecraft.getInstance().setScreen(new GuiItemSelection(this, quest.getProperty(NativeProps.ICON), value -> {
                    quest.setProperty(NativeProps.ICON, value);
                    sendChanges(questID);
                }));
            }
        }
    }

    public static void sendChanges(int questID) {
        IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
        CompoundTag payload = new CompoundTag();
        ListTag dataList = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.setInteger("questID", questID);
        entry.setTag("config", quest.writeToNBT(new CompoundTag()));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0);
        NetQuestEdit.sendEdit(payload);

        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen instanceof GuiQuestEditor gui) {
            gui.pnTitle.setText(QuestTranslation.translate("betterquesting.title.edit_quest", QuestTranslation.translate(quest.getProperty(NativeProps.NAME))));
            gui.flName.setText(quest.getProperty(NativeProps.NAME));
            gui.flDesc.setText(quest.getProperty(NativeProps.DESC));
        }

    }
}
