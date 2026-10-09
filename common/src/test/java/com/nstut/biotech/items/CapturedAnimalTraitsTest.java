package com.nstut.biotech.items;

import com.google.gson.JsonParser;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapturedAnimalTraitsTest {
    @Test void decodesPackedHorseAppearanceWithoutExposingWorldIdentity() {
        var traits = CapturedAnimalTraits.describe("minecraft:horse", JsonParser.parseString(
                "{\"Variant\":772,\"Age\":0,\"UUID\":\"private\",\"Pos\":[1,2,3]}").getAsJsonObject());
        assertTrue(traits.stream().map(line -> (TranslatableContents) line.getContents())
                .anyMatch(content -> content.getKey().endsWith("coat") && content.getArgs()[0].equals("Black")));
        assertTrue(traits.stream().map(line -> (TranslatableContents) line.getContents())
                .anyMatch(content -> content.getKey().endsWith("markings") && content.getArgs()[0].equals("White spots")));
        assertEquals(3, traits.size());
    }
    @Test void distinguishesPandaGenesAndRegistryVariants() {
        var panda = CapturedAnimalTraits.describe("minecraft:panda", JsonParser.parseString(
                "{\"MainGene\":4,\"HiddenGene\":5}").getAsJsonObject());
        assertEquals("Brown", ((TranslatableContents) panda.get(0).getContents()).getArgs()[0]);
        assertEquals("Weak", ((TranslatableContents) panda.get(1).getContents()).getArgs()[0]);
        var wolf = CapturedAnimalTraits.describe("minecraft:wolf", JsonParser.parseString(
                "{\"variant\":\"minecraft:spotted\"}").getAsJsonObject());
        assertEquals("Spotted", ((TranslatableContents) wolf.get(0).getContents()).getArgs()[0]);
    }
}
