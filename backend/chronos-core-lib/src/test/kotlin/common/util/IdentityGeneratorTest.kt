package common.util

import com.chronos.core.common.SourceType
import com.chronos.core.util.id.IdentityGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class IdentityGeneratorTest {

    @Test
    fun `generateEventId should be deterministic`() {
        // Arrange
        val source = SourceType.TELEGRAM
        val externalId = "123456789"

        // Act
        val uuid1 = IdentityGenerator.generateEventId(source, externalId)
        val uuid2 = IdentityGenerator.generateEventId(source, externalId)

        // Assert
        assertEquals(uuid1, uuid2, "UUIDs must be identical for the same input")

        // Let's verify the exact value to ensure algorithm stability over time (Regression Test)
        // If you change the implementation/namespace, this test will fail, alerting you.
        // Note: Value depends on your specific implementation of generateV5
        // assertEquals("expected-uuid-string", uuid1.toString())
    }

    @Test
    fun `generateEventId should handle normalization`() {
        // Arrange
        val idRaw = "  User_DUROV  "
        val idClean = "user_durov"

        // Act
        val uuid1 = IdentityGenerator.generateEventId(SourceType.TELEGRAM, idRaw)
        val uuid2 = IdentityGenerator.generateEventId(SourceType.TELEGRAM, idClean)

        // Assert
        assertEquals(uuid1, uuid2, "UUIDs should be identical despite case and whitespace differences")
    }

    @Test
    fun `generateEventId should differ for different sources`() {
        // Arrange
        val id = "durov"

        // Act
        val telegramUuid = IdentityGenerator.generateEventId(SourceType.TELEGRAM, id)
        val instagramUuid = IdentityGenerator.generateEventId(SourceType.INSTAGRAM, id)

        // Assert
        assertNotEquals(telegramUuid, instagramUuid, "Same ID from different sources must produce different UUIDs")
    }

    @Test
    fun `generateCompositeId should be deterministic`() {
        // Arrange
        val source = SourceType.TELEGRAM
        val channelId = "chronos_news"
        val postId = "42"

        // Act
        val uuid1 = IdentityGenerator.generateCompositeId(source, channelId, postId)
        val uuid2 = IdentityGenerator.generateCompositeId(source, "CHRONOS_NEWS", " 42 ")

        // Assert
        assertEquals(uuid1, uuid2, "Composite IDs must be deterministic and normalized")
    }
}