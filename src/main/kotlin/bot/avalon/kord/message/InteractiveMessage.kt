package bot.avalon.kord.message

import bot.avalon.kord.StartMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.message.MessageBuilder

val messageTypes: List<InteractiveMessage> = listOf(
    StartMessage
)

abstract class InteractiveMessage {
    protected open suspend fun content(interaction: ActionInteraction): String = ""
    protected open suspend fun MessageBuilder.embeds(interaction: ActionInteraction) {}
    protected abstract suspend fun MessageBuilder.components(interaction: ActionInteraction)

    protected suspend fun ButtonInteraction.updateContent() {
        this.message.edit {
            content = content(this@updateContent)
        }
    }

    protected suspend fun ButtonInteraction.updateEmbeds() {
        this.message.edit {
            embeds(this@updateEmbeds)
        }
    }

    protected suspend fun ButtonInteraction.updateComponents() {
        this.message.edit {
            components(this@updateComponents)
        }
    }

    protected suspend fun ButtonInteraction.updateAll() {
        this.message.edit {
            using(this@InteractiveMessage, this@updateAll)
        }
    }

    open suspend fun onInteract(interaction: ButtonInteraction, componentId: String) {}

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
