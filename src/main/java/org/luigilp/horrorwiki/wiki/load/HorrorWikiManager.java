package org.luigilp.horrorwiki.wiki.load;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.loading.FMLPaths;
import org.luigilp.horrorwiki.wiki.data.WikiEntityEntry;
import org.luigilp.horrorwiki.wiki.data.WikiModEntry;
import org.slf4j.Logger;
import vazkii.patchouli.api.PatchouliAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class HorrorWikiManager implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ENTITY_LIST_TYPE = new TypeToken<List<WikiEntityEntry>>() { }.getType();
    private static final Comparator<WikiEntityEntry> ENTITY_SORT = Comparator
            .comparing(WikiEntityEntry::getName, String.CASE_INSENSITIVE_ORDER);
    private static final HorrorWikiManager INSTANCE = new HorrorWikiManager();

    private volatile List<WikiModEntry> loadedMods = List.of();

    public static HorrorWikiManager getInstance() {
        return INSTANCE;
    }

    public List<WikiModEntry> getMods() {
        return loadedMods;
    }

    public void loadFrom(ResourceManager resourceManager) {
        loadedMods = readMods();
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture
                .supplyAsync(this::readMods, backgroundExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync(result -> loadedMods = result, gameExecutor);
    }

    private List<WikiModEntry> readMods() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("horrorwiki");
        Path entitiesFile = configDir.resolve("entities.json");
        ensureConfigFiles(configDir, entitiesFile);
        updateCompatibilityFlags();

        List<WikiEntityEntry> entities = readEntries(entitiesFile, ENTITY_LIST_TYPE, "entities");
        Map<String, List<WikiEntityEntry>> entitiesByModName = groupEntities(entities);

        LOGGER.info("Loaded {} horror wiki entities from config.", entities.size());
        return List.of();
    }

    private void updateCompatibilityFlags() {
        Set<String> installed = getInstalledModFilenamesLower();
        setFlag("horrorwiki_obsessed", containsAny(installed, "the_obsessed", "obsessed"));
        setFlag("horrorwiki_from_the_fog", containsAny(installed, "from_the_fog", "from-the-fog", "fromthefog"));
        setFlag("horrorwiki_the_man_from_the_fog", containsAny(installed, "the_man_from_the_fog", "the-man-from-the-fog", "man_from_the_fog", "manfromthefog"));
        setFlag("horrorwiki_the_knocker", containsAny(installed, "the_knocker", "the-knocker", "knocker"));
        setFlag("horrorwiki_apollyon", containsAny(installed, "apollyon"));
        setFlag("horrorwiki_cave_dweller", containsAnyExcept(installed,
                new String[] {"cave_dweller", "cavenoise", "cavenoisestalkingnightmare", "cave-dweller", "cavedweller"},
                new String[] {"bettercavedweller", "better-cave-dweller", "better_cave_dweller"}));
        setFlag("horrorwiki_midnight_lurker", containsAny(installed, "midnight_lurker", "the_midnight_lurker", "midnightlurker"));
        setFlag("horrorwiki_nightmare_stalker", containsAny(installed, "nightmare_stalker", "nightmare-stalker", "nightmarestalker"));
        setFlag("horrorwiki_siren_head_reborn", containsAny(installed, "sirenheadreborn", "siren_head_reborn", "siren-head-reborn"));
        setFlag("horrorwiki_the_anomaly", containsAny(installed, "theanomaly", "anomaly"));
        setFlag("horrorwiki_better_cave_dweller", containsAny(installed, "better-cave-dweller", "better_cave_dweller", "bettercavedweller", "cavedweller"));
        setFlag("horrorwiki_scp_087_stairwell", containsAny(installed, "thestairwell", "the_stairwell", "stairwell", "scp087"));
        setFlag("horrorwiki_assimilated_dweller", containsAny(installed, "assimilated_dweller", "the-assimilated-dweller", "assimilateddweller", "assimilationuprise", "smoreh"));
        setFlag("horrorwiki_ghost_players", containsAny(installed, "ghost_players", "whos-there", "whos_there", "whosthereghostplayers", "ghostplayers"));
        setFlag("horrorwiki_the_one_who_watches", containsAny(installed, "theonewhowatches", "the_one_who_watches", "onewhowatches", "onewhowatchesv"));
        setFlag("horrorwiki_mickey_mouse_dweller", containsAny(installed, "mickey", "mickey_mouses_nightmare", "mickey-mouse-dweller", "mickeymousesnightmare"));
    }

    private void setFlag(String name, boolean value) {
        PatchouliAPI.get().setConfigFlag(name, value);
    }

    private boolean containsAny(Set<String> installed, String... fragments) {
        for (String file : installed) {
            String normalizedFile = normalizeName(file);
            for (String fragment : fragments) {
                String normalizedFragment = normalizeName(fragment);
                if (file.contains(fragment) || normalizedFile.contains(normalizedFragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAnyExcept(Set<String> installed, String[] fragments, String[] excludedFragments) {
        for (String file : installed) {
            String normalizedFile = normalizeName(file);

            boolean excluded = false;
            for (String excludedFragment : excludedFragments) {
                String normalizedExcluded = normalizeName(excludedFragment);
                if (file.contains(excludedFragment) || normalizedFile.contains(normalizedExcluded)) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) {
                continue;
            }

            for (String fragment : fragments) {
                String normalizedFragment = normalizeName(fragment);
                if (file.contains(fragment) || normalizedFile.contains(normalizedFragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeName(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private void ensureConfigFiles(Path configDir, Path entitiesFile) {
        try {
            Files.createDirectories(configDir);
            Files.createDirectories(configDir.resolve("images"));
            if (!Files.isRegularFile(entitiesFile)) {
                Files.writeString(entitiesFile, GSON.toJson(List.of()));
            }
        } catch (IOException exception) {
            LOGGER.warn("Failed to create default horror wiki config files in {}.", configDir, exception);
        }
    }

    private <T> List<T> readEntries(Path file, Type type, String label) {
        if (!Files.isRegularFile(file)) {
            LOGGER.info("No horror wiki {} config found at {}.", label, file);
            return List.of();
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            JsonElement root = GSON.fromJson(reader, JsonElement.class);
            if (root == null || root.isJsonNull()) {
                return List.of();
            }
            if (root.isJsonArray()) {
                List<T> parsed = GSON.fromJson(root, type);
                return parsed == null ? List.of() : parsed;
            }
            if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                if (object.has(label) && object.get(label).isJsonArray()) {
                    List<T> parsed = GSON.fromJson(object.get(label), type);
                    return parsed == null ? List.of() : parsed;
                }
            }
            LOGGER.warn("Unsupported horror wiki JSON structure in {}.", file);
            return List.of();
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            LOGGER.warn("Failed to read horror wiki config file {}: {}", file, exception.getMessage());
            return List.of();
        }
    }

    private Map<String, List<WikiEntityEntry>> groupEntities(List<WikiEntityEntry> entities) {
        Map<String, List<WikiEntityEntry>> grouped = new HashMap<>();
        for (WikiEntityEntry entity : entities) {
            if (entity == null || !entity.isValid()) {
                continue;
            }
            String key = entity.getModName().toLowerCase(Locale.ROOT).trim();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entity);
        }
        return grouped;
    }

    private Set<String> getInstalledModFilenamesLower() {
        Path modsDir = FMLPaths.GAMEDIR.get().resolve("mods");
        if (!Files.isDirectory(modsDir)) {
            return Set.of();
        }

        Set<String> names = new HashSet<>();
        try (Stream<Path> stream = Files.list(modsDir)) {
            stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .forEach(names::add);
        } catch (IOException exception) {
            LOGGER.warn("Failed to read mod directory {}.", modsDir, exception);
        }
        return names;
    }

}
