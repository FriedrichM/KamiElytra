package net.kamielytra.astarpathfind;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import net.kamielytra.mixin.PacketEvent;
import net.kamielytra.util.KeyReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
/*
 I went full static here i dont know why but its easier at some points and im to lazy to change it.
 It doesnt matter tho as we never have more than one instance
 Atm i have locked the view as moving the yaw and pitch had effect on the fly movement i dont know why
 */
@EventBusSubscriber
public class Astarpathfinder {

    public static double HOVERLEVEL = 1.2;
    public static int nopathfound;
    public static boolean astar_enabled;
    private static Random rand = new Random();
    private static BlockPos currentplayerBlockpos;
    private static int direction;
    private static Vec3d currentplayerpos;
    private static Vec3d startplayerpos = new Vec3d(0, 0, 0);

    private static boolean diagonal;
    private static double hoverTarget;
    public static float packetYaw = 0.0f;

    private static boolean hoverState = false;
    private static List<MyAstarNode> path = new ArrayList<>();
    private static Minecraft mc = Minecraft.getMinecraft();
    private static long lastspacepress = 0;
    private static long lastStuckCheck = 0;
    private static Vec3d currentStuckCheckPos = new Vec3d(0, 0, 0);
    private static Vec3d lastStuckCheckPos = new Vec3d(0, 0, 0);
    private static boolean baritonePathStarted;
    private static TrackState buildstate = TrackState.GOTO_BUILD;
    private static LandState landstate = LandState.FLY_TO_OBSTACLE;
    private static BlockPos takeoffPos;
    private static long flytowardsstarttime;
    private static boolean flytowardsstarted;
    private static int rubberbandcounter = 0;
    private static double laspacketx;
    private static double laspacketz;
    private static int roof;
    public static float PitchOffset = 50;
    private static double distancetofirst;
    public static double flyspeed = 1.8;

    enum State {
        START_FLYING,
        GOTO_TRACK,
        LAND_SAVE,
        PATHFIND
    }

    enum TrackState {
        GOTO_BUILD,
        MINE_RADIUS,
        GOTO_TAKEOFF,
    }

    enum LandState {
        FLY_TO_OBSTACLE,
        LAND
    }

    private static State state = State.START_FLYING;

    public static void init() {

        if (mc.player != null) {

            mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Astar Pathfinding enabled"));
            if (mc.player != null) {
                currentplayerpos = new Vec3d(Math.floor(mc.player.posX) + 0.5, mc.player.posY, Math.floor(mc.player.posZ) + 0.5);
                startplayerpos = new Vec3d(currentplayerpos.x, currentplayerpos.y, currentplayerpos.z);
                hoverTarget = mc.player.posY + HOVERLEVEL;
                path = null;
                direction = (MathHelper.floor(mc.player.rotationYaw * 8.0f / 360.0f + 0.5) & 0x7);
                diagonal = direction % 2 == 0;
                mc.player.sendMessage(new TextComponentString("direction" + direction));
                state = State.START_FLYING;
                nopathfound = 0;
                lastStuckCheck = 0;
                currentStuckCheckPos = new Vec3d(0, 0, 0);
                baritonePathStarted = false;
                landstate = LandState.FLY_TO_OBSTACLE;
                buildstate = TrackState.GOTO_BUILD;
                flytowardsstarted = false;
                rubberbandcounter = 0;
                laspacketz = -1;
                laspacketx = -1;
                roof = mc.player.dimension == -1?121:255;
            } else {

            }
        }
    }

