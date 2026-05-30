package hauveli.fishcasting.common.registries;

import hauveli.fishcasting.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class FishcastingTags {
    // Tags
    public static final TagKey<Item> FUN = TagKey.create(Registries.ITEM, Constants.id("artifact_grade_u"));
    public static final TagKey<Item> NO_ENTITY_COLLISION_HOOK = TagKey.create(Registries.ITEM, Constants.id("hookless_hooks"));
    public static final TagKey<Item> MOB_PACIFYING_LINES = TagKey.create(Registries.ITEM, Constants.id("loud_lines"));
    public static final TagKey<Item> LUCK_TWEAKING_BOBBERS = TagKey.create(Registries.ITEM, Constants.id("blessed_bobbers"));
    public static final TagKey<Item> MUSIC_DISCS_FROM_FISHING = TagKey.create(Registries.ITEM, Constants.id("fishy_music_discs"));
    public static final TagKey<Item> NO_DURABILITY_ENCHANTMENTS = TagKey.create(Registries.ITEM, Constants.id("no_durability_enchantments"));
    public static final TagKey<Item> LORE_FRAGMENTS = TagKey.create(Registries.ITEM, Constants.id("lore_fragments"));
    public static final TagKey<Item> UNLUCKY_MULCH = TagKey.create(Registries.ITEM, Constants.id("unlucky_mulch"));
}
