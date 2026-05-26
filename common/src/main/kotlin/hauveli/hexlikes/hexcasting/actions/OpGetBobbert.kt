package hauveli.hexlikes.hexcasting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import com.li64.tide.registries.entities.misc.fishing.HookAccessor
import net.minecraft.world.entity.player.Player

object OpGetBobbert : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val activeHook = HookAccessor.getHook(env.castingEntity as Player) ?: return listOf(NullIota())
        val entity = activeHook.selfAndPassengers.findFirst().get()
        return listOf(EntityIota(entity))
    }
}