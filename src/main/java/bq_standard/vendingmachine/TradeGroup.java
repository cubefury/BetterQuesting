package bq_standard.vendingmachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import betterquesting.api2.utils.DirtyPlayerMarker;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class TradeGroup {

    public final List<Trade> trades = new ArrayList<>();
    public boolean hasCooldown = false;
    public int cooldown = 1;

    private final HashMap<UUID, NBTTagCompound> lastClaimed = new HashMap<>();

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

    public NBTTagCompound getTradeInfo(UUID uuid) {
        synchronized (lastClaimed) {
            return lastClaimed.get(uuid);
        }
    }

    public void setTradeInfo(UUID uuid, NBTTagCompound nbt) {
        if (uuid == null) return;

        synchronized(lastClaimed) {
            if (nbt == null) {
                lastClaimed.remove(uuid);
            } else {
                lastClaimed.put(uuid, nbt);
            }
            DirtyPlayerMarker.markDirty(uuid);
        }
    }

    public boolean canExecuteTrade(EntityPlayer player) {
        // I was thinking of putting the hasCooldown check here
        // and returning immediately, but if there is future
        // trade metadata to add, this early return can cause a bug.
        UUID user = new ParticipantInfo(player).UUID;
        long currentTime = System.currentTimeMillis();
        synchronized (lastClaimed) {
            NBTTagCompound entry = getTradeInfo(user);
            return !hasCooldown ||
                    (currentTime - entry.getLong("timestamp")) / 1000 >= this.cooldown;
        }

    }

    public void executeTrade(EntityPlayer player) {
        UUID user = new ParticipantInfo(player).UUID;
        synchronized (lastClaimed) {
            NBTTagCompound entry = getTradeInfo(user);
            if (entry == null) {
                entry = new NBTTagCompound();
            }
            entry.setLong("timestamp", System.currentTimeMillis());
            this.lastClaimed.put(user, entry);
            DirtyPlayerMarker.markDirty(user);
        }
    }
}
