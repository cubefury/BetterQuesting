package betterquesting.blocks;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import betterquesting.core.BetterQuesting;

public class TileVendingMachine extends TileEntity implements IInventory {

    public UUID owner = null;

    public TileVendingMachine() {
        super();
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int idx) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {}

    @Override
    public String getInventoryName() {
        return BetterQuesting.vendingMachine.getLocalizedName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return (owner == null || player.getUniqueID()
            .equals(owner)) && player.getDistanceSq(this.xCoord, this.yCoord, this.zCoord) < 256;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    public boolean isSetup() {
        return owner != null;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote || !isSetup()) {
            return;
        }
    }

}
