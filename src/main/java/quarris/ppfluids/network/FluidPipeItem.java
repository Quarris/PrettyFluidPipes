package quarris.ppfluids.network;

import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import quarris.ppfluids.items.FluidItem;
import quarris.ppfluids.pipe.FluidPipeTileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FluidPipeItem extends PipeItem {

    protected static Field lastWorldTick = ObfuscationReflectionHelper.findField(PipeItem.class, "lastWorldTick");
    protected static Field startInventory = ObfuscationReflectionHelper.findField(PipeItem.class, "startInventory");
    protected static Field currGoalPos = ObfuscationReflectionHelper.findField(PipeItem.class, "currGoalPos");

    protected static Method getNextTile = ObfuscationReflectionHelper.findMethod(PipeItem.class, "getNextTile", PipeTileEntity.class, boolean.class);
    protected static Method onPathObstructed = ObfuscationReflectionHelper.findMethod(PipeItem.class, "onPathObstructed", PipeTileEntity.class, boolean.class);

    public FluidPipeItem(ItemStack stack, float speed) {
        super(stack, speed);
    }

    public FluidPipeItem(CompoundNBT nbt) {
        super(nbt);
    }
/*
    @Override
    public void updateInPipe(PipeTileEntity currPipe) {
        long worldTick = currPipe.getWorld().getGameTime();

        try {
            if (lastWorldTick.getLong(this) != worldTick) {
                lastWorldTick.setLong(this, worldTick);
                float currSpeed = this.speed;
                BlockPos myPos = new BlockPos(this.x, this.y, this.z);
                if (!myPos.equals(currPipe.getPos()) && (currPipe.getPos().equals(this.getDestPipe()) || !myPos.equals(startInventory.get(this)))) {
                    currPipe.getItems().remove(this);
                    PipeTileEntity next = (PipeTileEntity) getNextTile.invoke(this, currPipe, true);
                    if (next == null) {
                        if (!currPipe.getWorld().isRemote) {
                            if (currPipe.getPos().equals(this.getDestPipe())) {
                                this.stack = this.store(currPipe);
                                if (!this.stack.isEmpty()) {
                                    onPathObstructed.invoke(this, currPipe, true);
                                }
                            } else {
                                onPathObstructed.invoke(this, currPipe, false);
                            }
                        }

                        return;
                    }

                    next.getItems().add(this);
                } else {
                    double dist = (new Vec3d((BlockPos) currGoalPos.get(this))).squareDistanceTo((this.x - 0.5F), (this.y - 0.5F), (this.z - 0.5F));
                    if (dist < (double)(this.speed * this.speed)) {
                        PipeTileEntity next = (PipeTileEntity) getNextTile.invoke(this, currPipe, false);
                        BlockPos nextPos;
                        if (next == null) {
                            if (!currPipe.getPos().equals(this.getDestPipe())) {
                                currPipe.getItems().remove(this);
                                if (!currPipe.getWorld().isRemote) {
                                    onPathObstructed.invoke(this, currPipe, false);
                                }

                                return;
                            }

                            nextPos = this.getDestInventory();
                        } else {
                            nextPos = next.getPos();
                        }

                        float tolerance = 0.001F;
                        if (dist >= (double)(tolerance * tolerance)) {
                            Vec3d motion = new Vec3d(this.x - this.lastX, this.y - this.lastY, this.z - this.lastZ);
                            Vec3d diff = new Vec3d((float)nextPos.getX() + 0.5F - this.x, (float)nextPos.getY() + 0.5F - this.y, (float)nextPos.getZ() + 0.5F - this.z);
                            if (motion.crossProduct(diff).length() >= (double)tolerance) {
                                currSpeed = (float)Math.sqrt(dist);
                            } else {
                                currGoalPos.set(this, nextPos);
                            }
                        } else {
                            currGoalPos.set(this, nextPos);
                        }
                    }
                }

                this.lastX = this.x;
                this.lastY = this.y;
                this.lastZ = this.z;
                BlockPos currentBlockPos = (BlockPos)currGoalPos.get(this);
                Vec3d dist = new Vec3d((float)currentBlockPos.getX() + 0.5F - this.x, (float)currentBlockPos.getY() + 0.5F - this.y, (float)currentBlockPos.getZ() + 0.5F - this.z);
                dist = dist.normalize();
                this.x = (float)(this.x + dist.x * currSpeed);
                this.y = (float)(this.y + dist.y * currSpeed);
                this.z = (float)(this.z + dist.z * currSpeed);
            }
        } catch (Exception e) {
            PPFluids.LOGGER.error(e);
        }
    }

 */
    @Override
    protected ItemStack store(PipeTileEntity currPipe) {
        if (currPipe instanceof FluidPipeTileEntity) {
            FluidPipeTileEntity currFluidPipe = (FluidPipeTileEntity) currPipe;
            Direction dir = Utility.getDirectionFromOffset(this.getDestInventory(), this.getDestPipe());
            IPipeConnectable connectable = currPipe.getPipeConnectable(dir);
            if (connectable != null) {
                return connectable.insertItem(currPipe.getWorld(), currPipe.getPos(), dir, this);
            } else {
                IFluidHandler handler = currFluidPipe.getFluidHandler(dir, this);
                if (handler == null)
                    return this.stack;

                return FluidItem.insertFluid(handler, this.stack, false);
            }
        }

        return ItemStack.EMPTY;
    }


}
