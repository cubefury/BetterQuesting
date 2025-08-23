package betterquesting.client.gui2;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.util.vector.Vector4f;

import com.google.common.collect.Maps;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.misc.TextureSizeHelper;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.textures.SimpleNoUVTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.GuiQuestLines.ScrollPosition;
import betterquesting.network.handlers.NetQuestAction;
import betterquesting.questing.QuestDatabase;
import bq_standard.vendingmachine.Trade;
import bq_standard.vendingmachine.TradeGroup;

public class GuiQuest extends GuiScreenCanvas implements IPEventListener, INeedsRefresh {

    /*
     * Map which contains scrolls positions. <questId, Triple<taskScroll, rewardScroll, descScroll>>
     */
    private static final Map<UUID, ScrollPosition> scrollsPositions = new HashMap<>();
    private static final Pattern img = Pattern.compile("\\[img height=([1-9]\\d*)] *(.*?:.*?) *\\[/img]");
    private ScrollPosition scrollPosition;

    public static class ScrollPosition {

        public ScrollPosition(int taskScrollY, int rewardScrollY, int descScrollY) {
            this.taskScrollY = taskScrollY;
            this.rewardScrollY = rewardScrollY;
            this.descScrollY = descScrollY;
        }

        @Deprecated
        public ScrollPosition(int taskScrollY, int rewardScrollY) {
            this(taskScrollY, rewardScrollY, 0);
        }

        private int taskScrollY;

        public int getTaskScrollY() {
            return taskScrollY;
        }

        public void setTaskScrollY(int taskScrollY) {
            this.taskScrollY = taskScrollY;
        }

        private int rewardScrollY;

        public int getRewardScrollY() {
            return rewardScrollY;
        }

        public void setRewardScrollY(int rewardScrollY) {
            this.rewardScrollY = rewardScrollY;
        }

        private int descScrollY;

        public int getDescScrollY() {
            return descScrollY;
        }

        public void setDescScrollY(int descScrollY) {
            this.descScrollY = descScrollY;
        }
    }

    private final UUID questID;

    private IQuest quest;

    private PanelButton btnDetect;
    private PanelButton btnClaim;

    private CanvasEmpty cvInner;

    private IGuiRect rectReward;
    private IGuiRect rectTask;

    private CanvasEmpty pnReward;
    private CanvasScrolling csReward;

    private CanvasEmpty pnTask;
    private CanvasScrolling csTask;

    private CanvasScrolling csDesc;

    public GuiQuest(GuiScreen parent, UUID questID) {
        super(parent);
        this.questID = questID;
        scrollPosition = scrollsPositions.get(questID);
        if (scrollPosition == null) {
            scrollPosition = new ScrollPosition(0, 0, 0);
            scrollsPositions.put(questID, scrollPosition);
        }
    }

