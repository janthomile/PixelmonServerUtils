package supermemnon.pixelmonutils.util;

import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class AIOverrideUtil {
    private static final int stareAtPriority = -1;

    public static void removeStare(NPCEntity entity) {
        for (PrioritizedGoal goal : entity.goalSelector.getRunningGoals().collect(Collectors.toList())) {
            if (goal.getGoal() instanceof StareAtPositionGoal) {
                entity.goalSelector.removeGoal(goal);
            }
        }
    }
    public static void safeAddStare(NPCEntity entity, Vector3d pos) {
        for (PrioritizedGoal goal : entity.goalSelector.getRunningGoals().collect(Collectors.toList())) {
            if (goal.getGoal() instanceof StareAtPositionGoal) {
                return;
            }
        }
        addStare(entity, pos);
    }
    public static void addStare(NPCEntity entity, Vector3d pos) {
        entity.goalSelector.addGoal(stareAtPriority, new StareAtPositionGoal(entity, pos));
    }

    class StandStillGoal extends Goal {

        @Override
        public boolean canUse() {
            return false;
        }
    }

    static class StareAtPositionGoal extends Goal {
        private static final int updateCooldownMax = 100;
        private int updateCooldown = updateCooldownMax;
        private static final int playerCooldownMax = 50;
        private int playerCooldown = playerCooldownMax;
        private final double detectionRadius = 8.0;
        Entity selfRef;
        Vector3d stareTargetPos;
        Entity nearTarget = null;
        public StareAtPositionGoal(Entity selfRef, Vector3d pos) {
            this.selfRef = selfRef;
            this.stareTargetPos = pos;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        private boolean handlePlayerCheck() {
            selfRef.level.getServer().sendMessage(new StringTextComponent("Checking for player target!"),Util.NIL_UUID);
            for (PlayerEntity player : (this.selfRef.level.getNearbyPlayers(EntityPredicate.DEFAULT, null,
                    new AxisAlignedBB(selfRef.getX()+detectionRadius,selfRef.getY()+detectionRadius,selfRef.getZ()+detectionRadius,
                            selfRef.getX()-detectionRadius,selfRef.getY()-detectionRadius,selfRef.getZ()-detectionRadius)))) {
                nearTarget = player;
                selfRef.level.getServer().sendMessage(new StringTextComponent("Found player target!"),Util.NIL_UUID);
                return true;
            }
            nearTarget = null;
            return false;
        }

        private boolean isPlayerNearby() {
            if (playerCooldown > 0) {
                playerCooldown--;
                return false;
            }
            playerCooldown = playerCooldownMax;
            return handlePlayerCheck();
        }

        @Override
        public boolean canContinueToUse() {
            return !(nearTarget == null) && nearTarget.distanceToSqr(selfRef) < (detectionRadius*detectionRadius);
        }
        @Override
        public void tick() {
            selfRef.lookAt(EntityAnchorArgument.Type.EYES, stareTargetPos);
        }
        @Override
        public boolean canUse() {
            return isPlayerNearby();
        }
    }
    static class StareAtEntityGoal extends Goal {
        private static final int updateCooldownMax = 100;
        private int updateCooldown = updateCooldownMax;
        private static final int playerCooldownMax = 50;
        private int playerCooldown = playerCooldownMax;
        private final double detectionRadius = 8.0;
        Entity selfRef;
        UUID stareTargetUUID;
        Entity stareTarget = null;
        Entity nearTarget = null;
        public StareAtEntityGoal(Entity selfRef, UUID stareTargetUUID) {
            this.selfRef = selfRef;
            this.stareTargetUUID = stareTargetUUID;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        private boolean findStareTarget() {
            ServerWorld world = (ServerWorld) selfRef.level;
            stareTarget = world.getEntity(stareTargetUUID);
            return stareTarget == null;
        }

        private boolean handlePlayerCheck() {
            selfRef.level.getServer().sendMessage(new StringTextComponent("Checking for player target!"),Util.NIL_UUID);
            for (PlayerEntity player : (this.selfRef.level.getNearbyPlayers(EntityPredicate.DEFAULT, null,
                    new AxisAlignedBB(selfRef.getX()+detectionRadius,selfRef.getY()+detectionRadius,selfRef.getZ()+detectionRadius,
                            selfRef.getX()-detectionRadius,selfRef.getY()-detectionRadius,selfRef.getZ()-detectionRadius)))) {
                nearTarget = player;
                selfRef.level.getServer().sendMessage(new StringTextComponent("Found player target!"),Util.NIL_UUID);
                return true;
            }
            nearTarget = null;
            return false;
        }

        private boolean isPlayerNearby() {
            if (playerCooldown > 0) {
                playerCooldown--;
                return false;
            }
            playerCooldown = playerCooldownMax;
            return handlePlayerCheck();
        }

        private boolean handleTargetCheck() {
            if (stareTarget != null) {
                return true;
            }
            if (updateCooldown > 0) {
                updateCooldown--;
                return false;
            }
            selfRef.level.getServer().sendMessage(new StringTextComponent("Checking for stare target!"),Util.NIL_UUID);
            if (!findStareTarget()){
                updateCooldown = updateCooldownMax;
                return false;
            }
            selfRef.level.getServer().sendMessage(new StringTextComponent("Found Stare target!"),Util.NIL_UUID);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !(nearTarget == null || stareTarget == null) && nearTarget.distanceToSqr(selfRef) < (detectionRadius*detectionRadius);
        }
        @Override
        public void tick() {
            selfRef.lookAt(EntityAnchorArgument.Type.EYES, stareTarget.position());
        }
        @Override
        public boolean canUse() {
            return handleTargetCheck() && isPlayerNearby();
        }
    }
}
