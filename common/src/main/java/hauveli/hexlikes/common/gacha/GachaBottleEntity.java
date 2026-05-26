package hauveli.hexlikes.common.gacha;

import at.petrak.hexcasting.api.HexAPI;
import com.li64.tide.data.loot.LootTableRef;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static hauveli.hexlikes.Constants.MOD_ID;

public class GachaBottleEntity extends ThrownPotion {
    public GachaBottleEntity(EntityType<? extends ThrownPotion> entityType, Level level) {
        super(entityType, level);
    }

    public GachaBottleEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    public GachaBottleEntity(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            generateRandomLoot();
            this.discard();
        }
    }

    private void applyWater() {
        return;
    }

    private void applySplash(Iterable<MobEffectInstance> effects, @Nullable Entity p_entity) {
        return;
    }

    private void makeAreaOfEffectCloud(PotionContents potionContents) {
        return;
    }

    private boolean isLingering() {
        return false;
    }

    private void dowseFire(BlockPos pos) {
        return;
    }

    void generateRandomLoot() {
        MinecraftServer server = this.getServer();
        Entity owner = this.getOwner();
        LootTable lootTable = server
                .reloadableRegistries()
                .getLootTable(LOOT_TABLES.get(random.nextInt(0,LOOT_TABLES.size()-1)));

        LootParams params = new LootParams.Builder((ServerLevel) this.level())
                .withParameter(LootContextParams.ORIGIN, owner.position())
                .withParameter(LootContextParams.THIS_ENTITY, owner)
                .create(LootContextParamSets.GIFT);

        ObjectArrayList<ItemStack> generatedLoot = lootTable.getRandomItems(params);

        for (ItemStack loot : generatedLoot) {
            this.spawnAtLocation(loot);
        }

        // 2 % chance for glass tee-hee
        // I considered adding glass shards, I'm not sure if I like that since it would just be bloat...
        // I should maybe do this via datapack but whatever....
        if (random.nextFloat() > 0.98) {
            this.spawnAtLocation(new ItemStack(Items.GLASS));
        }
    }


    ResourceKey<LootTable> RANDOM_SCROLL_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, "random_scroll")
    );
    ResourceKey<LootTable> RANDOM_CYPHER_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, "random_cypher")
    );


    LootTableRef MISC_JUNK_TABLE = LootTableRef.createNew(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "gameplay/fishing/message_in_a_bottle_junk")
    );

    // I should move this out of here but I'm busy accruing tech debt
    LootTableRef WARM_OCEAN_JUNK = LootTableRef.createNew(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "gameplay/fishing/warm_ocean_junk")
    );

    // Least common to most common in powers of 2: 1, 2, 4, 8
    List<ResourceKey<LootTable>> LOOT_TABLES = List.of(
            // God stupidest solution
            RANDOM_SCROLL_TABLE,
            RANDOM_CYPHER_TABLE,
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey()
    );

    Random random = new Random();
}
