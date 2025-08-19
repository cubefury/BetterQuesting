package betterquesting.client.gui2.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

import betterquesting.blocks.TileVendingMachine;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerVendingMachine extends Container {
    private TileVendingMachine tile;

    public ContainerVendingMachine(InventoryPlayer inventory, TileVendingMachine tile) {
        this.tile = tile;
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        if (idx < 0) {
            return null;
        }
        Slot slot = (Slot) this.inventorySlots.get(idx);
        // TODO: Implement dispensing logic here since player shiftclicks to claim
        return null;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }
}
