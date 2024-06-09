package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasHoverTray;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.popups.PopChoice;
import betterquesting.api2.client.gui.popups.PopContextMenu;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.GuiQuestEditor;
import betterquesting.client.gui2.editors.GuiQuestLinesEditor;
import betterquesting.client.gui2.editors.designer.GuiDesigner;
import betterquesting.handlers.ConfigHandler;
import betterquesting.network.handlers.NetQuestAction;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.*;

public class GuiQuestLines extends GuiScreenCanvas implements IPEventListener, INeedsRefresh {

    private ScrollPosition scrollPosition;

    private IQuestLine selectedLine = null;
    private static int selectedLineId = -1;

    private final List<Tuple<DBEntry<IQuestLine>, Integer>> visChapters = new ArrayList<>();

    private CanvasQuestLine cvQuest;

    // Keep these separate for now
    private static CanvasHoverTray cvChapterTray;
    private static CanvasHoverTray cvDescTray;
    private static CanvasHoverTray cvFrame;

    private CanvasScrolling cvDesc;
    private PanelVScrollBar scDesc;
    private CanvasScrolling cvLines;
    private PanelVScrollBar scLines;

    private PanelGeneric icoChapter;
    private PanelTextBox txTitle;
    private PanelTextBox txDesc;
    private PanelTextBox completionText;

    private PanelButton claimAll;

    private PanelButton btnDesign;

    private static boolean trayLock;
    private static boolean viewMode;
    private int questsCompleted = 0;
    private int totalQuests = 0;

    private final List<PanelButtonStorage<DBEntry<IQuestLine>>> btnListRef = new ArrayList<>();

    public GuiQuestLines(Screen parent) {
        super(parent);
        trayLock = BQ_Settings.lockTray;
        viewMode = BQ_Settings.viewMode;

        if (scrollPosition == null) {
            scrollPosition = new ScrollPosition(0);
        }
    }

    @Override
    public void refreshGui() {
        refreshChapterVisibility();
        refreshContent();
    }

