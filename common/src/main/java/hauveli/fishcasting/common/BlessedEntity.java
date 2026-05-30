package hauveli.fishcasting.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;

// Robed fisher that goes around fishing in underground caves
// This is meant to be a wandering trader-like but it just fishes up stuff and then goes away
// after fishing up a random number of fish (which the player can steal hehe...)
// should be targetted by the same mobs that target villagers
// Ideally, it has a (low?) chance to fish up Crystal Shrimp or Crystalline Carp
// maybe a similar look to The Traveler?
public class BlessedEntity extends Villager {
    public BlessedEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public BlessedEntity(EntityType<? extends Villager> entityType, Level level, VillagerType villagerType) {
        super(entityType, level, villagerType);
    }
}
