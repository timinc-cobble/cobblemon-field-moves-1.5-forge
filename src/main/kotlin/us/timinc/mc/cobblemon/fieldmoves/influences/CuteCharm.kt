package us.timinc.mc.cobblemon.fieldmoves.influences

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.pokemon.Gender
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves.config
import us.timinc.mc.cobblemon.fieldmoves.CobblemonFieldMoves.debug
import kotlin.random.Random.Default.nextDouble

class CuteCharm(val player: ServerPlayer) : SpawningInfluence {
    override fun affectAction(action: SpawnAction<*>) {
        if (action !is PokemonSpawnAction) return
        if (action.props.gender != null) return
        val species = action.props.species?.let(PokemonSpecies::getByName) ?: return
        if (species.maleRatio == 1F || species.maleRatio == 0F) {
            debug("Spawning species of $species has a fixed gender, cute charm can't affect")
            return
        }
        val gender = getCuteCharmGender(player) ?: return
        if (nextDouble() >= config.cuteCharmChance) {
            debug("Rolled for cute charm but missed.")
            return
        }
        debug("Setting wild ${action.props.species} to nature $gender")
        action.props.gender = gender
    }
    private fun getCuteCharmGender(player: ServerPlayer): Gender? {
        val playerPartyStore = Cobblemon.storage.getParty(player.uuid)
        if (config.mustBeFirst) {
            val firstPartyMember = playerPartyStore.firstOrNull()
            if (firstPartyMember?.ability?.name != "cutecharm") {
                debug("First party member does not have cute charm for ${player.name.string}")
                return null
            }
            return firstPartyMember.gender
        }
        val cuteCharmPartyMember = playerPartyStore.find { it.ability.name == "cutecharm"}
        if (cuteCharmPartyMember == null) {
            debug("No party member has cute charm for ${player.name.string}")
            return null
        }
        debug("${player.name.string}'s ${cuteCharmPartyMember.species.name} has cute charm and gender ${cuteCharmPartyMember.gender}")
        return cuteCharmPartyMember.gender
    }
}