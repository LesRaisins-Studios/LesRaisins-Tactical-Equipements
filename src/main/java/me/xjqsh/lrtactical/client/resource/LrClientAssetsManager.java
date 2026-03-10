package me.xjqsh.lrtactical.client.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.resource.display.ConsumableDisplayInstance;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import me.xjqsh.lrtactical.client.resource.display.ThrowableDisplayInstance;
import me.xjqsh.lrtactical.client.resource.manager.ConsumableDisplayManager;
import me.xjqsh.lrtactical.client.resource.manager.MeleeDisplayManager;
import me.xjqsh.lrtactical.client.resource.manager.ThrowableDisplayManager;
import me.xjqsh.lrtactical.init.ModItems;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = EquipmentMod.MOD_ID)
public enum LrClientAssetsManager {
    INSTANCE;
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
            .create();

    private ThrowableDisplayManager throwableDisplay;
    private MeleeDisplayManager meleeDisplay;
    private ConsumableDisplayManager consumableDisplay;

    public void reloadAndRegister(Consumer<PreparableReloadListener> register) {
        consumableDisplay = new ConsumableDisplayManager(GSON);
        throwableDisplay = new ThrowableDisplayManager(GSON);
        meleeDisplay = new MeleeDisplayManager(GSON);

        register.accept(consumableDisplay);
        register.accept(throwableDisplay);
        register.accept(meleeDisplay);
    }

    public ConsumableDisplayInstance getConsumableDisplay(ResourceLocation id) {
        return consumableDisplay.getData(id);
    }

    public ThrowableDisplayInstance getThrowableDisplay(ResourceLocation id) {
        return throwableDisplay.getData(id);
    }

    public MeleeDisplayInstance getMeleeDisplay(ResourceLocation id) {
        return meleeDisplay.getData(id);
    }

    // 要排在tacz后，因为我们要用到tacz的资源
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientResourceReload(RegisterClientReloadListenersEvent event) {
        LrClientAssetsManager.INSTANCE.reloadAndRegister(event::registerReloadListener);
        event.registerReloadListener(IClientItemExtensions.of(ModItems.FLASH_SHIELD.get()).getCustomRenderer());
    }
}
