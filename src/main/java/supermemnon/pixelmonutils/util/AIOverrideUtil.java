package supermemnon.pixelmonutils.util;

import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.UUID;

public class AIOverrideUtil {
    private static final int stareAtPriority = 10;
    public boolean addToNPC(NPCEntity entity, UUID targetUUID) {
        entity.goalSelector.addGoal(stareAtPriority, new StareAtGoal(entity, targetUUID));
        return false;
    }

    class StandStillGoal extends Goal {

        @Override
        public boolean canUse() {
            return false;
        }
    }

    //Basically stays idle while cooldown is active.
    //Cooldown is inactive when at 0
    class StareAtGoal extends Goal {
        private static final int updateCooldownMax = 100;
        private int updateCooldown = updateCooldownMax;
        private static final int playerCooldownMax = 50;
        private int playerCooldown = playerCooldownMax;
        private final double detectionRadius = 8.0;
        Entity selfRef;
        UUID stareTargetUUID = null;
        Entity stareTarget = null;
        Entity nearTarget = null;
        public StareAtGoal(Entity selfRef, UUID stareTargetUUID) {
            this.selfRef = selfRef;
            this.stareTargetUUID = stareTargetUUID;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        private boolean findStareTarget() {
            ServerWorld world = (ServerWorld) selfRef.level;
            stareTarget = world.getEntity(stareTargetUUID);
            return stareTarget == null;
        }

        private boolean findNearTarget() {
            for (PlayerEntity player : (this.selfRef.level.getNearbyPlayers(EntityPredicate.DEFAULT, null,
                    new AxisAlignedBB(selfRef.getX()+detectionRadius,selfRef.getY()+detectionRadius,selfRef.getZ()+detectionRadius,
                            selfRef.getX()-detectionRadius,selfRef.getY()-detectionRadius,selfRef.getZ()-detectionRadius)))) {
                nearTarget = player;
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
            return findNearTarget();
        }

        private boolean handleTargetCheck() {
            if (stareTarget != null) {
                return true;
            }
            if (updateCooldown > 0) {
                updateCooldown--;
                return false;
            }
            else if (!findStareTarget()){
                updateCooldown = updateCooldownMax;
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !(nearTarget == null || stareTarget == null);
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
