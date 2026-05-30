package hauveli.fishcasting.mixin;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TideFishingHook.class)
public abstract class HooklessFishingHookTideFishingHookMixin {
    @Shadow
    private Entity hookedIn;

    @Shadow
    public abstract ItemStack getHook();

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void fishcasting$smelly(CallbackInfo ci) {
        if (this.getHook().is(FishcastingTags.NO_ENTITY_COLLISION_HOOK)) {
            this.hookedIn = null;
            ci.cancel();
        }
    }

}
