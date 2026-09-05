# Networking changes in 2.1

Biotech network protocol version 2 requires exact client/server agreement. Hatch state is sent only when changed to players tracking the containing chunk. Machine UI state is sent to tracking players at a reduced cadence instead of globally every server tick.

All six custom machine `RecipeType` and `RecipeSerializer` instances are registered under the `biotech` namespace before datapack recipe synchronization. NeoForge 26.1.2 sends only those registered recipe types during `OnDatapackSyncEvent`, while the client refreshes its recipe cache from `RecipesReceivedEvent` and clears it on logout.

GameTest coverage verifies that every machine recipe type resolves to its expected `biotech:*` registry ID. This specifically guards against join-time recipe-content encoding failures caused by attempting to synchronize an unregistered recipe type.
