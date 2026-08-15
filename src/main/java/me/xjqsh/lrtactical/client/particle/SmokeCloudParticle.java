package me.xjqsh.lrtactical.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmokeCloudParticle extends TextureSheetParticle {
    public static SmokeCloudParticleProvider provider(SpriteSet spriteSet) {
        return new SmokeCloudParticleProvider(spriteSet);
    }

    public static class SmokeCloudParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public SmokeCloudParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SmokeCloudParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

    private final SpriteSet spriteSet;

    protected SmokeCloudParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;
        this.quadSize *= 5.5f;
        this.lifetime = 20;
        this.gravity = 0f;
        this.hasPhysics = false;
        this.xd = vx * 1;
        this.yd = vy * 1;
        this.zd = vz * 1;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightColor(float partialTick) {
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        if (!this.level.hasChunkAt(pos)) {
            return 0;
        }
        int light = LevelRenderer.getLightColor(this.level, pos);
        int sky = Math.max(light >> 20 & 15, 2);
        int block = Math.max(light >> 4 & 15, 2);
        // 粒子中心陷入实心方块时该格光照为 0，会直接渲染成黑色。
        // 如果低于下限，尝试采样周围方块取最大值
        if (sky <= 2 && block <= 2) {
            for (Direction direction : Direction.values()) {
                int neighbor = LevelRenderer.getLightColor(this.level, pos.relative(direction));
                sky = Math.max(sky, neighbor >> 20 & 15);
                block = Math.max(block, neighbor >> 4 & 15);
            }
        }
        return sky << 20 | block << 4;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        super.render(pBuffer, pRenderInfo, pPartialTicks);
    }

    @Override
    public boolean shouldCull() {
        return true;
    }

}
