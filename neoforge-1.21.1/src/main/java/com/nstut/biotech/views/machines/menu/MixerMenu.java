package com.nstut.biotech.views.machines.menu;

import com.nstut.biotech.blocks.entites.machines.MixerBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.nstutlib.recipes.ModRecipeData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MixerMenu extends MachineMenu {

    private final Level level;
    @Getter
    private final MixerBlockEntity blockEntity;
    private final BlockPos pos;

    @Setter
    @Getter
    private ModRecipeData recipe;

    @Setter
    private int energyCapacity;

    @Setter
    @Getter
    private int energyStored;

    @Setter
    @Getter
    private int energyConsumeRate;

    @Setter
    @Getter
    private int energyConsumed;

    @Setter
    @Getter
    private int recipeEnergyCost;

    private boolean isStructureValid;

    public int getEnergyCapacity() {
        return energyCapacity > 0? energyCapacity : 1;
    }

    public boolean getStructureValid() {
        return isStructureValid;
    }

    public void setStructureValid(boolean structureValid) {
        isStructureValid = structureValid;
    }

    public MixerMenu(int pContainerId, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        this(pContainerId, inventory, Objects.requireNonNull(inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos())));
    }

    public MixerMenu(int i, Inventory inventory, BlockEntity blockEntity) {
        super(MachineRegistries.MIXER.menu().get(), i);
        this.level = inventory.player.level();
        this.blockEntity = (MixerBlockEntity) blockEntity;
        this.pos = blockEntity.getBlockPos();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, pos), player, MachineRegistries.MIXER.block().get());
    }

    public boolean getIsOperating() {
        return recipe != null;
    }
}