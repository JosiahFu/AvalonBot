package bot.avalon.data

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
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

    class Member : KSerializer<MemberBehavior> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MemberBehavior", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MemberBehavior {
            val (guildId, id) = decoder.decodeString().split(":").map { Snowflake(it.toLong()) }
            return MemberBehavior(guildId, id, kord)
        }

        override fun serialize(encoder: Encoder, value: MemberBehavior) {
            encoder.encodeString("${value.guildId.value.toLong()}:${value.id.value.toLong()}")
        }
    }

    class Message : KSerializer<MessageBehavior> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MessageBehavior", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MessageBehavior {
            val (channelId, id) = decoder.decodeString().split(":").map { Snowflake(it.toLong()) }
            return MessageBehavior(channelId, id, kord)
        }

        override fun serialize(encoder: Encoder, value: MessageBehavior) {
            encoder.encodeString("${value.channelId.value.toLong()}:${value.id.value.toLong()}")
        }
    }
}

typealias UserId = @Serializable(with = KordSerializer.Member::class) MemberBehavior
typealias MessageId = @Serializable(with = KordSerializer.Message::class) MessageBehavior

fun ChannelBehavior.getMessageBehavior(messageId: Snowflake) = MessageBehavior(id, messageId, kord)
suspend fun ChannelBehavior.getMessage(messageId: Snowflake) = supplier.getMessage(id, messageId)
fun GuildBehavior.getMemberBehavior(id: Snowflake) = MemberBehavior(this.id, id, kord)
fun GuildBehavior.getMemberBehavior(user: UserBehavior) = MemberBehavior(id, user.id, kord)
