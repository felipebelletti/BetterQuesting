package betterquesting.client.gui2.editors.tasks;

import betterquesting.EnumUtil;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.buttons.PanelButtonBoolean;
import betterquesting.api2.client.gui.controls.buttons.PanelButtonEnum;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrollingNameValue;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import betterquesting.core.ModReference;
import betterquesting.questing.tasks.TaskRetrieval;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;


public class GuiEditTaskRetrieval extends GuiScreenCanvas implements IVolatileScreen {

    private final DBEntry<IQuest> quest;
    private final TaskRetrieval task;
    private CompoundTag current;

    public GuiEditTaskRetrieval(Screen parent, DBEntry<IQuest> quest, TaskRetrieval task) {
        super(parent);
        this.quest = quest;
        this.task = task;
        current = task.writeToNBT(new CompoundTag());
    }

    @Override
    public void initPanel() {
        super.initPanel();
        org.lwjgl.glfw.GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_REPEAT, org.lwjgl.glfw.GLFW.GLFW_TRUE);

        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0),
                QuestTranslation.translate("bq_standard.title.edit_retrieval_task")).setAlignment(1)
                .setColor(PresetColor.TEXT_HEADER.getColor()));

        CanvasScrollingNameValue cvList = new CanvasScrollingNameValue(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 24, 48), 0));
        cvBackground.addPanel(cvList);
        initItems(cvList);

        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-24, 32, 16, 48), 0));
        cvBackground.addPanel(scList);
        cvList.setScrollDriverY(scList);

        PanelButton btnEditNBT = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -36, 200, 16, 0),
                -1,
                QuestTranslation.translate("bq_standard.btn.edit_nbt"));
        btnEditNBT.setClickAction(btn -> {
            Minecraft.getInstance().setScreen(new GuiNbtEditor(GuiEditTaskRetrieval.this, current, value -> current = value));
        });
        cvBackground.addPanel(btnEditNBT);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.done")) {

            @Override
            public void onButtonClick() {
                task.readFromNBT(current);
                sendChanges();
                Minecraft.getInstance().setScreen(parent);
            }

        });
    }

    private void initItems(CanvasScrollingNameValue cvList) {
        addBoolean("autoConsume", cvList);
        addBoolean("consume", cvList);
        cvList.addPanel("entryLogic", rect -> new PanelButtonEnum<>(rect, -1, EnumUtil.getEnum(current.getString("entryLogic"), EnumLogic.AND)).setCallback(value -> current.setString("entryLogic", value.name())));
        addBoolean("groupDetect", cvList);
        addBoolean("ignoreNBT", cvList);
        addBoolean("partialMatch", cvList);
        cvList.addPanel("requiredItems", rect -> new PanelButton(rect, -1, "List...").setClickAction(b -> {
            Minecraft.getInstance().setScreen(new GuiNbtEditor(mc.currentScreen, (ListTag) current.getTag("requiredItems"), null));
        }));
    }

    private void addBoolean(String name, CanvasScrollingNameValue cvList) {
        cvList.addPanel(name, rect -> new PanelButtonBoolean(rect, -1, current.getBoolean(name)).setCallback(value -> current.setBoolean(name, value)));
    }

    private static final ResourceLocation QUEST_EDIT = new ResourceLocation(ModReference.MODID, "quest_edit"); // TODO: Really need to make the native packet types accessible in the API

    private void sendChanges() {
        CompoundTag payload = new CompoundTag();
        ListTag dataList = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.setInteger("questID", quest.getID());
        entry.setTag("config", quest.getValue().writeToNBT(new CompoundTag()));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0); // Action: Update data
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, payload));
    }

}
