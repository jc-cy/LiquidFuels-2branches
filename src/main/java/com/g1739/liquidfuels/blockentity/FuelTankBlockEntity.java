package com.g1739.liquidfuels.blockentity;

import com.g1739.liquidfuels.block.FuelTankBlock;
import com.g1739.liquidfuels.registry.LiquidFuelBlockEntities;
import com.g1739.liquidfuels.util.FuelTankContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlockEntity extends BlockEntity {
    private final TankFluidHandler fluidHandler = new TankFluidHandler();
    private FluidStack fluid = FluidStack.EMPTY;
    private boolean dropOnRemove = true;

    public FuelTankBlockEntity(BlockPos pos, BlockState state) {
        super(LiquidFuelBlockEntities.FUEL_TANK.get(), pos, state);
    }

    public int getCapacity() {
        return getBlockState().getBlock() instanceof FuelTankBlock tank ? tank.getCapacity() : 0;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public FluidStack getFluid() {
        return getStoredFluid();
    }

    public void setFluid(FluidStack stack) {
        if (stack.isEmpty() || stack.getAmount() <= 0) {
            fluid = FluidStack.EMPTY;
        } else {
            fluid = stack.copy();
            fluid.setAmount(Math.min(fluid.getAmount(), getCapacity()));
        }
        markChangedAndSync();
    }

    public boolean shouldDropOnRemove() {
        return dropOnRemove;
    }

    public void discardDropOnRemove() {
        dropOnRemove = false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        FluidStack stored = getStoredFluid();
        if (!stored.isEmpty()) {
            tag.put(FuelTankContents.FLUID_TAG, FuelTankContents.writeFluid(stored));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(FuelTankContents.FLUID_TAG, CompoundTag.TAG_COMPOUND)) {
            fluid = FuelTankContents.readFluid(tag.getCompound(FuelTankContents.FLUID_TAG));
        } else {
            fluid = FluidStack.EMPTY;
        }
        if (!fluid.isEmpty() && fluid.getAmount() > getCapacity()) {
            fluid.setAmount(getCapacity());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        loadAdditional(pkt.getTag(), provider);
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private final class TankFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank == 0 ? getStoredFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }

            FluidStack current = getStoredFluid();
            if (!current.isEmpty() && !isSameFluid(current, resource)) {
                return 0;
            }

            int filled = Math.min(getCapacity() - current.getAmount(), resource.getAmount());
            if (filled <= 0) {
                return 0;
            }

            if (action.execute()) {
                FluidStack result = resource.copy();
                result.setAmount(current.getAmount() + filled);
                fluid = result;
                markChangedAndSync();
            }
            return filled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack current = getStoredFluid();
            if (resource.isEmpty() || current.isEmpty() || !isSameFluid(current, resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack current = getStoredFluid();
            if (maxDrain <= 0 || current.isEmpty()) {
                return FluidStack.EMPTY;
            }

            int drained = Math.min(maxDrain, current.getAmount());
            FluidStack result = current.copy();
            result.setAmount(drained);

            if (action.execute()) {
                current.shrink(drained);
                fluid = current;
                if (fluid.getAmount() <= 0) {
                    fluid = FluidStack.EMPTY;
                }
                markChangedAndSync();
            }
            return result;
        }

        private boolean isSameFluid(FluidStack first, FluidStack second) {
            return FluidStack.isSameFluidSameComponents(first, second);
        }
    }

    private FluidStack getStoredFluid() {
        FluidStack stored = fluid.copy();
        int capacity = getCapacity();
        if (!stored.isEmpty() && capacity > 0 && stored.getAmount() > capacity) {
            stored.setAmount(capacity);
        }
        return stored;
    }
}
