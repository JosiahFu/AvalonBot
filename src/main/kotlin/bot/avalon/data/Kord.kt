package bot.avalon.data

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object KordSerializer {
    lateinit var kord: Kord

    private fun Decoder.decodeSnowflake() = Snowflake(decodeLong())

    class User : KSerializer<UserBehavior> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserBehavior", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): UserBehavior = UserBehavior(decoder.decodeSnowflake(), kord)

        override fun serialize(encoder: Encoder, value: UserBehavior) {
            encoder.encodeLong(value.id.value.toLong())
        }
    }
}

typealias UserId = @Serializable(with = KordSerializer.User::class) UserBehavior
typealias MessageId = Snowflake

fun ChannelBehavior.getMessageBehavior(messageId: Snowflake) = MessageBehavior(id, messageId, kord)
suspend fun ChannelBehavior.getMessage(messageId: Snowflake) = supplier.getMessage(id, messageId)
fun UserBehavior.asBehavior() = if (this is MemberBehavior) UserBehavior(id, kord) else this

operator fun Map<UserId, *>.contains(key: UserId) = containsKey(key.asBehavior())
