package hauveli.fishcasting.common.registries;

import hauveli.fishcasting.common.CursedEntity;
import hauveli.fishcasting.common.chair.TackleBoxChairEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class FishcastingEntityTypes {
    // Neither of these entities are finished...
    // Provides access to Allay
    public static final EntityType<CursedEntity> CURSED =
            EntityType.Builder
                    .of(CursedEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.35F)
                    .clientTrackingRange(8)
                    .build("cursed");

    // Provides access to budding amethyst (eventually)
    public static final EntityType<CursedEntity> BLESSED =
            EntityType.Builder
                    .of(CursedEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("blessed");

    // this one is, though!
    public static final EntityType<TackleBoxChairEntity> TACKLEBOX_CHAIR =
            EntityType.Builder.<TackleBoxChairEntity> // guh
                            of(TackleBoxChairEntity::new, MobCategory.MISC)
                            .sized(11.0F/16.0F, 8.0F/16.0F) // eyeballing it, todo: put exact values
                            .clientTrackingRange(10) // uhh enough to see it before players? idk
                            .build("tacklebox_chair");
}