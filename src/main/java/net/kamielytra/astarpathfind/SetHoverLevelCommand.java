package net.kamielytra.astarpathfind;

import net.kamielytra.KamiElytra;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

public class SetHoverLevelCommand  extends CommandBase implements IClientCommand {


    @Override
    public String getName() {
        return "sethoverlevel";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "sethoverlevel <levelDiff>";
    }


    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if( args.length>=1)
        {
            try {
                double level = Double.valueOf(args[0]);
                KamiElytra.mc.player.sendMessage(new TextComponentString(TextFormatting.BLUE+"hoverlevel Set to +"+level+ " default is 1.2"));
                Astarpathfinder.HOVERLEVEL=level;
            } catch (Exception e)
            {

            }
        }
    }


    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return true;
    }
}
