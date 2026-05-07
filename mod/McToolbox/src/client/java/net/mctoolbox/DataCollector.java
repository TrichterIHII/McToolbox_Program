package net.mctoolbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mctoolbox.mixin.client.PieChartAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerTiming;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DataCollector {

    public static @Nullable String collect(@NotNull MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return null;

        JsonObject root = new JsonObject();

        // Tick
        root.addProperty("tick", client.world.getTime());

        // Player
        JsonObject playerObj = new JsonObject();
        playerObj.addProperty("x", player.getX());
        playerObj.addProperty("y", player.getY());
        playerObj.addProperty("z", player.getZ());
        playerObj.addProperty("yaw", player.getYaw());
        playerObj.addProperty("pitch", player.getPitch());
        playerObj.addProperty("health", player.getHealth());
        playerObj.addProperty("hunger", player.getHungerManager().getFoodLevel());
        playerObj.addProperty("speed", player.getVelocity().length());
        root.add("player", playerObj);

        // World
        JsonObject worldObj = new JsonObject();
        RegistryEntry<Biome> biomeEntry = client.world.getBiome(player.getBlockPos());
        worldObj.addProperty("biome", biomeEntry.getKey()
                .map(k -> k.getValue().toString())
                .orElse("unknown"));
        worldObj.addProperty("light", client.world.getLightLevel(player.getBlockPos()));
        worldObj.addProperty("time", client.world.getTimeOfDay());
        worldObj.addProperty("dimension", client.world.getRegistryKey().getValue().toString());
        root.add("world", worldObj);

        // Nearby players – only to client sent players
        JsonArray playersArr = new JsonArray();
        for (AbstractClientPlayerEntity p : client.world.getPlayers()) {
            if (p == player) continue;
            JsonObject pObj = new JsonObject();
            pObj.addProperty("name", p.getName().getString());
            pObj.addProperty("x", p.getX());
            pObj.addProperty("y", p.getY());
            pObj.addProperty("z", p.getZ());
            pObj.addProperty("distance", player.distanceTo(p));
            playersArr.add(pObj);
        }
        root.add("nearby_players", playersArr);

        // Inventory – own inventory only
        JsonArray inventoryArr = new JsonArray();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            JsonObject item = new JsonObject();
            item.addProperty("slot", i);
            item.addProperty("item", stack.getItem().toString());
            item.addProperty("count", stack.getCount());
            inventoryArr.add(item);
        }
        root.add("inventory", inventoryArr);

        // PieChart
        JsonArray pieChartBlockEntitiesArr = new JsonArray();
        JsonArray pieChartEntitiesArr = new JsonArray();
        //noinspection ReferenceToMixin
        ProfileResult result = ((PieChartAccessor) MinecraftClient.getInstance().getDebugHud().getPieChart()).getProfileResult();
        if (result != null) {
            List<ProfilerTiming> timings_blockEntities = result.getTimings("root\u001etick\u001elevel\u001eblockEntities");
            List<ProfilerTiming> timings_entities = result.getTimings("root\u001etick\u001elevel\u001eentities");

            for (ProfilerTiming timing : timings_blockEntities) {
                JsonObject t = new JsonObject(); {
                    t.addProperty("name", timing.name);
                    t.addProperty("total", timing.totalUsagePercentage);
                    t.addProperty("parent", timing.parentSectionUsagePercentage);
                    t.addProperty("count", timing.visitCount);
                }
                pieChartBlockEntitiesArr.add(t);
            }

            for (ProfilerTiming timing : timings_entities) {
                JsonObject t = new JsonObject();
                {
                    t.addProperty("name", timing.name);
                    t.addProperty("total", timing.totalUsagePercentage);
                    t.addProperty("parent", timing.parentSectionUsagePercentage);
                    t.addProperty("count", timing.visitCount);
                }
                pieChartEntitiesArr.add(t);
            }
        }
        root.add("piechart_blockentities", pieChartBlockEntitiesArr);
        root.add("piechart_entities", pieChartEntitiesArr);

        return root.toString();
    }
}
