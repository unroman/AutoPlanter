package com.kotori316.auto_planter.planter;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.container.Container;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.kotori316.auto_planter.AutoPlanter;

public class PlanterBlock extends BlockWithEntity {
    public static final String name = "planter";

    public final BlockItem blockItem;

    public PlanterBlock() {
        super(Block.Settings.copy(Blocks.DIRT).strength(0.6f, 100));
        blockItem = new BlockItem(this, new Item.Settings().group(ItemGroup.DECORATIONS));
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
        BlockEntity entity = worldIn.getBlockEntity(pos);
        if (entity instanceof PlanterTile) {
            ItemStack stack = player.getStackInHand(handIn);
            if (hit.getSide() != Direction.UP || !PlanterTile.isSapling(stack)) {
                if (!worldIn.isClient)
                    ContainerProviderRegistry.INSTANCE.openContainer(new Identifier(PlanterContainer.GUI_ID), player, b -> b.writeBlockPos(pos));
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return AutoPlanter.Holder.PLANTER_TILE_TILE_ENTITY_TYPE.instantiate();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return Container.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockRemoved(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isClient) {
                BlockEntity entity = worldIn.getBlockEntity(pos);
                if (entity instanceof PlanterTile) {
                    PlanterTile inventory = (PlanterTile) entity;
                    ItemScatterer.spawn(worldIn, pos, inventory);
                    worldIn.updateHorizontalAdjacent(pos, state.getBlock());
                }
            }
            super.onBlockRemoved(state, worldIn, pos, newState, isMoving);

        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborUpdate(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (!worldIn.isClient) {
            BlockEntity t = worldIn.getBlockEntity(pos);
            if (t instanceof PlanterTile) {
                PlanterTile tile = (PlanterTile) t;
                tile.plantSapling();
            }
        }
    }
}
