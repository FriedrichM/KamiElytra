package net.kamielytra.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import net.kamielytra.astarpathfind.Astarpathfinder;
import net.kamielytra.mixin.PacketEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Created by 086 on 13/11/2017.
 * NoPacketKick fixed by 0x2E | PretendingToCode 4/10/2020
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Shadow public INetHandler packetListener;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Send(packet);
        if(event.getPacket() instanceof CPacketPlayer)
            Astarpathfinder.onsend(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {

        PacketEvent event = new PacketEvent.Receive(packet);
        if(packet instanceof SPacketPlayerPosLook) {
            Astarpathfinder.onreceive(event);
        }
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {

        return; // DON'T REMOVE THE FUCKING RETURN
    }

}
