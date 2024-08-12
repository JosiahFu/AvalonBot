package bot.avalon.kord

import bot.avalon.data.Role
import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import kotlin.enums.enumEntries

fun ActionRowBuilder.optionalRoleButtons(enabled: Collection<Role>) {
    for (role in enumEntries<Role>()) {
        if (role.isOptional) {
            interactionButton(if (role in enabled) ButtonStyle.Success else ButtonStyle.Secondary, role.name) {
                label = "${role}: ${if (role in enabled) "Enabled" else "Disabled"}"
            }
        }
    }
}
