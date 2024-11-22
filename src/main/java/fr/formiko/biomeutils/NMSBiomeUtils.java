package fr.formiko.biomeutils;

import java.util.Map;
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

public class NMSBiomeUtils {
    // Key, Biome
    private static Map<String, Biome> allBiomes;

    @Nonnull
    public static Registry<Biome> getBiomeRegistry() {
        return ((CraftServer) Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registries.BIOME);
    }

    /** Return the biome from it's key */
    @Nullable
    public static Biome getBiome(String key) { return getBiomeRegistry().get(resourceLocation(key)); }
    /**
     * Return the real biome at the given location. (Not the noise biome)
     */
    @Nullable
    public static Biome getBiome(Location location) {
        return getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }

    /**
     * Return the real biome at the given location. (Not the noise biome)
     */
    @Nullable
    public static Biome getBiome(int x, int y, int z, World bukkitWorld) {
        if (bukkitWorld == null)
            return null;
        ServerLevel nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        return nmsWorld.getNoiseBiome(x >> 2, y >> 2, z >> 2).value();
    }

    @Nullable
    public static ResourceLocation getBiomeKey(Location location) {
        Biome biome = getBiome(location);
        if (biome == null)
            return null;
        return getBiomeRegistry().getKey(biome);
    }
    @Nullable
    public static ResourceLocation getBiomeKey(int x, int y, int z, World bukkitWorld) {
        Biome biome = getBiome(x, y, z, bukkitWorld);
        if (biome == null)
            return null;
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
    public static ResourceLocation resourceLocation(@Nonnull String name) {
        String[] t = name.split(":");
        return ResourceLocation.fromNamespaceAndPath(t[0], t[1]);
    }

    // Convert between Minecraft and Bukkit biomes
    // minecraft to bukkit don't work with custom biomes.
    public static org.bukkit.block.Biome minecraftToBukkit(Biome minecraft) { return CraftBiome.minecraftToBukkit(minecraft); }
    public static Biome bukkitToMinecraft(org.bukkit.block.Biome bukkit) { return CraftBiome.bukkitToMinecraft(bukkit); }
    public static org.bukkit.block.Biome minecraftHolderToBukkit(Holder<Biome> minecraft) {
        return CraftBiome.minecraftToBukkit(minecraft.value());
    }
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
                .anyMatch(pair -> pair.getFirst().location().toString().equals(tagString) && pair.getSecond().contains(biomeHolder));
    }
    @Nullable
    public static ResourceKey<Biome> getBiomeResourceKey(String key) {
        Biome biome = getBiome(key);
        if (biome == null)
            return null;
        return getBiomeRegistry().getResourceKey(biome).get();
    }
    @Nullable
    public static Holder<Biome> getBiomeHolder(String key) { return getBiomeRegistry().getHolder(resourceLocation(key)).orElse(null); }

    @Nonnull
    public static Map<String, Biome> getAllBiomes() {
        if (allBiomes == null) {
            Registry<Biome> biomeRegistry = getBiomeRegistry();
            allBiomes = biomeRegistry.stream().collect(Collectors.toMap(biome -> biomeRegistry.getKey(biome).toString(), biome -> biome));
        }
        return allBiomes;
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
    public static void setCustomBiome(String newBiomeName, Location location) { setCustomBiome(newBiomeName, location, true); }
    /** This function assumes the world is "world" */
    public static void setCustomBiome(String newBiomeName, int x, int y, int z, boolean refresh) {
        setCustomBiome(newBiomeName, new Location(Bukkit.getWorld("world"), x, y, z), refresh);
    }
    public static void setCustomBiome(String newBiomeName, int x, int y, int z) { setCustomBiome(newBiomeName, x, y, z, true); }

    private static void setCustomBiome(int x, int y, int z, Level w, Holder<Biome> bb) {
        BlockPos pos = new BlockPos(x, 0, z);
        if (w.isLoaded(pos)) {
            ChunkAccess chunk = w.getChunk(pos);
            if (chunk != null) {
                chunk.setBiome(x >> 2, y >> 2, z >> 2, bb);
            }
        }
    }
}
