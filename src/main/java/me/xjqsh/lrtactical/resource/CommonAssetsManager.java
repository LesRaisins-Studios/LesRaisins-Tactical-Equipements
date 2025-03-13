package me.xjqsh.lrtactical.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.network.DataType;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SPackSyncMessage;
import me.xjqsh.lrtactical.resource.manager.ThrowableIndexManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class CommonAssetsManager implements ICommonResourceProvider {
    public static CommonAssetsManager INSTANCE;
    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    private CommonAssetsManager() {
    }

    public static ICommonResourceProvider get() {
        return INSTANCE == null ? CommonNetworkCache.INSTANCE : INSTANCE;
    }

    public ThrowableIndexManager throwableIndexManager;

    private void reloadAndRegister(Consumer<PreparableReloadListener> register) {
        throwableIndexManager = new ThrowableIndexManager(GSON);
        register.accept(throwableIndexManager);
    }

    public Map<DataType, Map<ResourceLocation, String>> toNetwork() {
        return Map.of(DataType.THROWABLE_INDEX, throwableIndexManager.getCache());
    }

    @Override
    public ThrowableIndex<?, ?> getThrowableIndex(ResourceLocation id) {
        return throwableIndexManager.getData(id);
    }

    @Override
    public Collection<ThrowableIndex<?, ?>> getThrowableIndexes() {
        return throwableIndexManager.getAllData().values();
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        var commonAssetsManager = new CommonAssetsManager();
        commonAssetsManager.reloadAndRegister(event::addListener);
        INSTANCE = commonAssetsManager;
    }

    @SubscribeEvent
    public static void OnDatapackSync(OnDatapackSyncEvent event) {
        if (CommonAssetsManager.INSTANCE == null) {
            return;
        }
        var msg = new SPackSyncMessage(CommonAssetsManager.INSTANCE.toNetwork());
        if (event.getPlayer() != null) {
            NetworkHandler.sendToClientPlayer(msg, event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> NetworkHandler.sendToClientPlayer(msg, player));
        }
    }
}
