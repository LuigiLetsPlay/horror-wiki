package org.luigilp.horrorwiki;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Horror_wiki.MODID)
public class Horror_wiki {
    private static final String PERSISTED_TAG = Player.PERSISTED_NBT_TAG;
    private static final String RECEIVED_BOOK_TAG = "received_horror_wiki";
    public static final String MODID = "horrorwiki";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> HORROR_WIKI = ITEMS.register(
            "horror_wiki",
            () -> new HorrorWikiItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC))
    );

    public Horror_wiki() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag persistedData = player.getPersistentData().getCompound(PERSISTED_TAG);
        if (persistedData.getBoolean(RECEIVED_BOOK_TAG)) {
            return;
        }

        ItemStack stack = new ItemStack(HORROR_WIKI.get());
        boolean added = player.getInventory().add(stack);
        if (!added) {
            player.drop(stack, false);
        }

        persistedData.putBoolean(RECEIVED_BOOK_TAG, true);
        player.getPersistentData().put(PERSISTED_TAG, persistedData);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag originalData = event.getOriginal().getPersistentData().getCompound(PERSISTED_TAG);
        if (originalData.contains(RECEIVED_BOOK_TAG)) {
            event.getEntity().getPersistentData().put(PERSISTED_TAG, originalData.copy());
        }
    }
}
