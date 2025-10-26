package io.goatmc.core.config;

import net.minecraft.server.MinecraftServer;
import java.util.Map;

/**
 * Класс для хранения всех настроек из goatmc.yml.
 * Значения загружаются один раз при старте сервера.
 */
public class GoatMCConfig {

    // --- Регионы ---
    public static int regionWidth;
    public static int regionShift;

    // --- Логирование ---
    public static boolean logConfigLoading;

    /**
     * Загружает значения из распарсенной карты YAML.
     * @param map Карта, полученная из SnakeYAML.
     */
    @SuppressWarnings("unchecked") // Безопасно, так как мы проверяем тип
    public static void load(Map<String, Object> map) {
        Map<String, Object> settings = getMap(map, "settings");

        // --- Регионы ---
        Map<String, Object> regions = getMap(settings, "regions");
        regionWidth = getInt(regions, "width-in-chunks", 32);
        // Вычисляем битовый сдвиг на основе ширины (например, для 32 это будет 5, так как 2^5 = 32)
        regionShift = 31 - Integer.numberOfLeadingZeros(regionWidth);

        // --- Логирование ---
        Map<String, Object> logging = getMap(settings, "logging");
        logConfigLoading = getBoolean(logging, "log-config-loading", true);

        if (logConfigLoading) {
            MinecraftServer.LOGGER.info("[GoatMC] Конфигурация goatmc.yml успешно загружена.");
        }
    }

    // --- Вспомогательные методы для безопасного получения значений ---

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Map ? (Map<String, Object>) value : new java.util.HashMap<>();
    }

    private static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
}
