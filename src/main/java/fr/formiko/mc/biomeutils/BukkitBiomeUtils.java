package fr.formiko.mc.biomeutils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

public class BukkitBiomeUtils {
    private BukkitBiomeUtils() {}
    /**
     * Get a Set of biomes from a comma separated list of biome or tag
     */
    public static Set<Biome> getBiomesFromList(String input) {
        Set<Biome> biomes = new HashSet<>();
        if (input.isBlank()) {
            return biomes;
        }
        return getBiomesFromList(input.trim().split(","));
    }

    /**
     * Get a Set of biomes from an array of biome or tag
     */
    public static Set<Biome> getBiomesFromList(String[] input) {
        Set<Biome> biomes = new HashSet<>();
        for (String biomeOrTag : input) {
            biomes.addAll(getBiomes(biomeOrTag));
        }
        return biomes;
    }

    /**
     * Get a Set of biomes from a list of biome or tag
     */
    public static Set<Biome> getBiomesFromList(List<String> input) {
        Set<Biome> biomes = new HashSet<>();
        for (String biomeOrTag : input) {
            biomes.addAll(getBiomes(biomeOrTag));
        }
        return biomes;
    }

    /**
     * Get a collection of biomes from a biome or tag
     */
    public static Collection<Biome> getBiomes(String biomeOrTag) {
        biomeOrTag = biomeOrTag.trim();
        try {
            if (biomeOrTag.startsWith("#")) {
                String tag = normalizePattern(biomeOrTag.substring(1));
                return getAllBiomesOfTag(tag);
            } else {
                return List.of(getBiomeRegistryAccess().get(toKey(normalizePattern(biomeOrTag))));
            }
        } catch (Exception e) {
            // ignore
        }
        return List.of();
    }


    /**
     * Normalize biome pattern
     */
    public static String normalizePattern(String pattern) {
        String normalized = pattern.toLowerCase(Locale.ROOT);
        return normalized.contains(":") ? normalized : "minecraft:" + normalized;
    }

    /**
     * Return the biome registry
     */
    public static Registry<Biome> getBiomeRegistryAccess() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
    }

    /**
     * Return all biomes of a tag
     */
    public static Collection<Biome> getAllBiomesOfTag(String tag) {
        return getBiomeRegistryAccess().getTagValues(TagKey.create(RegistryKey.BIOME, tag));
    }

    /**
     * Convert a string to a key
     */
    public static NamespacedKey toKey(String key) {
        return NamespacedKey.fromString(key);
    }
}

