package bq_standard.vendingmachine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map.Entry;

import betterquesting.api2.utils.DirtyTradeMarker;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;

public class TradeGroup {

    public final List<Trade> trades = new ArrayList<>();
    public boolean hasCooldown = false;
    public int cooldown = 1;

    // Currently the UUID is just associated with the last claimed time, but
    // we provide the flexibility to add other metadata if so desired, eg.
    // number of times a trade has been executed if we want to limit it.
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

    /* Used to get data associated with player uuid, probably for last claimed check. */
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
            DirtyTradeMarker.markDirty();
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
            if (entry == null) {
                return true;
            }
            return !hasCooldown ||
                    (currentTime - entry.getLong("timestamp")) / 1000 >= this.cooldown;
        }
    }

    // trade: actual trade being selected out of this trade group
    public void executeTrade(EntityPlayer player, Trade trade) {
        UUID user = new ParticipantInfo(player).UUID;
        synchronized (lastClaimed) {
            trade.executeTrade(player);
            NBTTagCompound entry = getTradeInfo(user);
            if (entry == null) {
                entry = new NBTTagCompound();
            }
            entry.setLong("timestamp", System.currentTimeMillis());
            this.lastClaimed.put(user, entry);
            DirtyTradeMarker.markDirty();
        }
    }

    // fullReset: reset this tradegroup cooldown for all users
    public void resetUser(UUID uuid, boolean fullReset) {
        synchronized (lastClaimed) {
            if (fullReset) {
                lastClaimed.clear();
            } else {
                lastClaimed.remove(uuid);
            }
            DirtyTradeMarker.markDirty();
        }
    }

    public NBTTagCompound writeTradeStateToNBT(NBTTagCompound nbt) {
        NBTTagList pList = new NBTTagList();
        synchronized (lastClaimed) {
            for (Entry<UUID, NBTTagCompound> entry : lastClaimed.entrySet()) {
                NBTTagCompound playerEntry= new NBTTagCompound();
                playerEntry.setString("uuid", entry.getKey().toString());
                playerEntry.setLong("last_claimed", entry.getValue().getLong("timestamp"));
                pList.appendTag(playerEntry);
            }
        }
        nbt.setTag("players", pList);
        return nbt;
    }

    public void readTradeStateFromNBT(NBTTagCompound nbt) {
        NBTTagList pList = nbt.getTagList("players", Constants.NBT.TAG_COMPOUND);
        synchronized (lastClaimed) {
            lastClaimed.clear();
            for (int i = 0; i < pList.tagCount(); i++) {
                NBTTagCompound entry = (NBTTagCompound) pList.getCompoundTagAt(i).copy();

                try {
                    NBTTagCompound pData = new NBTTagCompound();
                    pData.setLong("timestamp", entry.getLong("last_claimed"));
                    lastClaimed.put(UUID.fromString(entry.getString("uuid")),
                            pData);
                } catch (Exception e) {
                    BetterQuesting.logger.log(Level.ERROR,"Unable to load player UUID for trade", e);
                }
            }
        }
    }
}
