package hauveli.hexlikes.common.registries;

import hauveli.hexlikes.common.CursedEntity;
import hauveli.hexlikes.common.chair.TackleBoxChairEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class HexlikesEntityTypes {
    public static final EntityType<CursedEntity> CURSED =
            EntityType.Builder
                    .of(CursedEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.35F)
                    .clientTrackingRange(8)
                    .build("cursed");

    public static final EntityType<TackleBoxChairEntity> TACKLEBOX_CHAIR =
            EntityType.Builder.<TackleBoxChairEntity> // guh
                            of(TackleBoxChairEntity::new, MobCategory.MISC)
                            .sized(11.0F/16.0F, 8.0F/16.0F) // eyeballing it, todo: put exact values
                            .clientTrackingRange(10) // uhh enough to see it before players? idk
                            .build("tacklebox_chair");
}