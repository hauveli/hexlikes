package hauveli.fishcasting.mixin;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.llamalad7.mixinextras.sugar.Local;
import hauveli.fishcasting.Fishcasting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TideFishingHook.class)
public abstract class TagRecentlyCaughtByTideFishingHookMixin {

    @Shadow
    protected ItemStack rod;
    @Shadow
    public abstract ItemStack getHook();
    @Shadow
    public abstract ItemStack getBobber();
    // This mixin is SPECIFICALLY for the hexcasting pattern which obtains the entity hooked to it.
    // without this, I'd need to do something else to obtain a reference to this mob which would allow
    // me to be certain that it is the right entity
    @Inject(
            method = "retrieve(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;)I",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;catchType:Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook$CatchType;"
            )
    )
    public void tagTheFishButNotCompoundTag(ItemStack rod,
                                            ServerLevel level,
                                            Player player,
                                            CallbackInfoReturnable<Integer> cir,
                                            @Local(name = "entity") Entity entity) {
        // "fishingreal" does something which prevents me from obtaining a reference for sure via Tide
        // I will have to look at implementing a special case for that somewhere, if I intend to make this a proper addon
        // (which I do, but dang...)
        if (entity != null) {
            // not fishingreal
            entity.addTag(Fishcasting.FISHBERT_TAG); // this tag vanishes as soon as the item is picked up.
            // todo: it would be nice if the itemEntity were labelled as the owner, or the fish were marked with the fisher's UUID
            // if it does, please open an issue or let me know.
            entity.addTag(player.getStringUUID());
            // This seems to work.
            // Awesome! Now there is no ambiguity, there's no extra ItemStack data, and there's no extra cost! (except for the tags, which are not persistent!)
        } else {
            // fishingreal
            // todo
        }
    }
}
