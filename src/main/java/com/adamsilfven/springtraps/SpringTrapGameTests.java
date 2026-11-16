package com.adamsilfven.springtraps;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(SpringTraps.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpringTrapGameTests {
    @GameTest(template = "spring_trap_test", timeoutTicks = 80)
    public static void launchesEntityOnContact(GameTestHelper helper) {
        BlockPos trapPos = new BlockPos(1, 1, 1);
        helper.setBlock(trapPos, SpringTraps.SPRING_TRAP.get());
        Cow cow = helper.spawn(EntityType.COW, 1.5F, 2.0F, 1.5F);
        double startY = cow.getY();
        double[] peakY = { startY };
        helper.onEachTick(() -> peakY[0] = Math.max(peakY[0], cow.getY()));
        helper.runAfterDelay(12, () -> {
            if (cow.getY() < startY + 4.0D) {
                throw new GameTestAssertException("Cow did not launch after landing on trap");
            }
            if (!helper.getBlockState(trapPos).getValue(SpringTrapBlock.TRIGGERED)) {
                throw new GameTestAssertException("Trap was not triggered");
            }
            double horizontalTravel = Math.hypot(cow.getX() - helper.absolutePos(trapPos).getX() - 0.5D,
                    cow.getZ() - helper.absolutePos(trapPos).getZ() - 0.5D);
            if (horizontalTravel < 1.0D) {
                throw new GameTestAssertException("Cow was not launched sideways");
            }
        });
        helper.runAfterDelay(50, () -> {
            double height = peakY[0] - (helper.absolutePos(trapPos).getY() + 0.0625D);
            if (height < 20.0D || height > 25.0D) {
                throw new GameTestAssertException("Launch height was " + height + " blocks");
            }
            if (helper.getBlockState(trapPos).getValue(SpringTrapBlock.TRIGGERED)) {
                throw new GameTestAssertException("Trap did not reset");
            }
            helper.succeed();
        });
    }
}
