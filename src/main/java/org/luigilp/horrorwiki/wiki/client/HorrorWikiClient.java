package org.luigilp.horrorwiki.wiki.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.luigilp.horrorwiki.Horror_wiki;
import org.luigilp.horrorwiki.wiki.load.HorrorWikiManager;

@Mod.EventBusSubscriber(modid = Horror_wiki.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HorrorWikiClient {
    private HorrorWikiClient() {
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(HorrorWikiManager.getInstance());
    }
}
