package hauveli.hexlikes.mixin;

import hauveli.hexlikes.common.CursedEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static hauveli.hexlikes.common.registries.HexlikesItemsJ.CURSED;

@Mixin(ItemEntity.class)
public class CursedMostlyIndestructibleItemEntityMixin {
    // I couldn't figure out a better way.
    // Hopefully injecting into ItemEntity's hurt method is negligible, as it is called (usually) at most once
    // per item, and I don't see people getting too many CURSEDs

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void makeIndestructible(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity entity = (ItemEntity) (Object) this;
        ItemStack stack = entity.getItem();
        if (stack.is(CURSED)) {
            cir.setReturnValue(CursedEntity.relevantDamageSource(source));
            CursedEntity.doAllaySpawnOnLightningHitItem(entity, source);
        }
    }
}