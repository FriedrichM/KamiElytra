package net.kamielytra.astarpathfind;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class MyAstarGrid {
    public HashMap<BlockPos,MyAstarNode> nodes;

    public MyAstarGrid() {
        nodes= new HashMap<>();
    }
}
