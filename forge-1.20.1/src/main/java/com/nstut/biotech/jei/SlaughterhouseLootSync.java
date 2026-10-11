package com.nstut.biotech.jei;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nstut.biotech.items.MobItem;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.network.LootPreviewPacket;
import com.nstut.biotech.network.PacketRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.event.OnDatapackSyncEvent;

/** Sample active server loot tables with private deterministic seeds; never touch transactions. */
public final class SlaughterhouseLootSync {
    private SlaughterhouseLootSync() {}
    public static void sync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        var packet = new LootPreviewPacket(catalog(server.overworld()).toString());
        if (event.getPlayer() != null) PacketRegistries.sendToPlayer(event.getPlayer(), packet);
        else for (var player : event.getPlayerList().getPlayers()) PacketRegistries.sendToPlayer(player, packet);
    }
    public static JsonObject catalog(ServerLevel level) {
        JsonObject result = new JsonObject();
        for (var recipe : level.getServer().getRecipeManager().getRecipes()) {

            if (recipe instanceof com.nstut.biotech.recipes.GreenhouseRecipe greenhouse && greenhouse.usesBlockLoot()) {
                try {
                    var harvest = com.nstut.biotech.recipes.GreenhouseHarvestPreparation.harvest(greenhouse);
                    var items = new java.util.LinkedHashSet<String>();
                    var estimates = new java.util.LinkedHashMap<String, Double>();
                    String cropTable = com.nstut.biotech.recipes.GreenhouseHarvestPreparation.tableId(harvest.state());
                    items.addAll(LootPreviewItems.collect(table(level, cropTable), id -> table(level, id), id ->
                            BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, new ResourceLocation(id))).map(tag -> tag.stream().map(h -> BuiltInRegistries.ITEM.getKey(h.value()).toString()).toList())
                                    .orElse(java.util.List.of())));
                    for (int sample = 0; sample < LootQuantityEstimate.SAMPLES; sample++) {
                        for (var output : com.nstut.biotech.recipes.GreenhouseHarvestPreparation.roll(level, net.minecraft.core.BlockPos.ZERO,
                                harvest.state(), LootQuantityEstimate.seed(sample))) {
                            if (output.isEmpty()) continue;
                            String id = BuiltInRegistries.ITEM.getKey(output.getItem()).toString();
                            items.add(id);
                            estimates.merge(id, output.getCount() * (double) harvest.count() / LootQuantityEstimate.SAMPLES, Double::sum);
                        }
                    }
                    JsonArray array = new JsonArray();
                    for (String id : items) {
                        var output = new JsonObject();
                        output.addProperty("item", id);
                        output.addProperty("mean", estimates.getOrDefault(id, 0.0));
                        array.add(output);
                    }
                    result.add(recipe.getId().toString(), array);
                } catch (com.nstut.nstutlib.recipes.RecipeTransactionException invalid) {
                    // Invalid opt-in recipes remain safely unusable; do not block player login.
                }
                continue;
            }
            if (!(recipe instanceof SlaughterhouseRecipe slaughter) || !slaughter.usesEntityLoot()) continue;
            var items = new java.util.LinkedHashSet<String>();
            var estimates = new java.util.LinkedHashMap<String, Double>();
            for (var ingredient : slaughter.getItemIngredients()) {
                if (!ingredient.isConsumable()) continue;
                var stack = ingredient.getItemStack();
                var entity = stack.getItem() instanceof MobItem mob ? mob.createMob(level, stack)
                        : stack.getItem() instanceof CapturedAnimalItem captured ? captured.createCapturedEntity(level, stack) : null;
                if (!(entity instanceof LivingEntity living)) continue;
                String tableId = living.getLootTable().toString();
                items.addAll(LootPreviewItems.collect(table(level, tableId), id -> table(level, id), id ->
                        BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, new ResourceLocation(id)))
                                .map(tag -> tag.stream().map(h -> BuiltInRegistries.ITEM.getKey(h.value()).toString()).toList())
                                .orElse(java.util.List.of())));
                var loot = level.getServer().getLootData().getLootTable(new ResourceLocation(tableId));
                var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, living)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, living.position())
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                        .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
                for (int sample = 0; sample < LootQuantityEstimate.SAMPLES; sample++) {
                    long seed = LootQuantityEstimate.seed(sample);
                    loot.getRandomItems(params, seed, output -> {
                        String id = BuiltInRegistries.ITEM.getKey(output.getItem()).toString();
                        if (!output.isEmpty()) {
                            items.add(id);
                            estimates.merge(id, output.getCount() * (double) stack.getCount() / LootQuantityEstimate.SAMPLES, Double::sum);
                        }
                    });
                }
            }
            JsonArray array = new JsonArray();
            for (String id : items) {
                var output = new JsonObject();
                output.addProperty("item", id);
                output.addProperty("mean", estimates.getOrDefault(id, 0.0));
                array.add(output);
            }
            result.add(slaughter.getId().toString(), array);
        }
        return result;
    }
    private static JsonElement table(ServerLevel level, String id) {
        var table = level.getServer().getLootData().getLootTable(new ResourceLocation(id));
        return net.minecraft.world.level.storage.loot.Deserializers.createLootTableSerializer().create().toJsonTree(table);
    }
}
