package com.nstut.biotech;

import com.google.gson.JsonParser;
import com.nstut.biotech.machines.MachineBalance;
import com.nstut.biotech.machines.MachineStatus;
import com.nstut.biotech.machines.RedstoneMode;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class FactoryControlContractTest {
    @Test void energyScalingPreservesFreeCyclesAndCannotOverflow() {
        assertEquals(0, MachineBalance.energyCost(0, 100));
        assertEquals(1, MachineBalance.energyCost(1, 0.01));
        assertEquals(101, MachineBalance.energyCost(10001, 0.01));
        assertEquals(20000, MachineBalance.energyCost(10000, 2));
        assertEquals(Integer.MAX_VALUE, MachineBalance.energyCost(Integer.MAX_VALUE, 100));
    }

    @Test void legacyAndUnknownSavedModesKeepAutomaticProcessing() {
        assertEquals(RedstoneMode.IGNORE, RedstoneMode.fromId(-1));
        assertEquals(RedstoneMode.IGNORE, RedstoneMode.fromId(500));
        assertTrue(RedstoneMode.IGNORE.permits(false));
        assertTrue(RedstoneMode.IGNORE.permits(true));
        assertFalse(RedstoneMode.HIGH.permits(false));
        assertTrue(RedstoneMode.HIGH.permits(true));
        assertTrue(RedstoneMode.LOW.permits(false));
        assertFalse(RedstoneMode.LOW.permits(true));
    }

    @Test void englishFallbackCoversLiteralReferencesAndDynamicStatusKeys() throws Exception {
        Path root = Path.of("").toAbsolutePath();
        while (root != null && !Files.isRegularFile(root.resolve("settings.gradle"))) root = root.getParent();
        assertNotNull(root, "Could not locate the repository from the loader's test working directory");
        var english = JsonParser.parseString(Files.readString(root.resolve(
                "common/src/main/resources/assets/biotech/lang/en_us.json"))).getAsJsonObject();
        for (MachineStatus status : MachineStatus.values()) assertTrue(english.has(status.translationKey()), status.translationKey());
        for (RedstoneMode mode : RedstoneMode.values()) assertTrue(english.has(mode.translationKey()), mode.translationKey());
        var references = Pattern.compile("(?:translatable|caption)\\(\"([^\"]+)\"");
        for (String module : List.of("common", "common-legacy", "common-neoforge", "forge-1.20.1", "neoforge-1.21.1", "neoforge-26.1.2")) {
            try (var paths = Files.walk(root.resolve(module + "/src/main/java"))) {
                for (Path source : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                    var matcher = references.matcher(Files.readString(source));
                    while (matcher.find()) {
                        String key = matcher.group(1);
                        if (!key.contains(".")) key = "ui.biotech." + key;
                        if (key.contains(".biotech.") && !key.endsWith(".")) assertTrue(english.has(key), source + ": " + key);
                    }
                }
            }
        }
        assertEquals(2, english.get("ui.biotech.energy.stored").getAsString().split("%s", -1).length - 1);
        assertEquals(1, english.get("ui.biotech.energy.rate").getAsString().split("%s", -1).length - 1);
    }
}
