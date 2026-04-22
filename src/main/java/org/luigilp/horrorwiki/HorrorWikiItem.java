package org.luigilp.horrorwiki;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.api.PatchouliAPI;

public class HorrorWikiItem extends Item {
    private static final ResourceLocation BOOK_ID = new ResourceLocation("horrorwiki", "horror_book");

    public HorrorWikiItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            PatchouliAPI.get().openBookGUI(BOOK_ID);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
