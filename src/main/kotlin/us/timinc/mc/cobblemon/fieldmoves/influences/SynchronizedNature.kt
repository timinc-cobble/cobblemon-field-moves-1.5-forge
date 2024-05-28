package us.timinc.mc.cobblemon.fieldmoves.influences

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.pokemon.Nature
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves.config
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves.debug
import kotlin.random.Random.Default.nextDouble

class SynchronizedNature(val player: ServerPlayer) : SpawningInfluence {
    override fun affectAction(action: SpawnAction<*>) {
        if (action !is PokemonSpawnAction) return
        if (action.props.nature != null) return

        val nature = getSynchronizedNature(player)?.name?.path ?: return

        val roll = nextDouble()

        if (roll < config.synchronizeChance) {
            debug("Setting wild ${action.props.species} to nature $nature")
            action.props.nature = nature
        }
    }

    private fun getSynchronizedNature(player: Player): Nature? {
        val playerPartyStore = Cobblemon.storage.getParty(player.uuid)
        if (config.mustBeFirst) {
            val firstPartyMember = playerPartyStore.firstOrNull()
            if (firstPartyMember?.ability?.name != "synchronize") {
                debug("First party member does not have synchronize for ${player.name.string}")
                return null
            }
            return firstPartyMember.nature
        }

        val synchronizePartyMember = playerPartyStore.find { it.ability.name == "synchronize" }
        if (synchronizePartyMember == null) {
            debug("No party member has synchronize for ${player.name.string}")
            return null
        }

        debug("${player.name.string}'s ${synchronizePartyMember.species.name} has synchronize and nature ${synchronizePartyMember.nature}")
        return synchronizePartyMember.nature
    }
}