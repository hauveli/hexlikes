package hauveli.fishcasting.mixin.tacklebox_chair;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BucketItem.class)
public interface ContentAccessorBucketItemMixin {
        @Accessor("content")
        Fluid getContent();
}
