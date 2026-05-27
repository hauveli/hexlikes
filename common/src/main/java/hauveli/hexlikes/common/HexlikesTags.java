package hauveli.hexlikes.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static hauveli.hexlikes.Constants.MOD_ID;

public class HexlikesTags {
    // Tags
    public static final TagKey<Item> FUN = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "artifact_grade_u"));
    public static final TagKey<Item> NO_ENTITY_COLLISION_HOOK = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "hookless_hooks"));
    public static final TagKey<Item> MOB_PACIFYING_LINES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "loud_lines"));
    public static final TagKey<Item> LUCK_TWEAKING_BOBBERS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "blessed_bobbers"));
    public static final TagKey<Item> MUSIC_DISCS_FROM_FISHING = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "fishy_music_discs"));
    public static final TagKey<Item> NO_DURABILITY_ENCHANTMENTS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "no_durability_enchantments"));
    public static final TagKey<Item> LORE_FRAGMENTS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "lore_fragments"));
    public static final TagKey<Item> UNLUCKY_MULCH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "unlucky_mulch"));
}
