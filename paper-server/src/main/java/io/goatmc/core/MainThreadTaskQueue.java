package io.goatmc.core;

import net.minecraft.server.MinecraftServer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Потокобезопасная очередь для задач, которые должны быть выполнены
 * исключительно в основном потоке сервера (main server thread).
 * Это наш "почтовый ящик" для отложенных операций.
 */
public class MainThreadTaskQueue {

    // Используем ConcurrentLinkedQueue, так как она отлично подходит для сценария,
    // когда много потоков-производителей (наши регионы) добавляют задачи,
    // а один поток-потребитель (главный поток сервера) их забирает.
    private static final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    /**
     * Добавляет новую задачу в очередь.
     * Этот метод может безопасно вызываться из любого потока.
     * @param task Задача для выполнения.
     */
    public static void queueTask(Runnable task) {
        taskQueue.add(task);
    }

    /**
     * Выполняет все задачи, накопившиеся в очереди.
     * Этот метод должен вызываться ТОЛЬКО из основного потока сервера.
     */
    public static void runTasks() {
        // Мы не можем просто итерироваться по очереди, так как другие потоки
        // могут добавлять в нее новые задачи прямо во время выполнения.
        // Поэтому мы забираем все задачи, которые есть на данный момент.
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                MinecraftServer.LOGGER.error("[GoatMC] Ошибка при выполнении отложенной задачи из главной очереди", e);
            }
        }
    }
}
