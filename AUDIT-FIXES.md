# Biotech / NsTut Lib audit remediation

This branch is the Biotech half of the coordinated audit remediation. Shared recipe, multiblock, scanner and Smart Hammer fixes live in NsTut Lib 0.8.1; this branch consumes that API and fixes Biotech-specific machine, hatch, networking, build, documentation, and captured-animal behavior.

The final parity audit also moved duplicated Biotech implementation and resources into shared source tiers, verified all 59 generated machine recipes are semantically identical across the three supported Minecraft targets, corrected Patchouli/reference metadata, hardened generation failures, and reduced cross-version UI/item drift through shared helpers where the underlying Minecraft APIs permit it.

Compatibility-specific code remains local when the APIs are materially different, including Forge versus NeoForge capabilities, 1.20 NBT versus later data components, 26.1 transfer/storage APIs, and the 26.1 rendering/JEI surface. Do not force those boundaries into common code through reflection or duplicated compatibility shims merely to reduce line count.