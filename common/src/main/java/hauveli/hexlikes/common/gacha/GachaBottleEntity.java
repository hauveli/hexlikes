package hauveli.hexlikes.common.gacha;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.iota.*;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import com.li64.tide.data.loot.LootTableRef;
import hauveli.hexlikes.common.registries.HexlikesItemsJ;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
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

        ServerLevel serverLevel = (ServerLevel) this.level();
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, owner.position())
                .withParameter(LootContextParams.THIS_ENTITY, owner)
                .create(LootContextParamSets.GIFT);

        ObjectArrayList<ItemStack> generatedLoot = lootTable.getRandomItems(params);

        for (ItemStack loot : generatedLoot) {
            if (loot.getItem() instanceof IotaHolderItem iotaHolderItem) {
                // Dispensing it with a machine will yield non-written-to thoughtknots/scrolls every time
                if (owner instanceof ServerPlayer serverPlayer) {
                    if (random.nextFloat() > 0.075) { // about 92.5% of the time, apply datum
                        if (loot.getItem() instanceof ItemScroll itemScroll) {
                            itemScroll.writeDatum(
                                    loot, new PatternIota(getRandomPattern())
                            );
                        } else {
                            iotaHolderItem.writeDatum(
                                    loot, generateRandomIota()
                            );
                        }
                    }
                }
            }

            this.spawnAtLocation(loot);
        }

        /*
            I do it this way because it's low effort while accomplishing a shatter-like effect
            10% 1 shard
            90% 2 shards or more
                -> 50% chance 2 shards
                -> 50% chance 3 shards or more
                    -> 90% chance 3 shards
                    -> 10% chance 4 shards
         */
        this.spawnAtLocation(new ItemStack(HexlikesItemsJ.GLASS_SHARD));
        if (random.nextFloat() > 0.10) {
            this.spawnAtLocation(new ItemStack(HexlikesItemsJ.GLASS_SHARD));
            if (random.nextFloat() > 0.5) {
                this.spawnAtLocation(new ItemStack(HexlikesItemsJ.GLASS_SHARD));
                if (random.nextFloat() > 0.9) {
                    this.spawnAtLocation(new ItemStack(HexlikesItemsJ.GLASS_SHARD));
                }
            }
        }
    }

    public Iota generateRandomIota() {
        //if (level.isClientSide) return new NullIota(); // I don't think this is possible?
        ServerLevel serverLevel  = (ServerLevel) this.level();
        switch (random.nextInt(0,8)) {
            case 0: {
                return new Vec3Iota(findNearestTreasure(serverLevel, this.position()));
            }
            case 1: {
                var entityMaybe = getNearbyMob(serverLevel, this);
                if (entityMaybe != null) {
                    return new EntityIota(entityMaybe);
                }
            }
            case 2: {
                // I think past 53 bits double likely is not prime, I'm setting it to 52 just in case....
                return new DoubleIota(BigInteger.probablePrime(52, random).doubleValue());
            }
            case 3: {
                return new BooleanIota(random.nextBoolean());
            }
            case 4: {
                return new GarbageIota();
            }
            case 5: {
                return new PatternIota(getRandomPattern());
            }
            case 6: {
                // unlikely (impossible with Random?) to recurse forever, but its funny so it stays
                return new ListIota(List.of(generateRandomIota()));
            }
            default: {
                return new NullIota();
            }
        }
    }
    public HexPattern getRandomPattern() {
        List<HexPattern> patterns = new ArrayList<>();
        HexActions.register((entry, id) -> {
            patterns.add(entry.prototype()); // heehee...
        });
        return patterns.get(random.nextInt(0, patterns.size()));
    }

    public Entity getNearbyMob(
            ServerLevel level,
            Entity entity
    ) {
        List<Mob> nearbyMobs = level.getEntitiesOfClass(
                Mob.class,
                entity.getBoundingBox().inflate(128)
        );
        // mob can not be player, so no true names will be written, I think
        if (nearbyMobs.isEmpty()) {
            return null;
        }
        return nearbyMobs.get(random.nextInt(0, nearbyMobs.size()));
    }

    public static Vec3 findNearestTreasure(ServerLevel level, Vec3 origin) {
        BlockPos start = BlockPos.containing(origin);
        int radius = 100;
        BlockPos treasurePos = level.findNearestMapStructure(
                StructureTags.ON_TREASURE_MAPS,
                start,
                radius,
                false
        );

        if (treasurePos == null) {
            return Vec3.ZERO;
        }

        return Vec3.atCenterOf(treasurePos).subtract(origin);
    }


    // I didnt know how else to obtain a reference to this
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

    List<ResourceKey<LootTable>> LOOT_TABLES = List.of(
            // God stupidest solution
            RANDOM_CYPHER_TABLE,
            RANDOM_SCROLL_TABLE,
            RANDOM_SCROLL_TABLE,
            RANDOM_SCROLL_TABLE,
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey(),
            MISC_JUNK_TABLE.getKey()
    );

    Random random = new Random();
}
