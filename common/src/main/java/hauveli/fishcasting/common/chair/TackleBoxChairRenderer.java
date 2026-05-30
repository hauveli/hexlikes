package hauveli.fishcasting.common.chair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import static hauveli.fishcasting.common.chair.TackleBoxChairModel.LAYER_LOCATION;

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
