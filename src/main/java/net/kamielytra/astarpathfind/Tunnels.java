package net.kamielytra.astarpathfind;

import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class Tunnels {
    static final List<Vec3i> XTUNNEL = new ArrayList<Vec3i>() {{
        add(new Vec3i(2,0,0));
        add(new Vec3i(1,0,0));
        add(new Vec3i(0,0,0));
        add(new Vec3i(-1,0,0));
        add(new Vec3i(-2,0,0));
        add(new Vec3i(2,1,0));
        add(new Vec3i(1,1,0));
        add(new Vec3i(0,1,0));
        add(new Vec3i(-1,1,0));
        add(new Vec3i(-2,1,0));
    }};
    static final List<Vec3i> ZTUNNEL = new ArrayList<Vec3i>() {{
        add(new Vec3i(0,0,2));
        add(new Vec3i(0,0,1));
        add(new Vec3i(0,0,0));
        add(new Vec3i(0,0,-1));
        add(new Vec3i(0,0,-2));
        add(new Vec3i(0,1,2));
        add(new Vec3i(0,1,1));
        add(new Vec3i(0,1,0));
        add(new Vec3i(0,1,-1));
        add(new Vec3i(0,1,-2));
    }};
    static final List<Vec3i> PPNNTUNNEL = new ArrayList<Vec3i>() {{
        add(new Vec3i(0,0,0));
        add(new Vec3i(1,0,0));
        add(new Vec3i(1,0,1));
        add(new Vec3i(2,0,1));
        add(new Vec3i(2,0,2));
        add(new Vec3i(3,0,2));
        add(new Vec3i(0,0,-1));
        add(new Vec3i(-1,0,-1));
        add(new Vec3i(-1,0,-2));
        add(new Vec3i(-2,0,-2));
        add(new Vec3i(-2,0,-3));

        add(new Vec3i(0,1,0));
        add(new Vec3i(1,1,0));
        add(new Vec3i(1,1,1));
        add(new Vec3i(2,1,1));
        add(new Vec3i(2,1,2));
        add(new Vec3i(3,1,2));
        add(new Vec3i(0,1,-1));
        add(new Vec3i(-1,1,-1));
        add(new Vec3i(-1,1,-2));
        add(new Vec3i(-2,1,-2));
        add(new Vec3i(-2,1,-3));

    }};
    static final List<Vec3i> PNNPTUNNEL = new ArrayList<Vec3i>() {{
        add(new Vec3i(0,0,0));
        add(new Vec3i(1,0,0));
        add(new Vec3i(1,0,-1));
        add(new Vec3i(2,0,-1));
        add(new Vec3i(2,0,-2));
        add(new Vec3i(3,0,-2));
        add(new Vec3i(0,0,1));
        add(new Vec3i(-1,0,1));
        add(new Vec3i(-1,0,2));
        add(new Vec3i(-2,0,2));
        add(new Vec3i(-2,0,3));

        add(new Vec3i(0,1,0));
        add(new Vec3i(1,1,0));
        add(new Vec3i(1,1,-1));
        add(new Vec3i(2,1,-1));
        add(new Vec3i(2,1,-2));
        add(new Vec3i(3,1,-2));
        add(new Vec3i(0,1,1));
        add(new Vec3i(-1,1,1));
        add(new Vec3i(-1,1,2));
        add(new Vec3i(-2,1,2));
        add(new Vec3i(-2,1,3));
    }};

}
