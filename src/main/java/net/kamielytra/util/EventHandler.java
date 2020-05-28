package net.kamielytra.util;

import net.kamielytra.KamiElytra;
import net.kamielytra.astarpathfind.Astarpathfinder;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@EventBusSubscriber
public class EventHandler {


    private static boolean astar_initialPress=true;


    @SubscribeEvent
    public static void onRespawn(PlayerRespawnEvent e) {

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if(KamiElytra.keyAstar.isPressed()&&astar_initialPress){
            Astarpathfinder.astar_enabled =!Astarpathfinder.astar_enabled;
            if(Astarpathfinder.astar_enabled){
                Astarpathfinder.init();
            }else {
                Astarpathfinder.turnOff();
            }
        } else if (!KamiElytra.keyAstar.isPressed() && !astar_initialPress) {
            astar_initialPress = true;
        }

        if(KamiElytra.mc.player!=null) {
            Astarpathfinder.update();
        }

    }


}
