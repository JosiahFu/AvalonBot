package bot.avalon.kord.message

import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder

@Suppress("UNCHECKED_CAST")
abstract class StatefulMessageType<T: U, U> {
    abstract var ActionInteraction.state: U

    protected open suspend fun content(state: T, kord: Kord): String = ""
    protected open suspend fun MessageBuilder.embeds(state: T, kord: Kord) {}
    protected abstract suspend fun MessageBuilder.components(state: T, kord: Kord, disable: Boolean = false)

    protected suspend fun ComponentInteraction.updateContent(defer: Boolean = true) {
        if (defer) deferPublicMessageUpdate()
        this.message.edit {
            content = content(state as T, kord)
        }
    }

    protected suspend fun ComponentInteraction.updateEmbeds(defer: Boolean = true) {
        if (defer) deferPublicMessageUpdate()
        this.message.edit {
            embeds(state as T, kord)
        }
    }

    protected suspend fun ComponentInteraction.updateComponents(defer: Boolean = true) {
        if (defer) deferPublicMessageUpdate()
        this.message.edit {
            components(state as T, kord)
        }
    }

    protected suspend fun ComponentInteraction.disableComponents(defer: Boolean = false) {
        if (defer) deferPublicMessageUpdate()
        this.message.edit {
            components(state as T, kord, disable = true)
        }
    }

    protected suspend fun ComponentInteraction.updateAll(defer: Boolean = true) {
        if (defer) deferPublicMessageUpdate()
        this.message.edit {
            using(this@StatefulMessageType, state as T, kord)
        }
    }

    suspend fun respondTo(interaction: ActionInteraction) {
        interaction.respondPublic {
            using(this@StatefulMessageType, interaction.state as T, interaction.kord)
        }
    }

    protected open suspend fun onInteract(interaction: ComponentInteraction, state: T, componentId: String, setState: (U) -> Unit ) {}

    suspend fun interact(interaction: ComponentInteraction, componentId: String) {
        this.onInteract(interaction, interaction.state as T, componentId) { interaction.state = it }
    }

    abstract val ids: Collection<String>

    companion object {
        suspend fun <T> MessageBuilder.using(message: StatefulMessageType<T, *>, state: T, kord: Kord) {
            with(message) {
                content = content(state, kord)
                embeds(state, kord)
                components(state, kord)
            }
        }
    }
}

