# Biotech 2.7-alpha.2

- Require the current layout for all six machines. Complete 2.5 shells and the old 2.6 Habitat no longer form.
- Invalid structures stop processing. No automatic block replacement, relocation, inventory clearing or transaction reset occurs.
- Before updating, finish active cycles and empty item/fluid/energy hatches. If already updated, recover hatch contents before dismantling and retain a backup before breaking an active controller.
- Require NsTut Lib 0.8.2. Smart Hammer can replace ordinary blocks in the current pattern, returning recovered block items to inventory (or dropping overflow at the player's feet). Unsupported/populated block entities and protected infrastructure block the build; recover hatches manually first.
- Service Pipes connect in all six directions with multipart models and matching collision arms. Placement/removal updates adjacent connections; Smart Hammer resolves connected states instead of leaving default variants.

This changes the next alpha release; published 2.6 and 2.7-alpha.1 binaries are unchanged.
