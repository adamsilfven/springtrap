package com.adamsilfven.springtraps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SpringTrapBlock extends Block {
    public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 1, 15);
    private static final int RESET_TICKS = 20;
    private static final double LAUNCH_SPEED = 2.15D;
    private static final double HORIZONTAL_SPEED = 0.65D;

    public SpringTrapBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TRIGGERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRIGGERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        AABB trigger = new AABB(pos.getX() + 0.0625D, pos.getY(), pos.getZ() + 0.0625D,
                pos.getX() + 0.9375D, pos.getY() + 0.25D, pos.getZ() + 0.9375D);
        if (!level.isClientSide && !state.getValue(TRIGGERED)
                && entity instanceof LivingEntity && entity.isAlive()
                && !entity.isSpectator() && entity.getBoundingBox().intersects(trigger)) {
            level.setBlock(pos, state.setValue(TRIGGERED, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, RESET_TICKS);

            double heading = level.random.nextDouble() * Math.PI * 2.0D;
            entity.setDeltaMovement(Math.cos(heading) * HORIZONTAL_SPEED, LAUNCH_SPEED,
                    Math.sin(heading) * HORIZONTAL_SPEED);
            entity.hasImpulse = true;
            entity.hurtMarked = true;

            level.playSound(null, pos, SpringTraps.SPRING_BOING.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
            ServerLevel server = (ServerLevel) level;
            server.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D,
                    18, 0.42D, 0.12D, 0.42D, 0.055D);
            server.sendParticles(ParticleTypes.POOF,
                    pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                    24, 0.42D, 0.16D, 0.42D, 0.04D);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TRIGGERED)) {
            level.setBlock(pos, state.setValue(TRIGGERED, false), Block.UPDATE_ALL);
        }
    }
}
