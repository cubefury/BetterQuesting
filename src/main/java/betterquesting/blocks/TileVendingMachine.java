package betterquesting.blocks;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.collect.Maps;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.UuidConverter;
import betterquesting.api2.cache.QuestCache;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;

public class TileVendingMachine extends TileEntity implements IInventory {

    public UUID owner = null;
    public TileVendingMachine() { super(); }

    @Override
    public int getSizeInventory() { return 0; }

    @Override
    public ItemStack getStackInSlot(int idx) { return null; }

    @Override
    public ItemStack decrStackSize(int index, int count) { return null; }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) { return null; }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) { }

    @Override
    public String getInventoryName() {
        return BetterQuesting.vendingMachine.getLocalizedName();
    }

    @Override
    public boolean hasCustomInventoryName() { return false; }

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
