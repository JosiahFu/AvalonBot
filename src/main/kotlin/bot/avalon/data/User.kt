package bot.avalon.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class User(val id: String)

class UserSerializer : KSerializer<User> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("User", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): User {
        return User(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: User) {
        encoder.encodeString(value.id)
    }
}

typealias SerializableUser = @Serializable(with = UserSerializer::class) User
