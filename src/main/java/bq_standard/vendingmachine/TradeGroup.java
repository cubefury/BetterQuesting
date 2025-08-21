package bq_standard.vendingmachine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import betterquesting.api.questing.vendingmachine.ITradeGroup;
import net.minecraftforge.common.util.Constants;

public class TradeGroup implements ITradeGroup {

    public final List<Trade> trades = new ArrayList<>();
    public boolean hasCooldown = false;
    public int cooldown = 1;
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("hasCooldown", hasCooldown);
        nbt.setInteger("cooldown", cooldown);

        NBTTagList tradeArray = new NBTTagList();
        for (Trade trade : this.trades) {
            tradeArray.appendTag(trade.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("trades", tradeArray);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        hasCooldown = nbt.getBoolean("hasCooldown");
        cooldown = nbt.getInteger("cooldown");

        trades.clear();
        NBTTagList tList = nbt.getTagList("trades", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tList.tagCount(); i++) {
            Trade ti = new Trade();
            ti.readFromNBT(tList.getCompoundTagAt(i));
            trades.add(ti);
        }
    }
}
