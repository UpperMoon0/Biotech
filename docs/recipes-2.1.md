# Recipe processing in 2.1

All six Biotech machines use NsTut Lib 0.8's persisted transaction helper. Inputs are consumed once, progress survives reloads, invalid structures pause processing, exact remaining energy can finish a recipe, and outputs are committed only when they fit.
