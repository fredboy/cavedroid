package ru.fredboy.cavedroid.data.save.repository

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import ru.fredboy.cavedroid.domain.save.persist
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChunkPersistenceCoordinatorTest {
    @TempDir
    lateinit var directory: File

    @Test
    @Timeout(10)
    fun `serialization runs outside lock and delayed write cannot replace newer deletion`() {
        val coordinator = ChunkPersistenceCoordinator()
        val target = File(directory, "chunk")
        val encoding = CountDownLatch(1)
        val release = CountDownLatch(1)
        val old = coordinator.prepare(target.path, {
            encoding.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            byteArrayOf(1)
        }) { target.writeBytes(requireNotNull(it)) }
        Executors.newSingleThreadExecutor().use { executor ->
            val writing = executor.submit { old.write() }
            try {
                assertTrue(encoding.await(5, TimeUnit.SECONDS))
                coordinator.prepare(target.path, { null }) { target.delete() }.persist()
                release.countDown()
                writing.get(5, TimeUnit.SECONDS)
                assertFalse(target.exists())
            } finally {
                release.countDown()
                old.finish()
                old.finish()
            }
        }
        coordinator.prepare(target.path, { byteArrayOf(3) }) { target.writeBytes(requireNotNull(it)) }.persist()
        assertArrayEquals(byteArrayOf(3), target.readBytes())
    }

    @Test
    fun `failed writes do not advance persisted revision`() {
        val coordinator = ChunkPersistenceCoordinator()
        val target = File(directory, "chunk")
        val old = coordinator.prepare(target.path, { byteArrayOf(1) }) { target.writeBytes(requireNotNull(it)) }
        val failed = coordinator.prepare(target.path, { byteArrayOf(2) }) { error("Unavailable") }
        assertThrows<IllegalStateException> { failed.persist() }
        old.persist()
        assertArrayEquals(byteArrayOf(1), target.readBytes())
    }

    @Test
    @Timeout(10)
    fun `matching reader waits for the entire write`() {
        val coordinator = ChunkPersistenceCoordinator()
        val target = File(directory, "chunk")
        val partial = CountDownLatch(1)
        val release = CountDownLatch(1)
        val reading = CountDownLatch(1)
        val prepared = coordinator.prepare(target.path, { byteArrayOf(1, 2) }) { bytes ->
            target.writeBytes(byteArrayOf(1))
            partial.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            target.writeBytes(requireNotNull(bytes))
        }
        Executors.newFixedThreadPool(2).use { executor ->
            val write = executor.submit { prepared.write() }
            try {
                assertTrue(partial.await(5, TimeUnit.SECONDS))
                val read = executor.submit<ByteArray> {
                    reading.countDown()
                    coordinator.read(target.path) { target.readBytes() }
                }
                assertTrue(reading.await(5, TimeUnit.SECONDS))
                assertFalse(read.isDone)
                release.countDown()
                write.get(5, TimeUnit.SECONDS)
                assertArrayEquals(byteArrayOf(1, 2), read.get(5, TimeUnit.SECONDS))
            } finally {
                release.countDown()
                prepared.finish()
            }
        }
    }
}
