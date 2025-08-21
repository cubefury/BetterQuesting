package betterquesting.api.questing.vendingmachine;

import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface ITrade extends INBTSaveLoad<NBTTagCompound> {
    void executeTrade(EntityPlayer player);
}
