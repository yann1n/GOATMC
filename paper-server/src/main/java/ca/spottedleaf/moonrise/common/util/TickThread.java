package ca.spottedleaf.moonrise.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel; // <--- ДОБАВЛЕН ИМПОРТ
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class TickThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickThread.class);

    // ========================================================
    // НАЧАЛО НАШИХ ИЗМЕНЕНИЙ
    // ========================================================

    /**
     * НОВОЕ ПОЛЕ: ThreadLocal - это специальная переменная. У каждого потока будет
     * своя собственная, независимая копия этой переменной.
     * Мы будем использовать ее, чтобы "помечать" рабочие потоки как временные "главные" потоки.
     */
    private static final ThreadLocal<Level> CURRENT_TICK_WORLD = new ThreadLocal<>();

    /**
     * НОВЫЙ МЕТОД (тот самый, которого не хватало):
     * Этот метод позволяет любому потоку "зарегистрироваться" как главный поток для конкретного мира.
     * @param world Мир, который этот поток будет обрабатывать, или null, чтобы "разрегистрироваться".
     */
    public static void setCurrentTickThread(final Level world) {
        if (world == null) {
            CURRENT_TICK_WORLD.remove();
        } else {
            CURRENT_TICK_WORLD.set(world);
        }
    }

    /**
     * ИЗМЕНЕННЫЙ МЕТОД: Теперь он проверяет не только тип потока, но и нашу новую ThreadLocal переменную.
     * @return true, если это основной поток сервера ИЛИ если рабочий поток был временно "помечен" как главный.
     */
    public static boolean isTickThread() {
        // Старая проверка (является ли поток экземпляром TickThread) + новая проверка (установлена ли наша переменная).
        return Thread.currentThread() instanceof TickThread || CURRENT_TICK_WORLD.get() != null;
    }

    // ========================================================
    // КОНЕЦ НАШИХ ИЗМЕНЕНИЙ
    // ========================================================


    private static String getThreadContext() {
        return "thread=" + Thread.currentThread().getName();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void ensureTickThread(final String reason) {
        if (!isTickThread()) {
            LOGGER.error("Thread failed main thread check: " + reason + ", context=" + getThreadContext(), new Throwable());
            throw new IllegalStateException(reason);
        }
    }

    public static void ensureTickThread(final Level world, final BlockPos pos, final String reason) {
        if (!isTickThreadFor(world, pos)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", block_pos=" + pos;
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public static void ensureTickThread(final Level world, final BlockPos pos, final int blockRadius, final String reason) {
        if (!isTickThreadFor(world, pos, blockRadius)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", block_pos=" + pos + ", block_radius=" + blockRadius;
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public static void ensureTickThread(final Level world, final ChunkPos pos, final String reason) {
        if (!isTickThreadFor(world, pos)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", chunk_pos=" + pos;
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public static void ensureTickThread(final Level world, final int chunkX, final int chunkZ, final String reason) {
        if (!isTickThreadFor(world, chunkX, chunkZ)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", chunk_pos=" + new ChunkPos(chunkX, chunkZ);
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public static void ensureTickThread(final Entity entity, final String reason) {
        if (!isTickThreadFor(entity)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", entity=" + EntityUtil.dumpEntity(entity);
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public static void ensureTickThread(final Level world, final AABB aabb, final String reason) {
        if (!isTickThreadFor(world, aabb)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", aabb=" + aabb;
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public static void ensureTickThread(final Level world, final double blockX, final double blockZ, final String reason) {
        if (!isTickThreadFor(world, blockX, blockZ)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", block_pos=" + new Vec3(blockX, 0.0, blockZ);
            LOGGER.error(ex, new Throwable());
            throw new IllegalStateException(ex);
        }
    }

    public final int id;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    public TickThread(final String name) {
        this(null, name);
    }

    public TickThread(final Runnable run, final String name) {
        this(null, run, name);
    }

    public TickThread(final ThreadGroup group, final Runnable run, final String name) {
        this(group, run, name, ID_GENERATOR.incrementAndGet());
    }

    private TickThread(final ThreadGroup group, final Runnable run, final String name, final int id) {
        super(group, run, name);
        this.id = id;
    }

    public static TickThread getCurrentTickThread() {
        Thread current = Thread.currentThread();
        if (current instanceof TickThread) {
            return (TickThread) current;
        }
        return null;
    }

    public static boolean isShutdownThread() {
        return false;
    }

    public static boolean isTickThreadFor(final Level world, final BlockPos pos) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final BlockPos pos, final int blockRadius) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final ChunkPos pos) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final Vec3 pos) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final int chunkX, final int chunkZ) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final AABB aabb) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final double blockX, final double blockZ) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final Vec3 position, final Vec3 deltaMovement, final int buffer) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final int fromChunkX, final int fromChunkZ, final int toChunkX, final int toChunkZ) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Level world, final int chunkX, final int chunkZ, final int radius) {
        return isTickThread();
    }

    public static boolean isTickThreadFor(final Entity entity) {
        return isTickThread();
    }
}
