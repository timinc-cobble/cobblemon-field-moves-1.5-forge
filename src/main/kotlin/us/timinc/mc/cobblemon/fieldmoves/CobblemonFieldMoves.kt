package us.timinc.mc.cobblemon.fieldmoves

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import us.timinc.mc.cobblemon.fieldmoves.config.Config
import us.timinc.mc.cobblemon.fieldmoves.config.ConfigBuilder
import us.timinc.mc.cobblemon.fieldmoves.influences.SynchronizedNature

@Mod(CobblemonFieldMoves.MOD_ID)
object CobblemonFieldMoves {
    const val MOD_ID = "field_moves"
    var config: Config = ConfigBuilder.load(Config::class.java, MOD_ID)

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.FORGE)
    object Registration {
        @SubscribeEvent
        fun onInit(e: ServerStartedEvent) {
            CobblemonEvents.BATTLE_VICTORY.subscribe { evt ->
                val world = evt.winners.flatMap { it.pokemonList }.firstNotNullOfOrNull { it.entity?.level() }
                    ?: return@subscribe
                val server = world.server
                for (winner in evt.winners) {
                    val position = winner.pokemonList.firstNotNullOf { it.entity }.position()
                    for (battlePokemon in winner.pokemonList) {
                        val pokemon = battlePokemon.effectedPokemon
                        debug("Rolling for ${pokemon.ability.name} on ${pokemon.getDisplayName().string}")

                        if (!pokemon.heldItem().isEmpty) {
                            debug("${pokemon.getDisplayName().string} is already holding an item")
                            continue
                        }

                        val identifier = ResourceLocation("pickup", "gameplay/${pokemon.ability.name}")
                        val lootManager = server!!.lootData
                        val lootTable = lootManager.getLootTable(identifier)
                        val list = lootTable.getRandomItems(
                            LootParams.Builder(world as ServerLevel)
                                .withParameter(LootContextParams.ORIGIN, position)
                                .create(
                                    LootContextParamSets.EMPTY
                                )
                        )

                        if (list.isEmpty) {
                            debug("Attempted to roll for ability ${pokemon.ability.name} but nothing dropped.")
                            continue
                        }

                        pokemon.swapHeldItem(list.first())
                    }
                }
            }
            PlayerSpawnerFactory.influenceBuilders.add { SynchronizedNature(it) }
        }
    }

    fun debug(msg: String) {
        if (!config.debug) return
        println(msg)
    }
}