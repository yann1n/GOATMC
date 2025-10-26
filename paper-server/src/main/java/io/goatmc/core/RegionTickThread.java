package io.goatmc.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RegionTickThread {

    private final RegionManager manager;
    private final ServerLevel world;
    private final long regionKey;
    private final List<Entity> entitiesToTick = new ArrayList<>();

    public RegionTickThread(RegionManager manager, ServerLevel world, long regionKey) {
        this.manager = manager;
        this.world = world;
        this.regionKey = regionKey;
    }

    @Deprecated
    public void tick(Consumer<Entity> entityTicker) {
        for (Entity entity : this.entitiesToTick) {
            entityTicker.accept(entity);
        }
    }

    public List<AsyncTickResult> collectAsyncMovementResults(MoverType type) {
        List<AsyncTickResult> results = new ArrayList<>(this.entitiesToTick.size());

        for (Entity entity : this.entitiesToTick) {
            // Для движения мы всегда используем текущий deltaMovement в качестве входного вектора.
            AsyncTickResult result = entity.calculateAsyncMovement(type, entity.getDeltaMovement());
            results.add(result);
        }
        return results;
    }

    public void addEntityToTick(Entity entity) {
        this.entitiesToTick.add(entity);
    }

    public void clearEntities() {
        this.entitiesToTick.clear();
    }

    public long getRegionKey() {
        return regionKey;
    }
}
