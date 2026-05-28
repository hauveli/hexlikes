package hauveli.hexlikes.mixin;


import com.li64.tide.client.gui.overlays.CastBarOverlay;
import com.llamalad7.mixinextras.sugar.Local;
import hauveli.hexlikes.common.paraphernalia.HexyRodItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hauveli.hexlikes.Constants.MOD_ID;

@Mixin(CastBarOverlay.class)
public class WindUpCastBarOverlayMixin {
    @Unique
    private static ResourceLocation BAR_HEXY_TEX = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/fishing/cast_bar_hexy.png");

    @Unique
    private static float hexyDischargePercent = 0.0f;

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V",
                    shift = At.Shift.BEFORE // scary
            )
    )
    private static void beforeDisableBlend(
            GuiGraphics graphics,
            float deltaTicks,
            CallbackInfo ci,
            @Local(name = "x") int x,
            @Local(name = "y") int y,
            @Local(name = "fillWidth") int fillWidth,
            @Local(name = "texWidth") int texWidth,
            @Local(name = "texHeight") int texHeight
    ) {
        int antiFillWidth = (int) Math.ceil((1.0f - HexyRodItem.getHexyDischargePercent()) * texWidth);
        if (antiFillWidth > 0) {
            graphics.blit(BAR_HEXY_TEX,
                    x,
                    y,
                    0, 0,
                    antiFillWidth, texHeight, texWidth, texHeight);
        }
    }
}