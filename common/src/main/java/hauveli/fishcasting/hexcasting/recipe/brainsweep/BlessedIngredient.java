package hauveli.fishcasting.hexcasting.recipe.brainsweep;

import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.jetbrains.annotations.Nullable;

public class BlessedIngredient extends VillagerIngredient {

    public BlessedIngredient(@Nullable VillagerProfession profession, @Nullable VillagerType biome, int minLevel) {
        super(profession, biome, minLevel);
    }


    public static VillagerIngredient deserialize(JsonObject json) {
        VillagerProfession profession = null;
        if (json.has("profession") && !json.get("profession").isJsonNull()) {
            profession = BuiltInRegistries.VILLAGER_PROFESSION.get(
                    ResourceLocation.parse(
                            GsonHelper.getAsString(json, "profession")
                    )
            );
        }
        VillagerType biome = null;
        if (json.has("biome") && !json.get("biome").isJsonNull()) {
            biome = BuiltInRegistries.VILLAGER_TYPE.get(
                    ResourceLocation.parse(
                            GsonHelper.getAsString(json, "biome")
                    )
            );
        }
        int minLevel = GsonHelper.getAsInt(json, "minLevel");
        return new VillagerIngredient(profession, biome, minLevel);
    }

    public static VillagerIngredient read(FriendlyByteBuf buf) {
        VillagerProfession profession = null;
        var hasProfession = buf.readVarInt();
        if (hasProfession != 0) {
            profession = BuiltInRegistries.VILLAGER_PROFESSION.byId(buf.readVarInt());
        }
        VillagerType biome = null;
        var hasBiome = buf.readVarInt();
        if (hasBiome != 0) {
            biome = BuiltInRegistries.VILLAGER_TYPE.byId(buf.readVarInt());
        }
        int minLevel = buf.readInt();
        return new VillagerIngredient(profession, biome, minLevel);
    }
}
