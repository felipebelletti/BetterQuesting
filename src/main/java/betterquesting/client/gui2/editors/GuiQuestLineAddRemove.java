package betterquesting.client.gui2.editors;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestDatabase;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.GuiQuest;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;


import javax.annotation.Nullable;
import java.util.List;

public class GuiQuestLineAddRemove extends GuiScreenCanvas implements IPEventListener, IVolatileScreen, INeedsRefresh {

    @Nullable
    private IQuestLine questLine;
    private final int lineID;

    private CanvasQuestDatabase canvasDB;
    private CanvasScrolling canvasQL;
    private PanelGeneric pnLoading;

    public GuiQuestLineAddRemove(Screen parent, @Nullable IQuestLine questLine) {
        super(parent);
        this.questLine = questLine;
        this.lineID = QuestLineDatabase.INSTANCE.getID(questLine);
    }

    @Override
    public void refreshGui() {
        questLine = lineID < 0 ? null : QuestLineDatabase.INSTANCE.getValue(lineID);
        canvasDB.refreshSearch();
        if (questLine != null) refreshQuestList();
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        org.lwjgl.glfw.GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_REPEAT, org.lwjgl.glfw.GLFW.GLFW_TRUE);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.edit_line2", questLine == null ? "" : QuestTranslation.translate(questLine.getUnlocalisedName()))).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));

        // === LEFT SIDE ===

