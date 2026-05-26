package hauveli.hexlikes.hexcasting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import hauveli.hexlikes.Constants.MOD_ID
import hauveli.hexlikes.hexcasting.actions.OpGetBobbert
import hauveli.hexlikes.hexcasting.actions.OpGetBobbertsFriend
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

// https://github.com/YukkuriC/HexOverpowered/blob/1.21/common/src/main/java/io/yukkuric/hexop/actions/HexOPActions.kt
// but in JAVA!!!
class HexlikesActions {
    companion object {
        private val CACHED: MutableMap<ResourceLocation, ActionRegistryEntry> = HashMap()

        init {
            wrap("get_bobber","weeed",HexDir.NORTH_WEST,  OpGetBobbert)
            wrap("get_bobber_passenger", "ddwaa", HexDir.NORTH_WEST, OpGetBobbertsFriend)
        }

        @JvmStatic
        fun registerActions(handler: java.util.function.BiConsumer<ResourceLocation, ActionRegistryEntry>) {
            for ((key, value) in CACHED) handler.accept(key, value)
        }

        private fun wrap(name: String, signature: String, dir: HexDir, action: Action?): ActionRegistryEntry {
            val pattern = HexPattern.fromAngles(signature, dir)
            val key = ResourceLocation.fromNamespaceAndPath(MOD_ID, name)
            val entry = ActionRegistryEntry(pattern, action)
            CACHED[key] = entry
            return entry
        }
    }
}