    @Override
    public void initPanel() {
        super.initPanel();

        GuiHome.bookmark = this;
        // If we move to quest gui - we set skip home to true
        if (!BQ_Settings.skipHome) {
            ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Skip Home", false).set(true);
            ConfigHandler.config.save();
            BQ_Settings.skipHome = true;
        }

        if (selectedLineId >= 0) {
            selectedLine = QuestLineDatabase.INSTANCE.getValue(selectedLineId);
            if (selectedLine == null) selectedLineId = -1;
        } else {
            selectedLine = null;
        }

        boolean canEdit = QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player);
        boolean preOpen = false;
        // First time load, if tray locked - let the tray open
        if (trayLock && cvChapterTray == null && cvDescTray == null) preOpen = true;
        else if (trayLock && cvChapterTray != null && cvChapterTray.isTrayOpen()) preOpen = true;
        else if (trayLock && cvDescTray != null && cvDescTray.isTrayOpen()) preOpen = true;

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);

        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelButton btnExit = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 8, -24, 32, 16, 0), -1, "").setIcon(PresetIcon.ICON_PG_PREV.getTexture());
        btnExit.setClickAction((b) -> Minecraft.getInstance().setScreen(parent));
        btnExit.setTooltip(Collections.singletonList(QuestTranslation.translate("gui.back")));
        cvBackground.addPanel(btnExit);

        // Search Button
        PanelButton btnSearch = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 8, -40, 32, 16, 0), -1, "").setIcon(PresetIcon.ICON_ZOOM.getTexture());
        btnSearch.setClickAction(this::openSearch);
        btnSearch.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.gui.search")));
        cvBackground.addPanel(btnSearch);

        if (canEdit) {
            PanelButton btnEdit = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 8, -56, 16, 16, 0), -1, "").setIcon(PresetIcon.ICON_GEAR.getTexture());
            btnEdit.setClickAction((b) -> Minecraft.getInstance().setScreen(new GuiQuestLinesEditor(this)));
            btnEdit.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.edit")));
            cvBackground.addPanel(btnEdit);

            btnDesign = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 24, -56, 16, 16, 0), -1, "").setIcon(PresetIcon.ICON_SORT.getTexture());
            btnDesign.setClickAction($ -> Minecraft.getInstance().setScreen(new GuiDesigner(this, selectedLine)));
            btnDesign.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.designer")));
            cvBackground.addPanel(btnDesign);
            btnDesign.setActive(selectedLine != null);
        }

        txTitle = new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0F), new GuiPadding(60, 12, 0, -24), 0), "");
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);

        completionText = new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0F), new GuiPadding(214, 12, -154, -24), 0), "");
        completionText.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(completionText);


        icoChapter = new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, 40, 8, 16, 16, 0), null);
        cvBackground.addPanel(icoChapter);

        cvFrame = new CanvasHoverTray(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(40 + 150 + 24, 24, 8, 8), 0), new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(40, 24, 8, 8), 0), PresetTexture.AUX_FRAME_0.getTexture());
        cvFrame.setManualOpen(true);
        cvBackground.addPanel(cvFrame);
        cvFrame.setTrayState(!preOpen, 1);

        // === TRAY STATE ===

        boolean chapterTrayOpened = trayLock && cvChapterTray != null && cvChapterTray.isTrayOpen();
        boolean descTrayOpened = trayLock && cvDescTray != null && cvDescTray.isTrayOpen();
        if (preOpen && !chapterTrayOpened && !descTrayOpened) {
            chapterTrayOpened = true;
        }

        // === CHAPTER TRAY ===

        cvChapterTray = new CanvasHoverTray(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(40, 24, -24, 8), -1), new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(40, 24, -40 - 150 - 24, 8), -1), PresetTexture.PANEL_INNER.getTexture());
        cvChapterTray.setManualOpen(true);
        cvChapterTray.setOpenAction(() -> {
            cvDescTray.setTrayState(false, 200);
            cvFrame.setTrayState(false, 200);
            buildChapterList();
        });
        cvBackground.addPanel(cvChapterTray);

        cvLines = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 16, 8), 0));
        cvChapterTray.getCanvasOpen().addPanel(cvLines);

        scLines = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-16, 8, 8, 8), 0));
        cvLines.setScrollDriverY(scLines);
        cvChapterTray.getCanvasOpen().addPanel(scLines);

        // === DESCRIPTION TRAY ===

        cvDescTray = new CanvasHoverTray(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(40, 24, -24, 8), -1), new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(40, 24, -40 - 150 - 24, 8), -1), PresetTexture.PANEL_INNER.getTexture());
        cvDescTray.setManualOpen(true);
        cvDescTray.setOpenAction(() -> {
            cvChapterTray.setTrayState(false, 200);
            cvFrame.setTrayState(false, 200);
            cvDesc.resetCanvas();
            if (selectedLine != null) {
                txDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0, 0), QuestTranslation.translate(selectedLine.getUnlocalisedDescription()), true);
                txDesc.setColor(PresetColor.TEXT_AUX_0.getColor());//.setFontSize(10);
                cvDesc.addCulledPanel(txDesc, false);
                cvDesc.refreshScrollBounds();
                scDesc.setEnabled(cvDesc.getScrollBounds().getHeight() > 0);
            } else {
                scDesc.setEnabled(false);
            }
        });
        cvBackground.addPanel(cvDescTray);

        cvDesc = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 20, 8), 0));
        cvDescTray.getCanvasOpen().addPanel(cvDesc);

        scDesc = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-16, 8, 8, 8), 0));
        cvDesc.setScrollDriverY(scDesc);
        cvDescTray.getCanvasOpen().addPanel(scDesc);

        // === LEFT SIDEBAR ===

        PanelButton btnTrayToggle = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 8, 24, 32, 16, 0), -1, "");
        btnTrayToggle.setIcon(PresetIcon.ICON_BOOKMARK.getTexture(), selectedLineId < 0 && !chapterTrayOpened ? new GuiColorPulse(0xFFFFFFFF, 0xFF444444, 2F, 0F) : new GuiColorStatic(0xFFFFFFFF), 0);
        btnTrayToggle.setClickAction((b) -> {
            cvFrame.setTrayState(cvChapterTray.isTrayOpen(), 200);
            cvChapterTray.setTrayState(!cvChapterTray.isTrayOpen(), 200);
            btnTrayToggle.setIcon(PresetIcon.ICON_BOOKMARK.getTexture());
        });
        btnTrayToggle.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.title.quest_lines")));
        cvBackground.addPanel(btnTrayToggle);

        PanelButton btnDescToggle = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 8, 40, 32, 16, 0), -1, "").setIcon(PresetIcon.ICON_DESC.getTexture());
        btnDescToggle.setClickAction((b) -> {
            cvFrame.setTrayState(cvDescTray.isTrayOpen(), 200);
            cvDescTray.setTrayState(!cvDescTray.isTrayOpen(), 200);
        });
        btnDescToggle.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.gui.description")));
        cvBackground.addPanel(btnDescToggle);

        PanelButton fitView = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 8, 72, 32, 16, -2), 5, "");
        fitView.setIcon(PresetIcon.ICON_BOX_FIT.getTexture());
        fitView.setClickAction((b) -> {
            if (cvQuest.getQuestLine() != null) cvQuest.fitToWindow();
        });
        fitView.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.zoom_fit")));
        cvBackground.addPanel(fitView);

        claimAll = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 8, 56, 32, 16, -2), -1, "");
        claimAll.setIcon(PresetIcon.ICON_CHEST_ALL.getTexture());
        claimAll.setClickAction((b) -> {
            if (BQ_Settings.claimAllConfirmation) {
                openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.claim_all_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.claim_all_confirm"), PresetIcon.ICON_CHEST_ALL.getTexture(), integer -> {
                    if (integer == 1) {
                        ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Claim all requires confirmation", true).set(false);
                        ConfigHandler.config.save();
                        ConfigHandler.initConfigs();
                    }
                    if (integer <= 1) {
                        claimAll();
                    }
                }, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("betterquesting.gui.yes_always"), QuestTranslation.translate("gui.no")));
            } else {
                claimAll();
            }
        });
        claimAll.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.claim_all")));
        cvBackground.addPanel(claimAll);

        // The Jester1147 button
        PanelButton btnTrayLock = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 8, 88, 32, 16, -2), -1, "").setIcon(trayLock ? PresetIcon.ICON_LOCKED.getTexture() : PresetIcon.ICON_UNLOCKED.getTexture());
        btnTrayLock.setClickAction((b) -> {
            trayLock = !trayLock;
            b.setIcon(trayLock ? PresetIcon.ICON_LOCKED.getTexture() : PresetIcon.ICON_UNLOCKED.getTexture());
            ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Lock Tray", false).set(trayLock);
            ConfigHandler.config.save();
            ConfigHandler.initConfigs();
        });
        btnTrayLock.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.lock_tray")));
        cvBackground.addPanel(btnTrayLock);

        // View Mode Button
        PanelButton btnViewMode = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 8, 104, 32, 16, -2), -1, "").setIcon(viewMode ? PresetIcon.ICON_VISIBILITY_NORMAL.getTexture() : PresetIcon.ICON_VISIBILITY_HIDDEN.getTexture());
        btnViewMode.setClickAction((b) -> {
            viewMode = !viewMode;
            b.setIcon(viewMode ? PresetIcon.ICON_VISIBILITY_NORMAL.getTexture() : PresetIcon.ICON_VISIBILITY_HIDDEN.getTexture());
            ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "View mode", false).set(viewMode);
            ConfigHandler.config.save();
            ConfigHandler.initConfigs();
            refreshGui();
        });
        btnViewMode.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.view_mode")));
        cvBackground.addPanel(btnViewMode);

        // === CHAPTER VIEWPORT ===

        CanvasQuestLine oldCvQuest = cvQuest;
        cvQuest = new CanvasQuestLine(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), 2);
        CanvasEmpty cvQuestPopup = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0)) {
            @Override
            public boolean onMouseClick(int mx, int my, int click) {
                if (cvQuest.getQuestLine() == null || !this.getTransform().contains(mx, my)) {
                    return false;
                }
                if (click == 1) {
                    Font fr = Minecraft.getInstance().fontRenderer;
                    boolean questExistsUnderMouse = cvQuest.getButtonAt(mx, my) != null;
                    int maxWidth = questExistsUnderMouse ? RenderUtils.getStringWidth(QuestTranslation.translate("betterquesting.btn.share_quest"), fr) : 0;
                    if (canEdit) {
                        maxWidth = Math.max(maxWidth, Math.max(RenderUtils.getStringWidth(QuestTranslation.translate("betterquesting.btn.edit"), fr),
                                RenderUtils.getStringWidth(QuestTranslation.translate("betterquesting.btn.designer"), fr)));
                    }
                    PopContextMenu popup = new PopContextMenu(new GuiRectangle(mx, my, maxWidth + 12, questExistsUnderMouse ? 64 : 16), true);
                    if (canEdit) {
                        if (questExistsUnderMouse) {
                            GuiQuestEditor editor = new GuiQuestEditor(new GuiQuestLines(parent), cvQuest.getButtonAt(mx, my).getStoredValue().getID());
                            Runnable actionEditor = () -> Minecraft.getInstance().setScreen(editor);
                            popup.addButton(QuestTranslation.translate("betterquesting.btn.edit"), null, actionEditor);
                        }
                        GuiDesigner designer = new GuiDesigner(new GuiQuestLines(parent), cvQuest.getQuestLine());
                        Runnable actionDesigner = () -> Minecraft.getInstance().setScreen(designer);
                        popup.addButton(QuestTranslation.translate("betterquesting.btn.designer"), null, actionDesigner);
                    }
                    if (questExistsUnderMouse) {
                        Runnable questSharer = () -> {
                            mc.player.sendChatMessage("betterquesting.msg.share_quest:" + cvQuest.getButtonAt(mx, my).getStoredValue().getID());
                            Minecraft.getInstance().setScreen(null);
                        };
                        popup.addButton(QuestTranslation.translate("betterquesting.btn.share_quest"), null, questSharer);

                        Runnable questId = () -> {
                            String id = String.valueOf(cvQuest.getButtonAt(mx, my).getStoredValue().getID());
                            try {
                                Screen.setClipboardString(id);
                                mc.player.sendMessage(new TextComponentTranslation("betterquesting.msg.copy_quest_copied", id));
                                closePopup();
                            } catch (IllegalStateException e) {
                                mc.player.sendMessage(new TextComponentTranslation("betterquesting.msg.copy_quest_failed", id));
                            }
                        };
                        popup.addButton(QuestTranslation.translate("betterquesting.btn.copy_id"), null, questId);
                    }
                    openPopup(popup);
                    return true;
                }
                return false;
            }
        };
        cvFrame.addPanel(cvQuest);
        cvFrame.addPanel(cvQuestPopup);

        if (selectedLine != null) {
            cvQuest.setQuestLine(selectedLine);

            if (oldCvQuest != null) {
                cvQuest.setZoom(oldCvQuest.getZoom());
                cvQuest.setScrollX(oldCvQuest.getScrollX());
                cvQuest.setScrollY(oldCvQuest.getScrollY());
                cvQuest.refreshScrollBounds();
                cvQuest.updatePanelScroll();
            }

            refreshQuestCompletion();
            txTitle.setText(QuestTranslation.translate(selectedLine.getUnlocalisedName()));
            icoChapter.setTexture(new OreDictTexture(1F, selectedLine.getProperty(NativeProps.ICON), false, true), null);
        }

        // === MISC ===

        cvChapterTray.setTrayState(chapterTrayOpened, 1);
        cvDescTray.setTrayState(descTrayOpened, 1);

        refreshChapterVisibility();
        refreshClaimAll();
        refreshDesigner();

        cvLines.setScrollY(scrollPosition.getChapterScrollY());
        cvLines.updatePanelScroll();
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        try {
            return super.onMouseRelease(mx, my, click);
        } finally {
            if (cvLines != null) {
                scrollPosition.setChapterScrollY(cvLines.getScrollY());
            }
        }
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        try {
            return super.onMouseScroll(mx, my, scroll);
        } finally {
            if (cvLines != null) {
                scrollPosition.setChapterScrollY(cvLines.getScrollY());
            }
        }
    }

    private void claimAll() {
        if (cvQuest.getQuestButtons().isEmpty()) {
            return;
        }
        List<Integer> claimIdList = new ArrayList<>();
        for (PanelButtonQuest pbQuest : cvQuest.getQuestButtons()) {
            IQuest q = pbQuest.getStoredValue().getValue();
            if (q.getRewards().size() > 0 && q.canClaim(mc.player)) {
                claimIdList.add(pbQuest.getStoredValue().getID());
            }
        }

        int[] cIDs = new int[claimIdList.size()];
        for (int i = 0; i < cIDs.length; i++) {
            cIDs[i] = claimIdList.get(i);
        }

        NetQuestAction.requestClaim(cIDs);
        claimAll.setIcon(PresetIcon.ICON_CHEST_ALL.getTexture(), new GuiColorStatic(0xFF444444), 0);
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    // TODO: Change CanvasQuestLine to NOT need these panel events anymore
    private void onButtonPress(PEventButton event) {
        Minecraft mc = Minecraft.getInstance();
        IPanelButton btn = event.getButton();

        if (btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Quest Instance Select
        {
            @SuppressWarnings("unchecked")
            DBEntry<IQuest> quest = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            GuiHome.bookmark = new GuiQuest(this, quest.getID());

            Minecraft.getInstance().setScreen(GuiHome.bookmark);
        }
    }

    private void refreshChapterVisibility() {
        boolean canEdit = QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player);
        List<DBEntry<IQuestLine>> lineList = QuestLineDatabase.INSTANCE.getSortedEntries();
        this.visChapters.clear();
        UUID playerID = QuestingAPI.getQuestingUUID(mc.player);

        for (DBEntry<IQuestLine> dbEntry : lineList) {
            IQuestLine ql = dbEntry.getValue();
            EnumQuestVisibility vis = ql.getProperty(NativeProps.VISIBILITY);
            if (!canEdit && vis == EnumQuestVisibility.HIDDEN) continue;

            boolean show = false;
            boolean unlocked = false;
            boolean complete = false;
            boolean allComplete = true;
            boolean pendingClaim = false;

            if (canEdit) {
                show = true;
                unlocked = true;
                complete = true;
            }

            if (viewMode) {
                show = true;
            }

            for (DBEntry<IQuestLineEntry> qID : ql.getEntries()) {
                IQuest q = QuestDatabase.INSTANCE.getValue(qID.getID());
                if (q == null) continue;
                if (allComplete && !isQuestCompletedForQuestline(playerID, q)) allComplete = false;
                if (!pendingClaim && q.canClaimBasically(mc.player)) pendingClaim = true;
                if (!unlocked && q.isUnlocked(playerID)) unlocked = true;
                if (!complete && q.isComplete(playerID)) complete = true;
                if (!show && QuestCache.isQuestShown(q, playerID, mc.player)) show = true;
                if (unlocked && complete && show && pendingClaim && !allComplete) break;
            }

            if (vis == EnumQuestVisibility.COMPLETED && !complete) {
                continue;
            } else if (vis == EnumQuestVisibility.UNLOCKED && !unlocked) {
                continue;
            }

            int val = pendingClaim ? 1 : 0;
            if (allComplete) val |= 2;
            if (!show) val |= 4;

            visChapters.add(new Tuple<>(dbEntry, val));
        }

        if (cvChapterTray.isTrayOpen()) buildChapterList();
    }

    private boolean isQuestCompletedForQuestline(UUID playerID, @Nonnull IQuest q) {
        if (q.isComplete(playerID)) return true; // Completed quest
        if (q.getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.HIDDEN) return true; // Always hidden quest
        if (q.getProperty(NativeProps.LOGIC_QUEST) == EnumLogic.XOR) { // Quest with choice
            int reqCount = 0;
            for (int qRequirementId : q.getRequirements()) {
                IQuest quest = QuestDatabase.INSTANCE.getValue(qRequirementId);
                if (quest.isComplete(playerID)) reqCount++;
                if (reqCount == 2) return true;
            }
        }

        return false;
    }

    private void buildChapterList() {
        cvLines.resetCanvas();
        btnListRef.clear();

        int listW = cvLines.getTransform().getWidth();

        for (int n = 0; n < visChapters.size(); n++) {
            DBEntry<IQuestLine> entry = visChapters.get(n).getFirst();
            int vis = visChapters.get(n).getSecond();

            cvLines.addPanel(new PanelGeneric(new GuiRectangle(0, n * 16, 16, 16, 0), new OreDictTexture(1F, entry.getValue().getProperty(NativeProps.ICON), false, true)));

            if ((vis & 1) > 0) {
                cvLines.addPanel(new PanelGeneric(new GuiRectangle(8, n * 16 + 8, 8, 8, -1), new GuiTextureColored(PresetIcon.ICON_NOTICE.getTexture(), new GuiColorStatic(0xFFFFFF00))));
            } else if ((vis & 2) > 0) {
                cvLines.addPanel(new PanelGeneric(new GuiRectangle(8, n * 16 + 8, 8, 8, -1), new GuiTextureColored(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00))));
            }
            PanelButtonStorage<DBEntry<IQuestLine>> btnLine = new PanelButtonStorage<>(new GuiRectangle(16, n * 16, listW - 16, 16, 0), 1, QuestTranslation.translate(entry.getValue().getUnlocalisedName()), entry);
            btnLine.setTextAlignment(0);
            btnLine.setActive((vis & 4) == 0 && entry.getID() != selectedLineId);
            btnLine.setCallback((q) -> {
                btnListRef.forEach((b) -> {
                    if (b.getStoredValue().getID() == selectedLineId) b.setActive(true);
                });
                btnLine.setActive(false);
                selectedLine = q.getValue();
                selectedLineId = q.getID();
                cvQuest.setQuestLine(q.getValue());
                icoChapter.setTexture(new OreDictTexture(1F, q.getValue().getProperty(NativeProps.ICON), false, true), null);
                refreshQuestCompletion();
                txTitle.setText(QuestTranslation.translate(q.getValue().getUnlocalisedName()));
                if (!trayLock) {
                    cvFrame.setTrayState(true, 200);
                    cvChapterTray.setTrayState(false, 200);
                    cvQuest.fitToWindow();
                }
                refreshClaimAll();
                refreshDesigner();
            });
            cvLines.addPanel(btnLine);
            btnListRef.add(btnLine);
        }

        cvLines.refreshScrollBounds();
        scLines.setEnabled(cvLines.getScrollBounds().getHeight() > 0);
    }

    private void refreshQuestCompletion() {
        Player player = mc.player;
        UUID playerUUId = QuestingAPI.getQuestingUUID(player);

        if (selectedLine == null) {
            return;
        }

        questsCompleted = 0;
        totalQuests = 0;

        for (DBEntry<IQuestLineEntry> entry : selectedLine.getEntries()) {
            IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(entry.getID());

            if (quest.getProperty(NativeProps.LOGIC_QUEST) == EnumLogic.XOR) {
                // Subtract the number of requirements - 1 to simulate only doing 1 task for XOR requirements
                totalQuests = totalQuests - Math.max(0, quest.getRequirements().length - 1);
            }

            totalQuests++;

            if (quest.isComplete(playerUUId)) {
                questsCompleted++;
            }
        }
        completionText.setText(QuestTranslation.translate("betterquesting.title.completion", questsCompleted, totalQuests));
    }

    private void openQuestLine(DBEntry<IQuestLine> q) {
        selectedLine = q.getValue();
        selectedLineId = q.getID();
        for (int i = 0; i < btnListRef.size(); i++) {
            btnListRef.get(i).setActive((visChapters.get(i).getSecond() & 4) == 0 && q.getID() != selectedLineId);
        }

        cvQuest.setQuestLine(q.getValue());
        icoChapter.setTexture(new OreDictTexture(1F, q.getValue().getProperty(NativeProps.ICON), false, true), null);
        txTitle.setText(QuestTranslation.translate(q.getValue().getUnlocalisedName()));
        refreshQuestCompletion();

        if (!trayLock) {
            cvFrame.setTrayState(true, 200);
            cvChapterTray.setTrayState(false, 200);
            cvQuest.fitToWindow();
        }
        refreshClaimAll();
        refreshDesigner();
    }

    private void refreshContent() {
        if (selectedLineId >= 0) {
            selectedLine = QuestLineDatabase.INSTANCE.getValue(selectedLineId);
            if (selectedLine == null) selectedLineId = -1;
        } else {
            selectedLine = null;
        }

        float zoom = cvQuest.getZoom();
        int sx = cvQuest.getScrollX();
        int sy = cvQuest.getScrollY();
        cvQuest.setQuestLine(selectedLine);
        cvQuest.setZoom(zoom);
        cvQuest.setScrollX(sx);
        cvQuest.setScrollY(sy);
        cvQuest.refreshScrollBounds();
        cvQuest.updatePanelScroll();

        if (selectedLine != null) {

            refreshQuestCompletion();
            txTitle.setText(QuestTranslation.translate(selectedLine.getUnlocalisedName()));
            icoChapter.setTexture(new OreDictTexture(1F, selectedLine.getProperty(NativeProps.ICON), false, true), null);
        } else {
            txTitle.setText("");
            icoChapter.setTexture(null, null);
        }

        refreshClaimAll();
        refreshDesigner();
    }

    private void refreshClaimAll() {
        if (cvQuest.getQuestLine() == null || cvQuest.getQuestButtons().size() <= 0) {
            claimAll.setActive(false);
            claimAll.setIcon(PresetIcon.ICON_CHEST_ALL.getTexture(), new GuiColorStatic(0xFF444444), 0);
            return;
        }

        for (PanelButtonQuest btn : cvQuest.getQuestButtons()) {
            if (btn.getStoredValue().getValue().canClaim(mc.player)) {
                claimAll.setActive(true);
                claimAll.setIcon(PresetIcon.ICON_CHEST_ALL.getTexture(), new GuiColorPulse(0xFFFFFFFF, 0xFF444444, 2F, 0F), 0);
                return;
            }
        }

        claimAll.setIcon(PresetIcon.ICON_CHEST_ALL.getTexture(), new GuiColorStatic(0xFF444444), 0);
        claimAll.setActive(false);
    }

    private void refreshDesigner() {
        if (btnDesign != null) {
            btnDesign.setActive(selectedLine != null);
        }
    }

    private void openSearch(PanelButton panelButton) {
        GuiQuestSearch guiQuestSearch = new GuiQuestSearch(this);
        guiQuestSearch.setCallback(entry -> {
            openQuestLine(entry.getQuestLineEntry());
            int selectedQuestId = entry.getQuest().getID();
            Optional<PanelButtonQuest> targetQuestButton = cvQuest.getQuestButtons().stream().filter(panelButtonQuest -> panelButtonQuest.getStoredValue().getID() == selectedQuestId).findFirst();
            targetQuestButton.ifPresent(panelButtonQuest -> {
                GuiTextureColored newTexture = new GuiTextureColored(panelButtonQuest.txFrame,
                        new GuiColorPulse(
                                new GuiColorStatic(255, 220, 115, 255),
                                new GuiColorStatic(255, 191, 0, 255),
                                1, 0
                        ));
                panelButtonQuest.setTextures(newTexture, newTexture, newTexture);
            });
        });
        Minecraft.getInstance().setScreen(guiQuestSearch);
    }

    public static class ScrollPosition{
        public ScrollPosition(int chapterScrollY) {
            this.chapterScrollY = chapterScrollY;
        }

        private int chapterScrollY;

        public int getChapterScrollY() {
            return chapterScrollY;
        }

        public void setChapterScrollY(int chapterScrollY) {
            this.chapterScrollY = chapterScrollY;
        }
    }
}
