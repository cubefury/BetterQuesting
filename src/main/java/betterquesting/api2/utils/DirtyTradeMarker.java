package betterquesting.api2.utils;

import net.minecraftforge.common.MinecraftForge;

import betterquesting.api.events.MarkDirtyTradeEvent;

public class DirtyTradeMarker {

    public static void markDirty() {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyTradeEvent());
    }

}