    @Override
    public void initPanel() {
        super.initPanel();

        this.quest = QuestDatabase.INSTANCE.get(questID);

        if (quest == null) {
            mc.displayGuiScreen(this.parent);
            return;
        }

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(
            new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0),
            PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelTextBox panTxt = new PanelTextBox(
            new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0),
            QuestTranslation.translateQuestName(questID, quest)).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);

        if (QuestingAPI.getAPI(ApiReference.SETTINGS)
            .canUserEdit(mc.thePlayer)) {
            cvBackground.addPanel(
                new PanelButton(
                    new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0),
                    0,
                    QuestTranslation.translate("gui.back")));
            cvBackground.addPanel(
                new PanelButton(
                    new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0),
                    1,
                    QuestTranslation.translate("betterquesting.btn.edit")));
        } else {
            cvBackground.addPanel(
                new PanelButton(
                    new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0),
                    0,
                    QuestTranslation.translate("gui.back")));
        }

        cvInner = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 16, 24), 0));
        cvBackground.addPanel(cvInner);

        if (quest.getRewards()
            .size() > 0
            || !quest.getTradeGroups()
                .isEmpty()) {
            refreshDescPanel(true);

            if (quest.getRewards()
                .size() > 0) { // only show claim button if there are rewards to claim
                btnClaim = new PanelButton(
                    new GuiTransform(new Vector4f(0F, 1F, 0.5F, 1F), new GuiPadding(0, -16, 8, 0), 0),
                    6,
                    QuestTranslation.translate("betterquesting.btn.claim"));
                btnClaim.setActive(false);
                cvInner.addPanel(btnClaim);

                rectReward = new GuiTransform(new Vector4f(0F, 0.5F, 0.5F, 1F), new GuiPadding(0, 0, 8, 16), 0);
                rectReward.setParent(cvInner.getTransform());
            }

            refreshRewardPanel();
        } else {
            refreshDescPanel(false);
        }

        // if(quest.getTasks().size() > 0)
        {
            btnDetect = new PanelButton(
                new GuiTransform(new Vector4f(0.5F, 1F, 1F, 1F), new GuiPadding(8, -16, 0, 0), 0),
                7,
                QuestTranslation.translate("betterquesting.btn.detect_submit"));
            btnDetect.setActive(false);
            cvInner.addPanel(btnDetect);

            rectTask = new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 16, 0, 16), 0);
            rectTask.setParent(cvInner.getTransform());

            refreshTaskPanel();
        }

        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 0, 0, 0, 0);
        ls0.setParent(cvInner.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, 0, 0, 0, 0);
        le0.setParent(cvInner.getTransform());
        PanelLine paLine0 = new PanelLine(
            ls0,
            le0,
            PresetLine.GUI_DIVIDER.getLine(),
            1,
            PresetColor.GUI_DIVIDER.getColor(),
            1);
        cvInner.addPanel(paLine0);
    }

    @Override
    public void refreshGui() {
        this.refreshTaskPanel();
        this.refreshRewardPanel();
        this.updateButtons();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (super.onMouseClick(mx, my, click)) {
            this.updateButtons();
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        try {
            return super.onMouseRelease(mx, my, click);
        } finally {
            if (csReward != null) {
                scrollPosition.setRewardScrollY(csReward.getScrollY());
            }

            if (csTask != null) {
                scrollPosition.setTaskScrollY(csTask.getScrollY());
            }

            if (csDesc != null) {
                scrollPosition.setDescScrollY(csDesc.getScrollY());
            }
        }
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        try {
            if (super.onMouseScroll(mx, my, scroll)) {
                this.updateButtons();
                return true;
            }

            return false;
        } finally {
            if (csReward != null) {
                scrollPosition.setRewardScrollY(csReward.getScrollY());
            }

            if (csTask != null) {
                scrollPosition.setTaskScrollY(csTask.getScrollY());
            }

            if (csDesc != null) {
                scrollPosition.setDescScrollY(csDesc.getScrollY());
            }
        }
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        if (super.onKeyTyped(c, keycode)) {
            this.updateButtons();
            return true;
        }

        return false;
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    private void onButtonPress(PEventButton event) {
        IPanelButton btn = event.getButton();

        if (btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if (btn.getButtonID() == 1) // Edit
        {
            // mc.displayGuiScreen(new GuiQuestEditor(this, quest));
            mc.displayGuiScreen(new betterquesting.client.gui2.editors.GuiQuestEditor(this, questID));
        } else if (btn.getButtonID() == 6) // Reward claim
        {
            NetQuestAction.requestClaim(Collections.singletonList(questID));
        } else if (btn.getButtonID() == 7) // Task detect/submit
        {
            NetQuestAction.requestDetect(Collections.singletonList(questID));
        }
    }

    // Vending machine unlocks also display here
    private void refreshRewardPanel() {
        if (pnReward != null) {
            cvInner.removePanel(pnReward);
        }

        if (rectReward == null) {
            this.initPanel();
            return;
        }

        pnReward = new CanvasEmpty(rectReward);
        cvInner.addPanel(pnReward);
        int yOffset = 0;

        csReward = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0));
        pnReward.addPanel(csReward);

        PanelVScrollBar scList = new PanelVScrollBar(
            new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        pnReward.addPanel(scList);
        csReward.setScrollDriverY(scList);

        for (DBEntry<IReward> entry : quest.getRewards()
            .getEntries()) {
            IReward rew = entry.getValue();

            PanelTextBox titleReward = new PanelTextBox(
                new GuiTransform(new Vector4f(), 0, yOffset, rectReward.getWidth(), 12, 0),
                QuestTranslation.translate(rew.getUnlocalisedName()));
            titleReward.setColor(PresetColor.TEXT_HEADER.getColor())
                .setAlignment(1);
            titleReward.setEnabled(true);
            csReward.addPanel(titleReward);
            yOffset += 12;

            IGuiPanel rewardGui = rew.getRewardGui(
                new GuiTransform(GuiAlign.FULL_BOX, 0, 0, rectReward.getWidth(), rectReward.getHeight(), 111),
                Maps.immutableEntry(questID, quest));
            if (rewardGui != null) {
                rewardGui.initPanel();
                // Wrapping into canvas allow avoid empty space at end
                CanvasEmpty tempCanvas = new CanvasEmpty(
                    new GuiTransform(
                        GuiAlign.TOP_LEFT,
                        0,
                        yOffset,
                        rectReward.getWidth(),
                        rewardGui.getTransform()
                            .getHeight()
                            - rewardGui.getTransform()
                                .getY(),
                        1));
                csReward.addPanel(tempCanvas);
                tempCanvas.addPanel(rewardGui);
                yOffset += tempCanvas.getTransform()
                    .getHeight();
            }
        }

        if (!quest.getTradeGroups()
            .isEmpty()) {
            PanelTextBox titleTradeUnlock = new PanelTextBox(
                new GuiTransform(new Vector4f(), 0, yOffset, rectReward.getWidth(), 12, 0),
                QuestTranslation.translate("bq_standard.trade_unlock"));
            titleTradeUnlock.setColor(PresetColor.TEXT_HEADER.getColor())
                .setAlignment(1);
            titleTradeUnlock.setEnabled(true);
            csReward.addPanel(titleTradeUnlock);
            yOffset += 12;
            for (TradeGroup tg : quest.getTradeGroups()) {
                // If we want to show tradegroups visually to players, here would
                // be the place to add another wrapper per trade group
                for (Trade trade : tg.trades) {
                    IGuiPanel tradeGui = trade.getTradeGui(
                        new GuiTransform(GuiAlign.FULL_BOX, 0, 0, rectReward.getWidth(), rectReward.getHeight(), 111));
                    if (tradeGui != null) {
                        tradeGui.initPanel();
                        CanvasEmpty tempCanvas = new CanvasEmpty(
                            new GuiTransform(
                                GuiAlign.TOP_LEFT,
                                0,
                                yOffset,
                                rectReward.getWidth(),
                                tradeGui.getTransform()
                                    .getHeight()
                                    - tradeGui.getTransform()
                                        .getY(),
                                1));
                        csReward.addPanel(tempCanvas);
                        tempCanvas.addPanel(tradeGui);
                        yOffset += tempCanvas.getTransform()
                            .getHeight();
                    }
                }
            }
        }

        csReward.setScrollY(scrollPosition.getRewardScrollY());
        csReward.updatePanelScroll();

        updateButtons();
    }

    private void refreshTaskPanel() {
        if (pnTask != null) {
            cvInner.removePanel(pnTask);
        }

        pnTask = new CanvasEmpty(rectTask);
        cvInner.addPanel(pnTask);

        csTask = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0));
        pnTask.addPanel(csTask);

        PanelVScrollBar scList = new PanelVScrollBar(
            new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        pnTask.addPanel(scList);
        csTask.setScrollDriverY(scList);

        int yOffset = 0;
        EnumLogic taskLogic = quest.getProperty(NativeProps.LOGIC_TASK);
        List<DBEntry<ITask>> entries = quest.getTasks()
            .getEntries();
        for (int i = 0; i < entries.size(); i++) {
            ITask tsk = entries.get(i)
                .getValue();
            String taskName = (i + 1) + ". " + QuestTranslation.translate(tsk.getUnlocalisedName());
            PanelTextBox titleReward = new PanelTextBox(
                new GuiTransform(new Vector4f(), 0, yOffset, rectTask.getWidth(), 12, 0),
                taskName);
            titleReward.setColor(PresetColor.TEXT_HEADER.getColor())
                .setAlignment(1);
            titleReward.setEnabled(true);
            csTask.addPanel(titleReward);
            yOffset += 10;

            IGuiPanel taskGui = tsk.getTaskGui(
                new GuiTransform(GuiAlign.FULL_BOX, 10, 10, rectTask.getWidth(), rectTask.getHeight(), 0),
                Maps.immutableEntry(questID, quest));
            if (taskGui != null) {
                taskGui.initPanel();
                // Wrapping into canvas allow avoid empty space at end
                CanvasTextured tempCanvas = new CanvasTextured(
                    new GuiTransform(
                        GuiAlign.TOP_LEFT,
                        0,
                        yOffset,
                        rectTask.getWidth() - 15,
                        taskGui.getTransform()
                            .getHeight() + 20
                            - taskGui.getTransform()
                                .getY(),
                        1),
                    PresetTexture.PANEL_MAIN.getTexture());
                csTask.addPanel(tempCanvas);
                tempCanvas.addPanel(taskGui);
                int guiHeight = tempCanvas.getTransform()
                    .getHeight();
                yOffset += guiHeight + 5;
            }

            if (taskLogic == EnumLogic.OR && i < entries.size() - 1) {
                yOffset += 10;
                String logicText = QuestTranslation.translate(
                    "betterquesting.gui.logic." + taskLogic.name()
                        .toLowerCase());
                PanelTextBox panelLogic = new PanelTextBox(
                    new GuiTransform(new Vector4f(), 0, yOffset, rectTask.getWidth(), 12, 0),
                    logicText);
                panelLogic.setColor(PresetColor.TEXT_HIGHLIGHT.getColor())
                    .setAlignment(1);
                csTask.addPanel(panelLogic);
                yOffset += 10;
            }

            // Indent from the previous
            yOffset += 8;
        }
        csTask.setScrollY(scrollPosition.getTaskScrollY());
        csTask.updatePanelScroll();

        updateButtons();
    }

    private void refreshDescPanel(boolean hasRewardOrTradeUnlock) {
        if (hasRewardOrTradeUnlock) {
            csDesc = new CanvasScrolling(
                new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.5F), new GuiPadding(0, 0, 16, 16), 0));
        } else {
            csDesc = new CanvasScrolling(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 0, 16, 0), 0));
        }
        cvInner.addPanel(csDesc);
        addQuestDescPanels();

        PanelVScrollBar paDescScroll;
        if (hasRewardOrTradeUnlock) {
            paDescScroll = new PanelVScrollBar(
                new GuiTransform(
                    GuiAlign.quickAnchor(GuiAlign.TOP_CENTER, GuiAlign.MID_CENTER),
                    new GuiPadding(-16, 0, 8, 16),
                    0));
        } else {
            paDescScroll = new PanelVScrollBar(
                new GuiTransform(
                    GuiAlign.quickAnchor(GuiAlign.TOP_CENTER, GuiAlign.BOTTOM_CENTER),
                    new GuiPadding(-16, 0, 8, 0),
                    0));
        }
        cvInner.addPanel(paDescScroll);
        csDesc.setScrollDriverY(paDescScroll);
        paDescScroll.setEnabled(
            csDesc.getScrollBounds()
                .getHeight() > 0);

        csDesc.setScrollY(scrollPosition.getDescScrollY());
        csDesc.updatePanelScroll();
    }

    private void addQuestDescPanels() {
        String questText = QuestTranslation.translateQuestDescription(questID, quest);
        Matcher matcher = img.matcher(questText);
        int last = 0;
        int y = 0;
        while (matcher.find()) {
            y += addQuestDescTextSegment(y, questText.substring(last, matcher.start())).getTransform()
                .getHeight();
            last = matcher.end();
            int imgHeight = Integer.parseInt(matcher.group(1));
            // 2px margin around images
            y += addQuestDescImageSegment(y + 2, new ResourceLocation(matcher.group(2)), imgHeight).getTransform()
                .getHeight() + 2;
        }
        if (last < questText.length()) {
            String trailing = questText.substring(last);
            if (!StringUtils.isBlank(trailing)) {
                addQuestDescTextSegment(y, trailing);
            }
        }
    }

    private IGuiPanel addQuestDescTextSegment(int y, String questText) {
        PanelTextBox paDesc = new PanelTextBox(
            new GuiRectangle(
                0,
                y,
                csDesc.getTransform()
                    .getWidth(),
                0),
            questText,
            true,
            true);
        paDesc.setColor(PresetColor.TEXT_MAIN.getColor());// .setFontSize(10);
        csDesc.addCulledPanel(paDesc, false);
        return paDesc;
    }

    private IGuiPanel addQuestDescImageSegment(int y, ResourceLocation resourceLocation, int height) {
        IGuiRect dimension = TextureSizeHelper.getDimension(resourceLocation);
        int containerWidth = csDesc.getTransform()
            .getWidth();
        float sx = (float) containerWidth / dimension.getWidth(), sy = (float) height / dimension.getHeight();
        if (sx < sy) {
            height = ceilDiv(dimension.getHeight() * containerWidth, dimension.getWidth());
        }
        PanelGeneric paDesc = new PanelGeneric(
            new GuiRectangle(0, y, containerWidth, height),
            new SimpleNoUVTexture(resourceLocation, dimension).maintainAspect(true));
        csDesc.addCulledPanel(paDesc, false);
        return paDesc;
    }

    private static int ceilDiv(int lhs, int rhs) {
        return -Math.floorDiv(-lhs, rhs);
    }

    private void updateButtons() {
        Minecraft mc = Minecraft.getMinecraft();

        if (btnClaim != null) {
            // Claim button state
            btnClaim.setActive(
                quest.getRewards()
                    .size() > 0 && quest.canClaim(mc.thePlayer));
        }

        if (btnDetect != null) {
            // Detect/submit button state
            btnDetect.setActive(quest.canSubmit(mc.thePlayer));
        }
    }
}
