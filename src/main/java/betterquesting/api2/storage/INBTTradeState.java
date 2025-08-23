package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

// Used when trade state specific data is being handled
public interface INBTTradeState<T extends NBTBase> {

    T writeTradeStateToNBT(T nbt);

    void readTradeStateFromNBT(T nbt);
}
