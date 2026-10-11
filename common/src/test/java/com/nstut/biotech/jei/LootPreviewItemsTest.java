package com.nstut.biotech.jei;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class LootPreviewItemsTest {
    @Test void referencedTablesTagsAndConditionalChildrenRemainDiscoverableWithoutRolling() {
        var root = JsonParser.parseString("""
                {"pools":[{"entries":[
                  {"type":"minecraft:alternatives","children":[
                    {"type":"minecraft:item","name":"minecraft:porkchop","conditions":[{"condition":"minecraft:random_chance","chance":0.000001}]},
                    {"type":"minecraft:tag","name":"test:products"}]},
                  {"type":"minecraft:loot_table","value":"test:nested"}]}]}
                """);
        var nested = JsonParser.parseString("""
                {"pools":[{"entries":[{"type":"minecraft:item","name":"minecraft:porkchop"},
                  {"type":"minecraft:item","name":"minecraft:diamond"},
                  {"type":"minecraft:loot_table","name":"test:nested"}]}]}
                """);
        assertEquals(List.of("minecraft:porkchop", "minecraft:leather", "minecraft:diamond"),
                LootPreviewItems.collect(root, id -> nested, id -> List.of("minecraft:leather")));
    }
    @Test void emptyAndReplacedLootTablesDoNotReuseOldItems() {
        assertTrue(LootPreviewItems.collect(JsonParser.parseString("{}"), id -> null, id -> List.of()).isEmpty());
        var replacement = JsonParser.parseString("{\"pools\":[{\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"minecraft:emerald\"}]}]}");
        assertEquals(List.of("minecraft:emerald"), LootPreviewItems.collect(replacement, id -> null, id -> List.of()));
    }
}
