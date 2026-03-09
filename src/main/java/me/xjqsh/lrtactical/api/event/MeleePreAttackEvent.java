package me.xjqsh.lrtactical.api.event;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class MeleePreAttackEvent extends Event {
    private final int playerId;
    private final MeleeAction state;
    private final ResourceLocation animationId;
    private AbstractClientPlayer player;

    public MeleePreAttackEvent(int playerId, MeleeAction state, @Nullable ResourceLocation animationId) {
        this.playerId = playerId;
        this.state = state;
        this.animationId = animationId;
        if (playerId > 0 && Minecraft.getInstance().level != null) {
            var entity = Minecraft.getInstance().level.getEntity(playerId);
            if (entity instanceof AbstractClientPlayer p) {
                this.player = p;
            }
        }
    }

    @Nullable
    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public int getPlayerId() {
        return playerId;
    }

    public MeleeAction getState() {
        return state;
    }

    @Nullable
    public ResourceLocation getAnimationId() {
        return animationId;
    }
}
