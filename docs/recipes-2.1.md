# Recipe processing in 2.1

All six Biotech machines use NsTut Lib 0.8's persisted transaction helper. Inputs are consumed once, progress survives reloads, invalid structures pause processing, exact remaining energy can finish a recipe, and outputs are committed only when they fit.

Biotech's internal hatches expose restorable `IItemHandlerModifiable`/`FluidTank` storage to the machine transaction path. NsTut Lib snapshots those handlers before input or output mutation and restores the complete state if a capability rejects or diverges during execution, preventing partial commits from becoming duplication or loss on retry.
