package net.kamielytra.astarpathfind;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;

public class MyAstarAlg {
    private static PriorityQueue<MyAstarNode> openlist;
    private static HashMap<BlockPos,MyAstarNode> closedlist;
    private static Vec3i targetVec;
    private static  int addlayer;
    private static double lowestf;
    private static MyAstarNode lowestfNode;

    private static MyAstarGrid grid;
    public static List<MyAstarNode> findPath(BlockPos start, BlockPos target, int adiitionallayer)
    {
        grid = new MyAstarGrid();
        MyAstarAlg.addlayer =adiitionallayer;
        lowestf=Double.POSITIVE_INFINITY;

        closedlist= new HashMap<>();
        targetVec= new Vec3i(target.getX(),target.getY(),target.getZ());
        openlist= new PriorityQueue<>(new Comparator<MyAstarNode>() {
            @Override
            public int compare(MyAstarNode o1, MyAstarNode o2) {
//                return Double.compare(Math.pow(o1.pos.getX()-targetVec.getX(),2)+Math.pow(o1.pos.getZ()-targetVec.getZ(),2),
//                        Math.pow(o2.pos.getX()-targetVec.getX(),2)+Math.pow(o2.pos.getZ()-targetVec.getZ(),2));
                return Double.compare(o1.f,o2.f);
            }
        });
        MyAstarNode startnode= new MyAstarNode(start,adiitionallayer);
        startnode.f=Math.pow(startnode.pos.getX()-targetVec.getX(),2)+Math.pow(startnode.pos.getZ()-targetVec.getZ(),2);
        lowestfNode=startnode;
        grid.nodes.put(start,startnode);
        openlist.add(startnode);
        int i=0;
        do {
            i++;
            MyAstarNode currentnode= openlist.remove();
            if(currentnode.pos.equals(target)){
                Astarpathfinder.nopathfound=0;
               return buildPath(currentnode);
            }else{
                closedlist.put(currentnode.pos,currentnode);
                expandNode(currentnode);
            }
        }while(!openlist.isEmpty()&&i<7000);
        if(i>=7000){
            System.out.println("no short path");
        }
        if(Math.sqrt(lowestfNode.pos.distanceSq(target))>10){
            Astarpathfinder.nopathfound++;
        }
        return buildPath(lowestfNode);
//        double lowesf=Double.POSITIVE_INFINITY;
//        for(Map.Entry<BlockPos,MyAstarNode> e:closedlist.entrySet()){
//            if(e.getValue().f<)
//        }
    }

    private static List<MyAstarNode> buildPath(MyAstarNode endnode) {
        ArrayList<MyAstarNode> path= new ArrayList<>();
        path.add(endnode);
        MyAstarNode pathnode= endnode;
        while(pathnode.pred!=null){
            pathnode= pathnode.pred;
            path.add(pathnode);
        }
        Collections.reverse(path);
        return path;
    }

    private static void expandNode(MyAstarNode currentnode) {
        MyAstarNode successor=null;
        boolean xblocked;
        boolean mxblocked;
        boolean yblocked;
        boolean myblocked;
        for(int i=0;i<8;i++){
            switch (i){
                case 0:
                    if((successor=grid.nodes.get(currentnode.pos.add(1,0,0)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(1,0,0), addlayer);
                    }
                    break;
                case 1:
                    if((successor=grid.nodes.get(currentnode.pos.add(0,0,1)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(0,0,1), addlayer);
                    }
                    break;
                case 2:
                    if((successor=grid.nodes.get(currentnode.pos.add(-1,0,0)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(-1,0,0), addlayer);

                    }
                    break;
                case 3:
                    if((successor=grid.nodes.get(currentnode.pos.add(0,0,-1)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(0,0,-1), addlayer);

                    }
                    break;
                case 4:
                    if((successor=grid.nodes.get(currentnode.pos.add(1,0,-1)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(1,0,-1), addlayer);
                    }
                    if(grid.nodes.get(currentnode.pos.add(1,0,0)).blocked||grid.nodes.get(currentnode.pos.add(0,0,-1)).blocked)
                        continue;
                    break;
                case 5:
                    if((successor=grid.nodes.get(currentnode.pos.add(-1,0,-1)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(-1,0,-1), addlayer);
                    }
                    if(grid.nodes.get(currentnode.pos.add(-1,0,0)).blocked||grid.nodes.get(currentnode.pos.add(0,0,-1)).blocked)
                        continue;
                    break;
                case 6:
                    if((successor=grid.nodes.get(currentnode.pos.add(-1,0,1)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(-1,0,1), addlayer);
                    }
                    if(grid.nodes.get(currentnode.pos.add(-1,0,0)).blocked||grid.nodes.get(currentnode.pos.add(0,0,1)).blocked)
                        continue;
                    break;
                case 7:
                    if((successor=grid.nodes.get(currentnode.pos.add(1,0,1)))==null)
                    {
                        successor= new MyAstarNode(currentnode.pos.add(1,0,1), addlayer);
                    }
                    if(grid.nodes.get(currentnode.pos.add(1,0,0)).blocked||grid.nodes.get(currentnode.pos.add(0,0,1)).blocked)
                        continue;
                    break;
            }

            if(closedlist.containsKey(successor.pos))
            {
                continue;
            }
            else{
                grid.nodes.put(successor.pos,successor);
                if(!successor.blocked)
                {

                    double tentative_g= currentnode.g+(i<4?1:1.41421);
                    if(openlist.contains(successor)&&tentative_g>= successor.g){

                    }else {
                        successor.pred=currentnode;
                        successor.g= tentative_g;
                        double f= tentative_g+(targetVec.distanceSq(successor.pos));
                        if(f<lowestf){
                            lowestf=f;
                            lowestfNode=successor;
                        }

                        successor.f=f;
                        openlist.remove(successor);
                        openlist.add(successor);

                    }
                }
            }
        }

    }
}
