package net.kamielytra;

import net.kamielytra.astarpathfind.*;
import net.kamielytra.util.KeyReflectionHelper;
import net.kamielytra.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, clientSideOnly = true)
public class KamiElytra {

	public static KeyBinding keyAstar = new KeyBinding("key.kamielytra.astar.desc", Keyboard.KEY_NONE, "key.kamielytra.category");
	public static Minecraft mc = Minecraft.getMinecraft();
	@Instance
	public static KamiElytra instance;
	private static Configuration config;

	@EventHandler
	public static void PreInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile(), "0.1", false);
		syncConfig();
	}

	@EventHandler
	public static void init(FMLInitializationEvent event) {
		KeyReflectionHelper.gatherFields();
		ClientRegistry.registerKeyBinding(keyAstar);
		ClientCommandHandler.instance.registerCommand(new SetHoverLevelCommand());
		ClientCommandHandler.instance.registerCommand(new SetAstarFlyspeedCommand());
		ClientCommandHandler.instance.registerCommand(new SetPitchOffsetcommand());


	}


	public static void syncConfig() { // Gets called from preInit
		try {
			// Load config
			config.load();

			Property flyspeedprop = config.get(Configuration.CATEGORY_GENERAL, // What category will it be saved to, can be any string
					"AstarFlyspeed", // Property name
					"1.8", // Default value
					"astar Flyspeed"); // Comment
			Property pitchOffsetprop = config.get(Configuration.CATEGORY_GENERAL, // What category will it be saved to, can be any string
					"pitchOffset", // Property name
					"8.0", // Default value
					"pitchOffset"); // Comment
			Astarpathfinder.flyspeed= flyspeedprop.getDouble();
			Astarpathfinder.PitchOffset= (float) pitchOffsetprop.getDouble();
		} catch (Exception e) {
			// Failed reading/writing, just continue
		} finally {
			// Save props to config IF config changed
			if (config.hasChanged()) config.save();
		}
	}
	public static void writeConfig(){
		try {
			// Load config
			config.load();
			Property flyspeedprop = config.get(Configuration.CATEGORY_GENERAL, // What category will it be saved to, can be any string
					"AstarFlyspeed", // Property name
					"1.8", // Default value
					"astar Flyspeed"); // Comment
			Property pitchOffsetprop = config.get(Configuration.CATEGORY_GENERAL, // What category will it be saved to, can be any string
					"pitchOffset", // Property name
					"8.0", // Default value
					"pitchOffset"); // Comment
			flyspeedprop.set(Astarpathfinder.flyspeed);
			pitchOffsetprop.set(Astarpathfinder.PitchOffset);
		} catch (Exception e) {
			// Failed reading/writing, just continue
		} finally {
			// Save props to config IF config changed
			if (config.hasChanged()) config.save();
		}
	}
}
