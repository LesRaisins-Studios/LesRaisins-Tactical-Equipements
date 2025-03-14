package me.xjqsh.lrtactical.util;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;


public class SightTraceUtil {
    // 尝试寻找路径上有没有方块遮挡
    @Nullable
    public static HitResult rayTraceOpaqueBlocks(Entity caster, Level world, Vec3 start, Vec3 end, boolean stopOnLiquid,
                                                 boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
        if(!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z))
        {
            if(!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z))
            {
                int endX = Mth.floor(end.x);
                int endY = Mth.floor(end.y);
                int endZ = Mth.floor(end.z);
                int startX = Mth.floor(start.x);
                int startY = Mth.floor(start.y);
                int startZ = Mth.floor(start.z);
                BlockPos pos = new BlockPos(startX, startY, startZ);
                BlockState stateInside = world.getBlockState(pos);

                // Added light opacity check
                if(stateInside.getLightBlock(world, pos) != 0 && (!ignoreBlockWithoutBoundingBox || stateInside.getCollisionShape(world, pos) != Shapes.empty()))
                {
                    return world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
                }

                HitResult raytraceresult2 = null;
                int limit = 200;
                while(limit-- >= 0)
                {
                    if(Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
                        return null;
                    }

                    if(startX == endX && startY == endY && startZ == endZ) {
                        return null;
                    }

                    boolean completedX = true;
                    boolean completedY = true;
                    boolean completedZ = true;
                    double d0 = 999;
                    double d1 = 999;
                    double d2 = 999;

                    if(endX > startX) {
                        d0 = startX + 1;
                    } else if(endX < startX) {
                        d0 = startX;
                    } else {
                        completedX = false;
                    }

                    if(endY > startY) {
                        d1 = startY + 1;
                    } else if(endY < startY) {
                        d1 = startY;
                    } else {
                        completedY = false;
                    }

                    if(endZ > startZ) {
                        d2 = startZ + 1;
                    } else if(endZ < startZ) {
                        d2 = startZ;
                    } else {
                        completedZ = false;
                    }

                    double d3 = 999;
                    double d4 = 999;
                    double d5 = 999;
                    double d6 = end.x - start.x;
                    double d7 = end.y - start.y;
                    double d8 = end.z - start.z;

                    if(completedX) d3 = (d0 - start.x) / d6;

                    if(completedY) d4 = (d1 - start.y) / d7;

                    if(completedZ) d5 = (d2 - start.z) / d8;

                    if(d3 == -0) d3 = -1.0E-4D;

                    if(d4 == -0) d4 = -1.0E-4D;

                    if(d5 == -0) d5 = -1.0E-4D;

                    Direction direction;

                    if(d3 < d4 && d3 < d5)
                    {
                        direction = endX > startX ? Direction.WEST : Direction.EAST;
                        start = new Vec3(d0, start.y + d7 * d3, start.z + d8 * d3);
                    }
                    else if(d4 < d5)
                    {
                        direction = endY > startY ? Direction.DOWN : Direction.UP;
                        start = new Vec3(start.x + d6 * d4, d1, start.z + d8 * d4);
                    }
                    else
                    {
                        direction = endZ > startZ ? Direction.NORTH : Direction.SOUTH;
                        start = new Vec3(start.x + d6 * d5, start.y + d7 * d5, d2);
                    }

                    startX = Mth.floor(start.x) - (direction == Direction.EAST ? 1 : 0);
                    startY = Mth.floor(start.y) - (direction == Direction.UP ? 1 : 0);
                    startZ = Mth.floor(start.z) - (direction == Direction.SOUTH ? 1 : 0);
                    pos = new BlockPos(startX, startY, startZ);
                    BlockState state = world.getBlockState(pos);

                    // Added light opacity check
                    if(state.getLightBlock(world, pos) != 0 && (!ignoreBlockWithoutBoundingBox || state.getCollisionShape(world, pos) != Shapes.empty()))
                    {
                        return world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
                    }
                }
                return null;
            }
            return null;
        }
        return null;
    }
}
