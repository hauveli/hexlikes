package hauveli.fishcasting.hexcasting.iota;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.li64.tide.data.fishing.FishData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
// import com.samsthenerd.inline.api.data.EntityInlineData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

// https://github.com/SuperKnux/HexMod/blob/indev/1.21.1/Common/src/main/java/at/petrak/hexcasting/api/casting/iota/EntityIota.java
public class FishIota extends Iota {
    private final Optional<FishData> fishData;
    private final ResourceLocation resLoc;

    public FishIota(FishData fishData) {
        super(() -> FishcastingIotaTypes.FISH);
        this.fishData = Optional.ofNullable(fishData);
        assert fishData != null;
        Item fishActually = fishData.fish().value();
        this.resLoc = BuiltInRegistries.ITEM.getKey(fishActually);
    }
    public FishIota(ResourceLocation resourceLocation) {
        super(() -> FishcastingIotaTypes.FISH);
        this.resLoc = resourceLocation;
        this.fishData = FishData.get(BuiltInRegistries.ITEM.get(resourceLocation));
    }

    public Optional<FishData> getFishData() {
        return this.fishData;
    }

    public ResourceLocation getResourceLocation() {
        return this.resLoc;
    }

    public String getNamespacedId() {
        return this.resLoc.getNamespace();
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
                && that instanceof FishIota dent
                && this.fishData == dent.getFishData();
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public Component display() {
        var fish = getFishData();
        if (fish.isPresent()) {
            Item fishItem = fish.get().fish().value();
            return fishItem.getName(fishItem.getDefaultInstance()).copy().withStyle(ChatFormatting.DARK_BLUE);
        }
        return Component.translatable("fishcasting.spelldata.fish.whoknows");
    }

    @Override
    public int hashCode() {
        return resLoc.hashCode(); // should be fine?
    }

    // TODO: just yoink the entityIota getEntityNameWithInline by creating a fake entity then discarding it hehe...
    private static Component getFishNameWithInline(FishData fishData) {
        Item fishItem = fishData.fish().value();
        ItemStack fishStack = fishItem.getDefaultInstance();
        MutableComponent baseName = (MutableComponent) fishItem.getName(fishStack);
        Component inlineEnt;
        //inlineEnt = EntityInlineData.fromType(entity.getType()).asText(false);
        // hmm maybe later
        return baseName.append(Component.literal(": ")).append(fishItem.getTooltipImage(fishStack).get().toString());
    }



    public static IotaType<FishIota> TYPE = new IotaType<>() {
        public static final MapCodec<FishIota> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        ResourceLocation.CODEC.fieldOf("namespacedId")
                                .forGetter(FishIota::getResourceLocation)
                ).apply(inst, FishIota::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, FishIota> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC,
                        FishIota::getResourceLocation,
                        FishIota::new
                );

        @Override
        public boolean validate(FishIota iota, ServerLevel level) {
            var fish = iota.getFishData();
            return fish.isPresent();
        }

        @Override
        public MapCodec<FishIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FishIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_69420f;
        }
    };
}