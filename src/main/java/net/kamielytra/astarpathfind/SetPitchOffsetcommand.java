package net.kamielytra.astarpathfind;

import net.kamielytra.KamiElytra;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

public class SetPitchOffsetcommand extends CommandBase implements IClientCommand {


    @Override
    public String getName() {
        return "setpitchoffset";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "setpitchoffset <offset in degrees>";
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
                float pitchoffset = Float.valueOf(args[0]);
                KamiElytra.mc.player.sendMessage(new TextComponentString(TextFormatting.BLUE+"pitchoffset now set to "+pitchoffset+ "default is 8.0"));
                KamiElytra.mc.player.sendMessage(new TextComponentString(TextFormatting.BLUE+"pitchoffset was set to "+Astarpathfinder.PitchOffset));
                Astarpathfinder.PitchOffset= pitchoffset;
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
