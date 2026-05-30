package hauveli.fishcasting.common.chair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hauveli.fishcasting.Fishcasting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;

public class TackleBoxChairModel<T extends Entity> extends EntityModel<T> {

    // why can't I just import a .json for this........
    private final ModelPart bone;

    public TackleBoxChairModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    // So that I can re-remember that this is what the first argument in "model layer location" is meant to be
    private static final ResourceLocation TEXTURE = Fishcasting.id("textures/entity/tacklebox_chair.png");

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            TEXTURE,
            "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 18).addBox(-14.0F, -8.0F, 3.0F, 12.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-12.0F, -4.0F, 1.0F, 8.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-12.0F, -6.0F, 0.0F, 8.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(40, 43).addBox(-14.0F, -2.0F, 4.0F, 12.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(40, 43).addBox(-14.0F, -2.0F, 12.0F, 12.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition backsupport_r1 = bone.addOrReplaceChild("backsupport_r1", CubeListBuilder.create().texOffs(44, 27).addBox(-7.0F, -7.725F, 15.0F, 0.01F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(44, 45).addBox(-7.0F, -9.725F, 14.0F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(44, 45).addBox(-7.0F, -9.725F, 23.0F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -6.0F, -11.0F, 0.0F, 0.0F, 0.3927F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        bone.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(T t, float v, float v1, float v2, float v3, float v4) {

    }
}
