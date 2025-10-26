package io.goatmc.core;

import net.minecraft.world.entity.Entity; // <--- НУЖНЫЙ ИМПОРТ
import net.minecraft.world.phys.Vec3;

public class AsyncTickResult {

    public final Entity entity; // <--- ССЫЛКА НА СУЩНОСТЬ
    public final Vec3 newPosition;
    public final Vec3 newDeltaMovement;
    public final Vec3 originalMovement; // <--- ОРИГИНАЛЬНОЕ ДВИЖЕНИЕ (для событий Bukkit)

    // В будущем здесь появятся другие поля:
    // public List<Runnable> worldChanges; // Для запланированных изменений блоков

    public AsyncTickResult(Entity entity, Vec3 newPosition, Vec3 newDeltaMovement, Vec3 originalMovement) {
        this.entity = entity;
        this.newPosition = newPosition;
        this.newDeltaMovement = newDeltaMovement;
        this.originalMovement = originalMovement;
    }
}
