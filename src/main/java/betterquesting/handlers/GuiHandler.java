package betterquesting.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import betterquesting.blocks.TileSubmitStation;
import betterquesting.blocks.TileVendingMachine;
import betterquesting.client.gui2.GuiQuestHelp;
import betterquesting.client.gui2.inventory.ContainerSubmitStation;
import betterquesting.client.gui2.inventory.ContainerVendingMachine;
import betterquesting.client.gui2.inventory.GuiSubmitStation;
import betterquesting.client.gui2.inventory.GuiVendingMachine;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (ID == 0 && tile instanceof TileSubmitStation) {
            return new ContainerSubmitStation(player.inventory, (TileSubmitStation) tile);
        } else if (ID == 0 && tile instanceof TileVendingMachine) {
            return new ContainerVendingMachine(player.inventory, (TileVendingMachine) tile);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (ID == 0 && tile instanceof TileSubmitStation) {
            return new GuiSubmitStation(null, player.inventory, (TileSubmitStation) tile);
        } else if (ID == 0 && tile instanceof TileVendingMachine) {
            return new GuiVendingMachine(null, player.inventory, (TileVendingMachine) tile);
        } else if (ID == 1) {
            return new GuiQuestHelp(null);
        }

        return null;
    }

}