        CanvasEmpty cvLeft = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 32, 8, 24), 8));
        cvBackground.addPanel(cvLeft);

        if (questLine != null) {
            PanelTextBox txtQuest = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate(questLine.getUnlocalisedName())).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
            cvLeft.addPanel(txtQuest);
        }

        canvasQL = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        cvLeft.addPanel(canvasQL);

        PanelVScrollBar scReq = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        cvLeft.addPanel(scReq);
        canvasQL.setScrollDriverY(scReq);

        // === RIGHT SIDE ==

        CanvasEmpty cvRight = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 24), 0));
        cvBackground.addPanel(cvRight);

        PanelTextBox txtDb = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.database")).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
        cvRight.addPanel(txtDb);

        PanelTextField<String> searchBox = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 24, -32), 0), "", FieldFilterString.INSTANCE);
        searchBox.setWatermark("Search...");
        cvRight.addPanel(searchBox);

        pnLoading = new PanelGeneric(new GuiTransform(GuiAlign.TOP_RIGHT, new GuiPadding(-24, 16, 8, -32), 0), PresetIcon.ICON_LOADING.getTexture(), new GuiColorStatic(0, 255, 0, 255));
        pnLoading.setEnabled(false);
        cvRight.addPanel(pnLoading);

        canvasDB = new CanvasQuestDatabase(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 32, 8, 24), 0)) {

            @Override
            protected boolean addResult(DBEntry<IQuest> entry, int index, int width) {

                PanelButtonStorage<DBEntry<IQuest>> btnAdd = new PanelButtonStorage<>(new GuiRectangle(0, index * 16, 16, 16, 0), 2, "", entry);
                btnAdd.setIcon(PresetIcon.ICON_POSITIVE.getTexture());
                btnAdd.setActive(questLine != null && questLine.getValue(entry.getID()) == null);
                this.addPanelToBuffer(btnAdd);

                PanelButtonStorage<DBEntry<IQuest>> btnEdit = new PanelButtonStorage<>(new GuiRectangle(16, index * 16, width - 32, 16, 0), 1, QuestTranslation.translate(entry.getValue().getProperty(NativeProps.NAME)), entry);
                this.addPanelToBuffer(btnEdit);

                PanelButtonStorage<DBEntry<IQuest>> btnDel = new PanelButtonStorage<>(new GuiRectangle(width - 16, index * 16, 16, 16, 0), 4, "", entry);
                btnDel.setIcon(PresetIcon.ICON_TRASH.getTexture());
                this.addPanelToBuffer(btnDel);

                return true;
            }

        };
        cvRight.addPanel(canvasDB);

        searchBox.setCallback(canvasDB::setSearchFilter);

        PanelVScrollBar scDb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 32, 0, 24), 0));
        cvRight.addPanel(scDb);
        canvasDB.setScrollDriverY(scDb);

        PanelButton btnNew = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -16, 0, 0), 0), 5, QuestTranslation.translate("betterquesting.btn.new"));
        cvRight.addPanel(btnNew);

        // === DIVIDERS ===

        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -24, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine0);

        refreshQuestList();
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        pnLoading.setEnabled(canvasDB.isSearching());
        super.drawPanel(mx, my, partialTick);
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event) {
        IPanelButton btn = event.getButton();

        if (btn.getButtonID() == 0) // Exit
        {
            Minecraft.getInstance().setScreen(this.parent);
        } else if (btn.getButtonID() == 1) // Edit
        {
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            Minecraft.getInstance().setScreen(new GuiQuest(this, entry.getID()));
        } else if (btn.getButtonID() == 2) // Add
        {
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            IQuestLineEntry qe = new QuestLineEntry(0, 0);
            int x1 = 0;
            int y1 = 0;

            topLoop:
            while (questLine != null) {
                for (DBEntry<IQuestLineEntry> qe2 : questLine.getEntries()) {
                    int x2 = qe2.getValue().getPosX();
                    int y2 = qe2.getValue().getPosY();
                    int s2 = Math.max(qe2.getValue().getSizeX(), qe2.getValue().getSizeY());

                    if (x1 >= x2 && x1 < x2 + s2 && y1 >= y2 && y1 < y2 + s2) {
                        x1 += s2;
                        y1 += s2;
                        continue topLoop; // We're in the way, move over and try again
                    }
                }

                break;
            }

            qe.setPosition(x1, y1);
            questLine.add(entry.getID(), qe);
            SendChanges();
        } else if (btn.getButtonID() == 3 && questLine != null) // Remove
        {
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            questLine.removeID(entry.getID());
            SendChanges();
        } else if (btn.getButtonID() == 4) // Delete
        {
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            CompoundTag payload = new CompoundTag();
            payload.setIntArray("questIDs", new int[]{entry.getID()});
            payload.setInteger("action", 1);
            NetQuestEdit.sendEdit(payload);
        } else if (btn.getButtonID() == 5) // New
        {
            CompoundTag payload = new CompoundTag();
            ListTag dataList = new ListTag();
            CompoundTag entry = new CompoundTag();
            entry.setInteger("questID", -1);
            dataList.appendTag(entry);
            payload.setTag("data", dataList);
            payload.setInteger("action", 3);
            NetQuestEdit.sendEdit(payload);
        } else if (btn.getButtonID() == 6) // Error resolve
        {
            CompoundTag payload = new CompoundTag();
            payload.setIntArray("questIDs", new int[]{((PanelButtonStorage<Integer>) btn).getStoredValue()});
            payload.setInteger("action", 1);
            NetQuestEdit.sendEdit(payload);
        }
    }

    private void refreshQuestList() {
        canvasQL.resetCanvas();

        if (questLine == null) {
            return;
        }

        int width = canvasQL.getTransform().getWidth();

        List<DBEntry<IQuestLineEntry>> qles = questLine.getEntries();
        for (int i = 0; i < qles.size(); i++) {
            DBEntry<IQuestLineEntry> entry = qles.get(i);

            IQuest quest = QuestDatabase.INSTANCE.getValue(entry.getID());

            if (quest == null) {
                PanelButtonStorage<Integer> btnErr = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), 6, "[ERROR]", entry.getID());
                btnErr.setActive(true);
                canvasQL.addPanel(btnErr);
                continue;
            }

            PanelButtonStorage<DBEntry<IQuest>> btnEdit = new PanelButtonStorage<>(new GuiRectangle(0, i * 16, width - 16, 16, 0), 1, QuestTranslation.translate(quest.getProperty(NativeProps.NAME)), new DBEntry<>(entry.getID(), quest));
            canvasQL.addPanel(btnEdit);

            PanelButtonStorage<DBEntry<IQuest>> btnRem = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), 3, "", new DBEntry<>(entry.getID(), quest));
            btnRem.setIcon(PresetIcon.ICON_NEGATIVE.getTexture());
            canvasQL.addPanel(btnRem);
        }
    }

    private void SendChanges() {
        if (questLine == null) return;

        CompoundTag payload = new CompoundTag();
        ListTag dataList = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.setInteger("chapterID", lineID);
        entry.setTag("config", questLine.writeToNBT(new CompoundTag(), null));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0);
        NetChapterEdit.sendEdit(payload);
    }

}
