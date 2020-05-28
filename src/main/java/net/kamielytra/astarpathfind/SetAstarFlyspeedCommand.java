package net.kamielytra.astarpathfind;

import net.kamielytra.KamiElytra;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

public class SetAstarFlyspeedCommand extends CommandBase implements IClientCommand {


    @Override
    public String getName() {
        return "setastarflyspeed";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "setastarflyspeed <speed>(default 1.8)";
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
                double speed = Double.valueOf(args[0]);
                KamiElytra.mc.player.sendMessage(new TextComponentString(TextFormatting.BLUE+"Astar Flyspeed set to "+speed+ "default is 1.8"));
                KamiElytra.mc.player.sendMessage(new TextComponentString(TextFormatting.BLUE+"Astar Flyspeed was set to "+Astarpathfinder.flyspeed));
                Astarpathfinder.flyspeed=speed;
                KamiElytra.writeConfig();
            } catch (Exception e)
            {
                KamiElytra.mc.player.sendMessage(new TextComponentString(TextFormatting.RED+"ERROR"));
            }
        }
    }


    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return true;
    }
}
