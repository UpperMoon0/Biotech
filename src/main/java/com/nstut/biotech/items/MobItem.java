package com.nstut.biotech.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MobItem extends Item {
    private final int type;

    public MobItem(int type) {
        super(new Item.Properties());
        this.type = type;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!world.isClientSide) {
            Mob mob = null;
            switch (type) {
                case 1: { // Cow
                    mob = new Cow(EntityType.COW, world);
                    break;
                }
                case 2: { // Baby Cow
                    mob = new Cow(EntityType.COW, world);
                    mob.setBaby(true);
                    break;
                }
                case 3: { // Chicken
                    mob = new Chicken(EntityType.CHICKEN, world);
                    break;
                }
                case 4: { // Baby Chicken
                    mob = new Chicken(EntityType.CHICKEN, world);
                    mob.setBaby(true);
                    break;
                }
                case 5: { // Pig
                    mob = new Pig(EntityType.PIG, world);
                    break;
                }
                case 6: { // Baby Pig
                    mob = new Pig(EntityType.PIG, world);
                    mob.setBaby(true);
                    break;
                }
                case 7:
                case 8: { // Sheep (adult for 7, baby for 8)
                    Sheep sheep = new Sheep(EntityType.SHEEP, world);
                    // Check if the item has stored sheep color; if so, apply it; otherwise, use white as default
                    if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("SheepColor")) {
                        int colorId = stack.getTag().getInt("SheepColor");
                        DyeColor color = DyeColor.byId(colorId);
                        sheep.setColor(color);
                    } else {
                        sheep.setColor(DyeColor.WHITE);
                    }
                    if (type == 8) {
                        sheep.setBaby(true);
                    }
                    mob = sheep;
                    break;
                }
                case 9: { // Rabbit
                    mob = new Rabbit(EntityType.RABBIT, world);
                    break;
                }
                case 10: { // Baby Rabbit
                    mob = new Rabbit(EntityType.RABBIT, world);
                    mob.setBaby(true);
                    break;
                }
                default: {
                    break;
                }
            }

            if (mob != null && player != null) {
                mob.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                world.addFreshEntity(mob);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (type == 7 || type == 8) { // Only for sheep items
            DyeColor color;
            if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("SheepColor")) {
                int colorId = stack.getTag().getInt("SheepColor");
                color = DyeColor.byId(colorId);
            } else {
                color = DyeColor.WHITE;
            }
            // Convert the color name to a capitalized string
            String name = color.getName();
            String[] words = name.split("_");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    sb.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1))
                            .append(" ");
                }
            }
            String capitalizedName = sb.toString().trim();
            tooltip.add(Component.literal("Color: " + capitalizedName));
        }
    }
}