package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.kord.StartMessage
import bot.avalon.kord.message.InteractiveMessage.Companion.using
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder

val messageTypes: List<InteractiveMessage> = listOf(
    StartMessage,
    DiscussionMessage,
)

abstract class InteractiveMessage {
    protected open suspend fun content(interaction: ActionInteraction): String = ""
    protected open suspend fun MessageBuilder.embeds(interaction: ActionInteraction) {}
    protected abstract suspend fun MessageBuilder.components(interaction: ActionInteraction, disable: Boolean = false)

    protected suspend fun ComponentInteraction.updateContent() {
        this.message.edit {
            content = content(this@updateContent)
        }
        deferPublicMessageUpdate()
    }

    protected suspend fun ComponentInteraction.updateEmbeds() {
        this.message.edit {
            embeds(this@updateEmbeds)
        }
        deferPublicMessageUpdate()
    }

    protected suspend fun ComponentInteraction.updateComponents() {
        this.message.edit {
            components(this@updateComponents)
        }
        deferPublicMessageUpdate()
    }

    protected suspend fun ComponentInteraction.disableComponents() {
        this.message.edit {
            components(this@disableComponents, disable = true)
        }
    }

    protected suspend fun ComponentInteraction.updateAll() {
        this.message.edit {
            using(this@InteractiveMessage, this@updateAll)
        }
        deferPublicMessageUpdate()
    }

    open suspend fun onInteract(interaction: ComponentInteraction, componentId: String) {}

    protected abstract val ids: Collection<String>

    companion object {
        suspend fun MessageBuilder.using(message: InteractiveMessage, interaction: ActionInteraction) {
            with(message) {
                content = content(interaction)
                embeds(interaction)
                components(interaction)
            }
        }

        fun of(componentId: String) = messageTypes.find { componentId in it.ids }!!
    }
}

suspend fun GameState.respondTo(interaction: ActionInteraction) {
    interaction.respondPublic {
        using(messageType, interaction)
    }
    this.message = interaction.getOriginalInteractionResponse().id
}
