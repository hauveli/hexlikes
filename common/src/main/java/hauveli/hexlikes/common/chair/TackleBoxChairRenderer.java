package hauveli.hexlikes.common.chair;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Locale;
import java.util.Map;

import static hauveli.hexlikes.Constants.MOD_ID;
import static hauveli.hexlikes.common.chair.TackleBoxChairModel.LAYER_LOCATION;

public class TackleBoxChairRenderer extends EntityRenderer<TackleBoxChairEntity> {

    private final TackleBoxChairModel model;

    public TackleBoxChairRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TackleBoxChairModel(
                context.bakeLayer(LAYER_LOCATION)
        );
    }

    @Override
    public void render(TackleBoxChairEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw + 90));
        VertexConsumer vertexconsumer = bufferSource.getBuffer(this.model.renderType(LAYER_LOCATION.getModel()));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        /*
        if (!p_entity.isUnderWater()) {
            VertexConsumer vertexconsumer1 = bufferSource.getBuffer(RenderType.waterMask());
            model.waterPatch().render(poseStack, vertexconsumer1, packedLight, OverlayTexture.NO_OVERLAY);
        }

         */
        poseStack.popPose();
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TackleBoxChairEntity tackleBoxChairEntity) {
        return LAYER_LOCATION.getModel();
    }
}
