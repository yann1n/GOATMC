package io.goatmc.core.config;

import net.minecraft.server.MinecraftServer;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

public class ConfigManager {

    // Путь к нашему шаблону внутри JAR-файла
    private static final String TEMPLATE_PATH = "config-data/configurations/goatmc.yml";
    // Имя файла, который будет создан в корневой папке сервера
    private static final String CONFIG_FILE_NAME = "goatmc.yml";

    public static void load() {
        File configFile = new File(CONFIG_FILE_NAME);

        // 1. Если файла нет, создаем его из ресурсов
        if (!configFile.exists()) {
            try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
                if (in == null) {
                    MinecraftServer.LOGGER.error("[GoatMC] Не удалось найти " + TEMPLATE_PATH + " в ресурсах!");
                    return;
                }
                Files.copy(in, configFile.toPath());
            } catch (Exception e) {
                MinecraftServer.LOGGER.error("[GoatMC] Не удалось создать " + CONFIG_FILE_NAME, e);
                return;
            }
        }

        // 2. Загружаем и парсим YAML
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Map<String, Object> map = yaml.load(fis);
            GoatMCConfig.load(map);
        } catch (Exception e) {
            MinecraftServer.LOGGER.error("[GoatMC] Ошибка при загрузке " + CONFIG_FILE_NAME, e);
        }
    }
}