    public static void turnOff() {

        if (mc.player != null) {
            mc.timer.tickLength = 50;
            if (mc.player != null) {
                mc.player.motionZ = 0;
                mc.player.motionX = 0;
                mc.player.capabilities.isFlying = false;
                mc.player.capabilities.setFlySpeed(0.05f);
                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = false;
            }
            setkeystate(mc.gameSettings.keyBindJump, false);
            mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Astar Pathfinding disabled"));
        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        path = null;
    }

    public static void onsend(PacketEvent event) {
        if (mc.player == null || !astar_enabled) return;
        if (!mc.player.isElytraFlying()) return;
        CPacketPlayer packet = (CPacketPlayer) event.getPacket();

        if (hoverState) {
            packet.pitch = (float) -PitchOffset;
        } else {
            packet.pitch = 0;
        }


        packet.yaw = packetYaw;
        if (event.getPacket() instanceof CPacketEntityAction && ((CPacketEntityAction) event.getPacket()).getAction() == CPacketEntityAction.Action.START_FALL_FLYING) {
            hoverTarget = mc.player.posY + 0.35;
        }
    }

    public static void onreceive(PacketEvent event) {

        if (!astar_enabled || mc.player == null || !mc.player.isElytraFlying()) return;
        SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
        if (mc.player.isElytraFlying()) {

        } else {
            packet.pitch = mc.player.rotationPitch;
        }
        if ((Math.abs(packet.x - laspacketx) < 0.05 && Math.abs(packet.z - laspacketz) < 0.05)) {
            rubberbandcounter++;
        } else {
            rubberbandcounter = 0;
        }
        laspacketx = packet.x;
        laspacketz = packet.z;
        if (rubberbandcounter >= 1) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "rubberband " + rubberbandcounter));
        } else {

        }
    }

    public static void update() {
        if (astar_enabled) {
            if (mc.player == null) return;

            switch (state) {
                case START_FLYING:
                    startFlying();
                    break;
                case PATHFIND:
                    pathfind();
                    break;
                case GOTO_TRACK:
                    gotoTrack();
                    break;
                case LAND_SAVE:
                    landSave();
                    break;
            }
        }
    }


    private static void landSave() {
        switch (landstate) {
            case FLY_TO_OBSTACLE:

                //TODO: make this smarter
                if (!flytowardsstarted) {
                    flytowardsstarted = true;
                    flytowardsstarttime = System.currentTimeMillis();
                    flyTowards(direction <= 4 ? direction * 45 : -180 + direction % 4 * 45);
                } else {
                    if (System.currentTimeMillis() - flytowardsstarttime < 1500) {
                        flyTowards(direction <= 4 ? direction * 45 : -180 + direction % 4 * 45);
                    } else {
                        landstate = LandState.LAND;
                        flytowardsstarted = false;
                    }
                }

                break;
            case LAND:
                Block blockbelow = getBlockBelowPlayer();
                if (blockbelow != null && !blockbelow.equals(Blocks.LAVA) && !blockbelow.equals(Blocks.FLOWING_LAVA) && !blockbelow.equals(Blocks.WEB)) {
                    mc.player.motionZ = 0;
                    mc.player.motionX = 0;
                    if (mc.player.onGround) {
                        buildstate = TrackState.GOTO_BUILD;
                        state = State.GOTO_TRACK;
                    }
                }else{
                    //TODO  find save landing spot and fly there

                }
                break;

        }


    }


    private static void pathfind() {
        setkeystate(mc.gameSettings.keyBindJump, false);
        mc.timer.tickLength = 50;
        currentplayerBlockpos = new BlockPos(Math.round(mc.player.getPositionVector().x), Math.round(mc.player.getPositionVector().y), Math.round(mc.player.getPositionVector().z));
        calcPath(true);
        removeAllBeforeClosest();
        if (checkStuck() || mc.player.onGround || !mc.player.isElytraFlying()) {
            lastStuckCheckPos = new Vec3d(0, 0, 0);
            currentStuckCheckPos = new Vec3d(0, 0, 0);
            System.out.println("BLOCKED");
            state = State.LAND_SAVE;
            nopathfound = 0;
        }
        float yawDeg = 0;

        if (path != null && path.size() > 1) {
            distancetofirst = Math.sqrt(Math.pow(path.get(0).pos.getX() - mc.player.posX, 2) + Math.pow(path.get(0).pos.getZ() - mc.player.posZ, 2));
            yawDeg = (float) calculateLookAt(path.get(Math.min(path.size() - 1, 1)).pos.getX() + 0.5, mc.player.posY, path.get(Math.min(path.size() - 1, 1)).pos.getZ() + 0.5, mc.player)[0];
        }
        packetYaw = yawDeg;
        mc.player.rotationPitch = 0;
        flyTowards(yawDeg);
    }

    private static void flyTowards(float yawDeg) {
        mc.player.rotationYaw = yawDeg;
        float yaw = (float) Math.toRadians(yawDeg);
        double motionAmount = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
        hoverState = hoverState ? mc.player.posY < hoverTarget + 0.1 : mc.player.posY < hoverTarget + 0.0;
        if (hoverState && motionAmount > 1.0) {

            if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
                mc.player.motionY = 2;
            } else {
                double calcMotionDiff = motionAmount * 0.008;
                mc.player.motionY += calcMotionDiff * 3.2;
                mc.player.motionX -= (double) (-MathHelper.sin(yaw)) * calcMotionDiff / 1.0;
                mc.player.motionZ -= (double) MathHelper.cos(yaw) * calcMotionDiff / 1.0;
                mc.player.motionX *= 0.99f;
                mc.player.motionY *= 0.98f;
                mc.player.motionZ *= 0.99f;
            }
        } else {
            double speed = flyspeed;
            mc.player.motionX = (double) (-MathHelper.sin(yaw)) * speed;
            mc.player.motionY = -0.001f;//-0.001f;
            mc.player.motionZ = (double) MathHelper.cos(yaw) * speed;
        }
    }

    private static boolean checkStuck() {
        if (System.currentTimeMillis() - lastStuckCheck > 1500) {
            lastStuckCheck = System.currentTimeMillis();
            lastStuckCheckPos = currentStuckCheckPos;
            currentStuckCheckPos = mc.player.getPositionVector();
            if (lastStuckCheckPos.distanceTo(currentStuckCheckPos) < 1.3) {
                mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "moved too little"));
                return true;
            }
        }
        if (nopathfound > 10) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "no path found"));
            return true;
        }
        if (path != null && !path.isEmpty() && calcPathLength() / Math.sqrt(Math.pow(mc.player.posX - path.get(path.size() - 1).pos.getX(), 2) + Math.pow(mc.player.posZ - path.get(path.size() - 1).pos.getZ(), 2)) > 2) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "long path ratio"));
            return true;
        }
        if (rubberbandcounter > 3)
            return true;
        return false;

    }

    private static void startFlying() {
        if (!mc.player.isElytraFlying()) {
            spamSpace();
            if (!mc.player.onGround && mc.player.motionY < -0.04) {
                Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));

                mc.timer.tickLength = 200;
                return;
            }
            return;
        } else {
            state = State.PATHFIND;
        }
    }

    private static void removeAllBeforeClosest() {
        if (path == null) return;
        double dist = Double.POSITIVE_INFINITY;
        int closest = 0;
        double tempdist;
        for (int i = 0; i < path.size(); i++) {
            if ((tempdist = Math.sqrt(Math.pow(path.get(i).pos.getX() + 0.5 - mc.player.posX, 2) + Math.pow(path.get(i).pos.getZ() + 0.5 - mc.player.posZ, 2))) < dist) {
                dist = tempdist;
                closest = i;
            }
        }

        if (dist < 0.5) {
            for (int i = 0; i <= closest; i++) {
                path.remove(0);
            }
        }
//        else if (new Vec3d(path.get(closest).pos.getX()+0.5,0,path.get(closest).pos.getZ())
//                .distanceTo( new Vec3d(mc.player.posX+mc.player.motionX,0,mc.player.posZ+mc.player.motionZ)) > dist) {
//            //TODO maby remove to
//        }

    }

    private static void calcPath(boolean forcenew) {

        //if (calcPathLength() >20) return;// dont calculate a new one itf the old onei s still long enough.
        List<MyAstarNode> newpath = new ArrayList<>();
        //atm a new path gets forced every tick. one would think that is too much for one tick but it actually isnt that bad.
        //should change this in the future tho.
        boolean continuePath = (!forcenew && path != null && !path.isEmpty());
        BlockPos startpath = continuePath ? path.get(path.size() - 1).pos : currentplayerBlockpos;
        int addlayer = 0;
        switch (direction) {
            case 0:
                //+z
                newpath = MyAstarAlg.findPath(startpath,
                        new BlockPos(startplayerpos.x, currentplayerBlockpos.getY(), currentplayerBlockpos.getZ() + 40),
                        addlayer);
                break;
            case 1:
                //-x+z
                newpath = MyAstarAlg.findPath(startpath.add(0, 0, -1),//+- / -+ diagonals are strange this somehow fixes it
                        new BlockPos(currentplayerBlockpos.getX() - 40, currentplayerBlockpos.getY(), startplayerpos.z + Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() - 40))),
                        addlayer);
                break;
            case 2:
                //-x
                newpath = MyAstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() - 40, currentplayerBlockpos.getY(), startplayerpos.z),
                        addlayer);

                break;
            case 3:
                //-x-z
                newpath = MyAstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() - 40, currentplayerBlockpos.getY(), startplayerpos.z - Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() - 40))),
                        addlayer);
                break;
            case 4:
                //-z
                newpath = MyAstarAlg.findPath(startpath,
                        new BlockPos(startplayerpos.x, currentplayerBlockpos.getY(), currentplayerBlockpos.getZ() - 40),
                        addlayer);
                break;
            case 5:
                //+x-z
                newpath = MyAstarAlg.findPath(startpath.add(-1, 0, 0),//+- / -+ diagonals are strange this somehow fixes it
                        new BlockPos(currentplayerBlockpos.getX() + 40, currentplayerBlockpos.getY(), startplayerpos.z - Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() + 40))),
                        addlayer);
                break;
            case 6:
                //+x
                newpath = MyAstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() + 40, currentplayerBlockpos.getY(), startplayerpos.z),
                        addlayer);
                break;
            case 7:
                //+x+z
                newpath = MyAstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() + 40, currentplayerBlockpos.getY(), startplayerpos.z + Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() + 40))),
                        addlayer);
                break;
        }
        if (continuePath) {
            newpath = reducePath(newpath);
            newpath.remove(0);
            path.addAll(newpath);
        } else {
            path = newpath;
        }
        //path= reducePath(path);


