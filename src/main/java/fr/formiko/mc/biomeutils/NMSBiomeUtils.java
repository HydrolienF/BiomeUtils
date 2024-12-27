package fr.formiko.mc.biomeutils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBiome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Tool class to manipulate nms biomes.
 * Unless the function name specifies it the objects are Minecraft objects and not Bukkit objects.
 */
public class NMSBiomeUtils {
    // Key, Biome
    private static Map<String, Biome> allBiomes;

    @Nonnull
    public static Registry<Biome> getBiomeRegistry() {
        return ((CraftServer) Bukkit.getServer()).getServer().registryAccess().lookupOrThrow(Registries.BIOME);
    }

    /** Return the biome from it's key */
    @Nullable
    public static Biome getBiome(@Nonnull String key) {
        Holder.Reference<Biome> ref = getBiomeRegistry().get(resourceLocation(key)).orElse(null);
        if (ref == null)
            return null;
        return ref.value();
    }
    /**
     * Return the real biome at the given location. (Not the noise biome)
     */
    @Nonnull
    public static Biome getBiome(@Nonnull Location location) {
        return getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }
    @Nonnull
    public static Holder<Biome> getBiomeHolder(@Nonnull Location location) {
        return getBiomeHolder(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }

    /**
     * Return the real biome at the given location. (Not the noise biome)
     */
    @Nonnull
    public static Biome getBiome(int x, int y, int z, World bukkitWorld) {
        Holder<Biome> biomeHolder = getBiomeHolder(x, y, z, bukkitWorld);
        return biomeHolder == null ? null : biomeHolder.value();
    }
    @Nonnull
    public static Holder<Biome> getBiomeHolder(int x, int y, int z, @Nonnull World bukkitWorld) {
        ServerLevel nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        return nmsWorld.getNoiseBiome(x >> 2, y >> 2, z >> 2);
    }

    @Nullable
    public static ResourceLocation getBiomeKey(@Nonnull Location location) {
        Biome biome = getBiome(location);
        return getBiomeRegistry().getKey(biome);
    }
    @Nullable
    public static ResourceLocation getBiomeKey(int x, int y, int z, @Nonnull World bukkitWorld) {
        Biome biome = getBiome(x, y, z, bukkitWorld);
        return getBiomeRegistry().getKey(biome);
    }
    @Nullable
    public static String getBiomeKeyString(Location location) {
        ResourceLocation key = getBiomeKey(location);
        return key == null ? null : key.toString();
    }
    @Nullable
    public static String getBiomeKeyString(int x, int y, int z, World bukkitWorld) {
        ResourceLocation key = getBiomeKey(x, y, z, bukkitWorld);
        return key == null ? null : key.toString();
    }
    @Nonnull
    public static ResourceLocation resourceLocation(@Nonnull String name) {
        String[] t = name.split(":");
        return ResourceLocation.fromNamespaceAndPath(t[0], t[1]);
    }

    // Convert between Minecraft and Bukkit biomes
    // minecraft to bukkit don't work with custom biomes.
    public static org.bukkit.block.Biome minecraftToBukkit(Biome minecraft) { return CraftBiome.minecraftToBukkit(minecraft); }
    public static Biome bukkitToMinecraft(org.bukkit.block.Biome bukkit) { return CraftBiome.bukkitToMinecraft(bukkit); }
    public static org.bukkit.block.Biome minecraftHolderToBukkit(Holder<Biome> minecraft) { return minecraftToBukkit(minecraft.value()); }
    public static Holder<Biome> bukkitToMinecraftHolder(org.bukkit.block.Biome bukkit) {
        return CraftBiome.bukkitToMinecraftHolder(bukkit);
    }

    /**
     * Return true if the biome match the tag.
     * It will always be false if an argument is null or if the biome or tag doesn't exist.
     */
    public static boolean matchTag(String biomeString, String tagString) {
        if (biomeString == null || tagString == null)
            return false;
        Holder<Biome> biomeHolder = getBiomeHolder(biomeString);
        if (biomeHolder == null)
            return false;

        return getBiomeRegistry().getTags()
                .anyMatch(pair -> pair.key().location().toString().equals(tagString) && pair.contains(biomeHolder));
    }
    /**
     * Return true if the biome match the tag.
     * It will always be false if an argument is null or if the biome or tag doesn't exist.
     */
    public static boolean matchTag(Location location, String tagString) { return matchTag(getBiomeKeyString(location), tagString); }
    /**
     * Return true if the biome match the tag.
     * It will always be false if an argument is null or if the biome or tag doesn't exist.
     */
    public static boolean matchTag(org.bukkit.block.Block block, String tagString) { return matchTag(block.getLocation(), tagString); }


    @Nullable
    public static ResourceKey<Biome> getBiomeResourceKey(@Nonnull String key) {
        Biome biome = getBiome(key);
        if (biome == null)
            return null;
        return getBiomeRegistry().getResourceKey(biome).orElse(null);
    }
    @Nullable
    public static Holder<Biome> getBiomeHolder(String key) { return getBiomeRegistry().get(resourceLocation(key)).orElse(null); }

    @Nonnull
    public static Map<String, Biome> getAllBiomes() {
        if (allBiomes == null) {
            Registry<Biome> biomeRegistry = getBiomeRegistry();
            allBiomes = biomeRegistry.stream().collect(Collectors.toMap(biome -> biomeRegistry.getKey(biome).toString(), biome -> biome));
        }
        return allBiomes;
    }
    @Nonnull
    public static Set<String> getAllBiomesKeyStringMatchingTag(String tag) {
        return getAllBiomes().keySet().stream().filter(biomeKey -> matchTag(biomeKey, tag)).collect(Collectors.toSet());
    }

    /**
     * Set a full chunk to a custom biome
     *
     * @param newBiomeName the name of the custom biome to set (such as tardis:skaro_lakes)
     * @param chunk        the chunk to set the biome for
     * @param refresh      whether to refresh the chunk after setting the biome
     */
    public static void setCustomBiome(String newBiomeName, Chunk chunk, boolean refresh) {
        Holder<Biome> biomeHolder = NMSBiomeUtils.getBiomeHolder(newBiomeName);
        Level w = ((CraftWorld) chunk.getWorld()).getHandle();
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = 0; y <= chunk.getWorld().getMaxHeight(); y++) {
                    setCustomBiome(chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z, w, biomeHolder);
                }
            }
        }
        if (refresh) {
            chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        }
    }
    /**
     * Set a full chunk to a custom biome.
     * It refresh the chunk after setting the biome.
     *
     * @param newBiomeName the name of the custom biome to set (such as tardis:skaro_lakes)
     * @param chunk        the chunk to set the biome for
     */
    public static void setCustomBiome(String newBiomeName, Chunk chunk) { setCustomBiome(newBiomeName, chunk, true); }

    /**
     * Set a location to a custom biome
     *
     * @param newBiomeName the name of the custom biome to set (such as tardis:skaro_lakes)
     * @param location     the location to set the biome for
     * @param refresh      whether to refresh the chunk after setting the biome
     */
    public static void setCustomBiome(String newBiomeName, Location location, boolean refresh) {
        setCustomBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), ((CraftWorld) location.getWorld()).getHandle(),
                NMSBiomeUtils.getBiomeHolder(newBiomeName));
        // Holder.direct(base));
        if (refresh) {
            location.getWorld().refreshChunk(location.getChunk().getX(), location.getChunk().getZ());
        }
    }
    /**
     * Set a location to a custom biome
     * It refresh the chunk after setting the biome.
     *
     * @param newBiomeName the name of the custom biome to set (such as tardis:skaro_lakes)
     * @param location     the location to set the biome for
     */
    public static void setCustomBiome(String newBiomeName, Location location) { setCustomBiome(newBiomeName, location, true); }
    /**
     * Set a location to a custom biome
     * 
     * @param newBiomeName the name of the custom biome to set
     * @param x            the x coordinate of the location to set the biome for
     * @param y            the y coordinate of the location to set the biome for
     * @param z            the z coordinate of the location to set the biome for
     * @param world        the world of the location to set the biome for
     * @param refresh      whether to refresh the chunk after setting the biome
     */
    public static void setCustomBiome(String newBiomeName, int x, int y, int z, World world, boolean refresh) {
        setCustomBiome(newBiomeName, new Location(world, x, y, z), refresh);
    }
    /**
     * Set a location to a custom biome
     * It refresh the chunk after setting the biome.
     * 
     * @param newBiomeName the name of the custom biome to set
     * @param x            the x coordinate of the location to set the biome for
     * @param y            the y coordinate of the location to set the biome for
     * @param z            the z coordinate of the location to set the biome for
     * @param world        the world of the location to set the biome for
     */
    public static void setCustomBiome(String newBiomeName, int x, int y, int z, World world) {
        setCustomBiome(newBiomeName, x, y, z, world, true);
    }

    private static void setCustomBiome(int x, int y, int z, Level w, Holder<Biome> bb) {
        BlockPos pos = new BlockPos(x, 0, z);
        if (w.isLoaded(pos)) {
            ChunkAccess chunk = w.getChunk(pos);
            if (chunk != null) {
                chunk.setBiome(x >> 2, y >> 2, z >> 2, bb);
            }
        }
    }

    public static @Nullable org.bukkit.block.Biome getBukkitBiome(String name) {
        try {
            return org.bukkit.block.Biome.valueOf(name.split(":")[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Normalize the biome name to the format minecraft:biome_name
     * Any bukkit biome name will be converted to minecraft biome name.
     * e.g. "minecraft:plains" or "PLAINS" will be returned as "minecraft:plains"
     * 
     * @param name the name of the biome
     * @return a normalized biome name
     */
    public static @Nonnull String normalizeBiomeName(@Nonnull String name) {
        name = name.toLowerCase();
        if (!name.contains(":")) {
            name = "minecraft:" + name;
        }
        return name;
    }
    /**
     * Normalize the biome name to the format minecraft:biome_name
     * Any bukkit biome name will be converted to minecraft biome name.
     * e.g. "minecraft:plains" or "PLAINS" will be returned as "minecraft:plains"
     * 
     * @param nameList a list of biome names
     * @return a list of normalized biome names
     */
    public static @Nonnull List<String> normalizeBiomeNameList(@Nonnull List<String> nameList) {
        return nameList.stream().map(NMSBiomeUtils::normalizeBiomeName).toList();
    }
}
