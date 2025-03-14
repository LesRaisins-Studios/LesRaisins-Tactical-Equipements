package me.xjqsh.lrtactical.client.audio;

import me.xjqsh.lrtactical.init.ModEffects;
import me.xjqsh.lrtactical.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class StunRingingSound extends AbstractTickableSoundInstance
{
    public StunRingingSound()
    {
        super(ModSounds.DEAFENED_RING.get(), SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.attenuation = Attenuation.NONE;
        this.tick();
    }

    @Override
    public void tick()
    {
        Player player = Minecraft.getInstance().player;
        if(player != null && player.isAlive())
        {
            MobEffectInstance effect = player.getEffect(ModEffects.DEAFENED.get());
            if(effect != null)
            {
                this.x = (float) player.getX();
                this.y = (float) player.getY();
                this.z = (float) player.getZ();
                this.volume = Math.min((effect.getDuration() / 100f), 1f);
                return;
            }
        }

        //Stops playing the sound
        this.stop();
    }
}