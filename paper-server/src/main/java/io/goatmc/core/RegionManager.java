package io.goatmc.core;

import ca.spottedleaf.moonrise.common.util.TickThread;
import io.goatmc.core.config.GoatMCConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.entity.EntityTickList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class RegionManager {

    public static final int REGION_WIDTH_IN_CHUNKS = GoatMCConfig.regionWidth;
    public static final int REGION_SHIFT = GoatMCConfig.regionShift;

    private final ServerLevel world;
    private final ConcurrentHashMap<Long, RegionTickThread> regionThreads = new ConcurrentHashMap<>();

    private final List<AsyncTickResult> movementResults = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService executor;

    public RegionManager(ServerLevel world) {
        this.world = world;
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(availableProcessors);
        MinecraftServer.LOGGER.info("[GoatMC] Пул потоков для мира '{}' создан с {} потоками.", world.serverLevelData.getLevelName(), availableProcessors);
    }

    public void tickEntities(EntityTickList entityTickList, Consumer<Entity> entityTicker) {
        // --- ФАЗА 0: ПОДГОТОВКА И РАСПРЕДЕЛЕНИЕ ---
        this.movementResults.clear();

        entityTickList.forEach(entity -> {
            if (entity.isRemoved()) return;
            long regionKey = getRegionKey(entity.chunkPosition().x, entity.chunkPosition().z);
            RegionTickThread thread = getOrCreateRegionThread(regionKey);
            thread.addEntityToTick(entity);
        });

        // --- ФАЗА 1: ПАРАЛЛЕЛЬНОЕ ВЫЧИСЛЕНИЕ (МОЗГ) ---
        if (!this.regionThreads.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(this.regionThreads.size());

            for (RegionTickThread thread : this.regionThreads.values()) {
                this.executor.submit(() -> {
                    TickThread.setCurrentTickThread(this.world);
                    try {
                        // Вызываем асинхронный расчет движения и добавляем результаты в общий список.
                        List<AsyncTickResult> results = thread.collectAsyncMovementResults(MoverType.SELF);
                        this.movementResults.addAll(results);

                    } catch (Exception e) {
                        MinecraftServer.LOGGER.error("[GoatMC] Критическая ошибка в АСИНХРОННОМ расчете региона " + thread.getRegionKey(), e);
                    } finally {
                        TickThread.setCurrentTickThread(null);
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                MinecraftServer.LOGGER.error("[GoatMC] Основной поток был прерван.", e);
                Thread.currentThread().interrupt();
            }
        }

        // --- ФАЗА 2: СИНХРОННОЕ ПРИМЕНЕНИЕ (МЫШЦЫ) ---

        // ВНИМАНИЕ: На этом этапе мы должны выполнять остальную синхронную логику тика.
        // Пока что мы просто применяем движение (но логика событий/AI/деспауна отсутствует).

        for (AsyncTickResult result : this.movementResults) {
            result.entity.applyAsyncTickResult(result);
        }

        // --- ФАЗА 3: Очистка ---
        for (RegionTickThread thread : this.regionThreads.values()) {
            thread.clearEntities();
        }
    }

    public void shutdown() {
        MinecraftServer.LOGGER.info("[GoatMC] Завершение работы пула потоков для мира '{}'...", world.serverLevelData.getLevelName());
        this.executor.shutdown();
    }

    public RegionTickThread getOrCreateRegionThread(long regionKey) {
        return this.regionThreads.computeIfAbsent(regionKey, key -> new RegionTickThread(this, this.world, key));
    }

    public static long getRegionKey(int chunkX, int chunkZ) {
        int regionX = chunkX >> REGION_SHIFT;
        int regionZ = chunkZ >> REGION_SHIFT;
        return (long)regionX & 0xFFFFFFFFL | ((long)regionZ & 0xFFFFFFFFL) << 32;
    }
}
