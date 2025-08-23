package betterquesting.client.gui2.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.GuiContainerCanvas;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.blocks.TileVendingMachine;
import betterquesting.questing.QuestDatabase;

public class GuiVendingMachine extends GuiContainerCanvas implements INeedsRefresh {

    private final ContainerVendingMachine vmContainer;
    private final TileVendingMachine tile;

    private final List<Map.Entry<UUID, IQuest>> quests = new ArrayList<>();

    private IGuiCanvas cvBackground;

    public GuiVendingMachine(GuiScreen parent, InventoryPlayer playerInvo, TileVendingMachine vendingMachine) {
        super(parent, new ContainerVendingMachine(playerInvo, vendingMachine));
        this.vmContainer = (ContainerVendingMachine) this.inventorySlots;
        this.tile = vendingMachine;
    }

    @Override
    public void refreshGui() {
        refreshAvailableTrades();
        refreshTradesPanel();
    }

    @Override
    public void initPanel() {
        super.initPanel();

        Keyboard.enableRepeatEvents(true);
        refreshAvailableTrades();

        // Background panel
        cvBackground = new CanvasTextured(
            new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0),
            PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        refreshTradesPanel();
    }

    public void refreshAvailableTrades() {
        quests.clear();
        QuestCache qc = (QuestCache) mc.thePlayer.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        if (qc != null) {
            quests.addAll(
                QuestDatabase.INSTANCE.filterKeys(qc.getCompletedTradeQuests())
                    .entrySet());
        }
    }

    public void refreshTradesPanel() {
        // implement trades panel gui refresh here
    }
}