//
//        ArrayList<PathPoint>render = new ArrayList<>();
//        for (int i = 0; i < path.size(); i++) {
//            render.add(new PathPoint((int) path.get(i).pos.getX(), (int) mc.player.posY, (int) path.get(i).pos.getZ()));
//        }
//        renderpaths.add(render);


    }

    private static double calcPathLength() {
        if (path == null || path.size() < 2) return 0;
        double sum = Math.sqrt(Math.pow(mc.player.posX - path.get(0).pos.getX(), 2) + Math.pow(mc.player.posZ - path.get(0).pos.getZ(), 2));
        for (int i = 0; i < path.size() - 1; i++) {
            sum += Math.sqrt(Math.pow(path.get(i).pos.getX() - path.get(i + 1).pos.getX(), 2) + Math.pow(path.get(i).pos.getZ() - path.get(i + 1).pos.getZ(), 2));
        }
        return sum;

    }

    private static List<MyAstarNode> reducePath(List<MyAstarNode> original) {
        if (original.size() <= 3)
            return original;
        List<MyAstarNode> result = new ArrayList<>();
        int lastxdif = Integer.signum(original.get(0).pos.getX() - original.get(1).pos.getX());
        int lastzdif = Integer.signum(original.get(0).pos.getZ() - original.get(1).pos.getZ());
        int curxdif = 0;
        int curzdif = 0;
        result.add(original.get(0));
        for (int i = 1; i < original.size() - 1; i++) {
            curxdif = Integer.signum(original.get(i).pos.getX() - original.get(i + 1).pos.getX());
            curzdif = Integer.signum(original.get(i).pos.getZ() - original.get(i + 1).pos.getZ());
            if (curxdif != lastxdif || curzdif != lastzdif) {
                result.add(original.get(i));
            }
            lastxdif = curxdif;
            lastzdif = curzdif;
        }
        result.add(original.get(original.size() - 1));

        return result;

    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        // to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]{yaw, pitch};
    }

    @SubscribeEvent
    public static void onWorldRender(RenderWorldLastEvent event) {
        if (mc.player == null || path == null || path.isEmpty()) return;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(1.5f);
        GL11.glColor3f(1, 1, 0);
        GlStateManager.disableDepth();
        GL11.glBegin(GL11.GL_LINES);
        Vec3d first = new Vec3d(mc.player.getPosition().x, mc.player.getPosition().y, mc.player.getPosition().z);
        GL11.glVertex3d(first.x - mc.getRenderManager().renderPosX + .5, mc.player.posY - 0.5 - mc.getRenderManager().renderPosY, first.z - mc.getRenderManager().renderPosZ + .5);
        for (int i = 0; i < path.size() - 1; i++) {
            BlockPos pathPoint = path.get(i).pos;
            GL11.glVertex3d(pathPoint.x - mc.getRenderManager().renderPosX + .5, mc.player.posY - 0.5 - mc.getRenderManager().renderPosY, pathPoint.z - mc.getRenderManager().renderPosZ + .5);
            if (i != path.size() - 1) {
                GL11.glVertex3d(pathPoint.x - mc.getRenderManager().renderPosX + .5, mc.player.posY - 0.5 - mc.getRenderManager().renderPosY, pathPoint.z - mc.getRenderManager().renderPosZ + .5);
            }
        }
        GL11.glEnd();
        GlStateManager.enableDepth();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private static void gotoTrack() {
        switch (buildstate) {
            case GOTO_BUILD:
                if (!BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive()) {
                    if (!baritonePathStarted) {
                        takeoffPos = findClosestTunnel();
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(takeoffPos));
                        baritonePathStarted = true;
                    } else {
                        baritonePathStarted = false;
                        buildstate = TrackState.MINE_RADIUS;

                    }
                }
                break;
            case MINE_RADIUS:
                if (!BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isActive()) {
                    Iterable<BlockPos> blocks = BlockPos.getAllInBox(takeoffPos.add(-1, 0, -1), takeoffPos.add(1, 2, 1));
                    switch (direction) {
                        case 0:
                            //+z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(0, 0, 3));
                            break;
                        case 1:
                            //-x+z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(-2, 0, +2));
                            break;
                        case 2:
                            //-x
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(-3, 0, 0));
                            break;
                        case 3:
                            //-x-z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(-2, 0, -2));
                            break;
                        case 4:
                            //-z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(0, 0, -3));
                            break;
                        case 5:
                            //+x-z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(+2, 0, -2));
                            break;
                        case 6:
                            //+x
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(3, 0, 0));
                            break;
                        case 7:
                            //+x+z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(2, 0, 2));
                            break;
                        default:
                            break;

                    }
                    for (BlockPos p : blocks) {
                        if (!(Blocks.BEDROCK.equals(mc.world.getBlockState(p).getBlock()) || Blocks.AIR.equals(mc.world.getBlockState(p).getBlock()))) {
                            BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().clearArea(p, p);
                            return;
                        }
                    }
                    buildstate = TrackState.GOTO_TAKEOFF;
                }
                break;
            case GOTO_TAKEOFF:
                if (!BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive()) {
                    if (!baritonePathStarted) {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(takeoffPos));
                        baritonePathStarted = true;
                    } else {
                        //TODO Center in block
                        mc.player.rotationPitch = 0;
                        mc.player.rotationYaw = direction <= 4 ? direction * 45 : -180 + direction % 4 * 45;
                        baritonePathStarted = false;
                        buildstate = TrackState.GOTO_BUILD;
                        hoverTarget = takeoffPos.y + HOVERLEVEL;
                        state = State.START_FLYING;

                    }
                }
                break;
        }

    }

    private static void spamSpace() {
        if (System.currentTimeMillis() - lastspacepress > 100) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                setkeystate(mc.gameSettings.keyBindJump, false);
            } else {
                setkeystate(mc.gameSettings.keyBindJump, true);
            }
            lastspacepress = System.currentTimeMillis();
        }
    }

    public static void setkeystate(KeyBinding keyBinding, boolean b) {
        if (b) {
            KeyReflectionHelper.pressKey(keyBinding);
        } else {
            KeyReflectionHelper.unpressKey(keyBinding);
        }
    }

    private static Block getBlockBelowPlayer() {
        int i = 0;
        Block block;
        while (i < 70) {
            block = mc.world.getBlockState(mc.player.getPosition().add(0, -(int) Math.min(i, mc.player.posY), 0)).getBlock();
            if (!block.isPassable(mc.world, mc.player.getPosition().add(0, -(int) Math.min(i, mc.player.posY), 0)) || block.equals(Blocks.WEB)) {
                return block;
            }
            i++;
        }
        return Blocks.LAVA;
    }

    private static BlockPos findClosestTunnel() {
        final BlockPos target;
        List<Vec3i> tunnel = null;
        switch (direction) {
            case 0:
                //+z
                target = new BlockPos(startplayerpos.x, startplayerpos.y,
                        mc.player.getPosition().getZ() + 10);
                tunnel = Tunnels.ZTUNNEL;
                break;
            case 1:
                //-x+z
                target = new BlockPos(mc.player.getPosition().getX() - 10, startplayerpos.y,
                        startplayerpos.z + Math.abs(startplayerpos.x - (mc.player.getPosition().getX() - 10)));
                tunnel = Tunnels.PNNPTUNNEL;
                break;
            case 2:
                //-x
                target = new BlockPos(mc.player.getPosition().getX() - 10, startplayerpos.y,
                        startplayerpos.z);
                tunnel = Tunnels.XTUNNEL;
                break;
            case 3:
                //-x-z
                target = new BlockPos(mc.player.getPosition().getX() - 10, startplayerpos.y,
                        startplayerpos.z - Math.abs(startplayerpos.x - (mc.player.getPosition().getX() - 10)));
                tunnel = Tunnels.PPNNTUNNEL;
                break;
            case 4:
                //-z
                target = new BlockPos(startplayerpos.x, startplayerpos.y,
                        mc.player.getPosition().getZ() - 10);
                tunnel = Tunnels.ZTUNNEL;
                break;
            case 5:
                //+x-z
                target = new BlockPos(mc.player.getPosition().getX() + 10, startplayerpos.y,
                        startplayerpos.z - Math.abs(startplayerpos.x - (mc.player.getPosition().getX() + 10)));
                tunnel = Tunnels.PNNPTUNNEL;
                break;
            case 6:
                //+x
                target = new BlockPos(mc.player.getPosition().getX() + 10, startplayerpos.y,
                        startplayerpos.z);
                tunnel = Tunnels.XTUNNEL;
                break;
            case 7:
                //+x+z
                target = new BlockPos(mc.player.getPosition().getX() + 10, startplayerpos.y, startplayerpos.z + Math.abs(startplayerpos.x - (mc.player.getPosition().getX() + 10)));
                tunnel = Tunnels.PPNNTUNNEL;
                break;
            default:
                target = null;
                break;
        }
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(new BlockPos(target.x - 20, Math.max(3, target.y - 10), target.z - 20), new BlockPos(target.x + 20, Math.min(roof, target.y + 10), target.z + 20));
        List<BlockPos> blocklist = new ArrayList<BlockPos>();
        blocks.iterator().forEachRemaining(blocklist::add);
        Collections.sort(blocklist, new Comparator<BlockPos>() {
            @Override
            public int compare(BlockPos o1, BlockPos o2) {
                //  we dont want to change hight if not needed.
                double dist1 = Math.pow(target.x - o1.x, 2) + Math.pow((target.y - o1.y) * 3, 2) + Math.pow(target.z - o1.z, 2);
                double dist2 = Math.pow(target.x - o2.x, 2) + Math.pow((target.y - o2.y) * 3, 2) + Math.pow(target.z - o2.z, 2);
                return Double.compare(dist1, dist2);
            }
        });
        for (BlockPos b : blocklist) {
            if (checkTunnelAt(b, tunnel)) {
                mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Tunnel Found"));
                return b;
            }

        }
        return target;

    }

    private static boolean checkTunnelAt(BlockPos center, List<Vec3i> tunnel) {
        boolean blocked = true;
        if (Blocks.BEDROCK.equals(mc.world.getBlockState(center.add(0, 2, 0)).getBlock())
                || Blocks.LAVA.equals(mc.world.getBlockState(center.add(0, 3, 0)).getBlock())
                || Blocks.FLOWING_LAVA.equals(mc.world.getBlockState(center.add(0, 3, 0)).getBlock())
                || Blocks.LAVA.equals(mc.world.getBlockState(center.add(0, -1, 0)).getBlock())
                || Blocks.FLOWING_LAVA.equals(mc.world.getBlockState(center.add(0, -1, 0)).getBlock())
                || MyAstarNode.noclipblocks.contains(mc.world.getBlockState(center.add(0, -1, 0)).getBlock())
        ) {
            return false;
        }
        for (Vec3i v : tunnel) {
            if (!MyAstarNode.noclipblocks.contains(mc.world.getBlockState(center.add(v)).getBlock())) {
                blocked = false;
                break;
            }
        }
        return blocked;
    }
}
