package com.github.teamaurora.enhanced_mushrooms.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EMBoatItem extends Item {
    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::collides);
    private final Supplier<EntityType<EMBoatEntity>> boatSupplier;

    public EMBoatItem(Supplier<EntityType<EMBoatEntity>> boatSupplier, Settings settings) {
        super(settings);

        this.boatSupplier = boatSupplier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        HitResult hit = raycast(world, player, RaycastContext.FluidHandling.ANY);

        if (hit.getType() != HitResult.Type.BLOCK) {
            return new TypedActionResult<>(ActionResult.PASS, stack);
        }

        Vec3d rotation = player.getRotationVec(1.0F);

        List<Entity> entities = world.getOtherEntities(player, player.getBoundingBox().stretch(rotation.multiply(5.0D)).expand(1.0D), RIDERS);

        if (!entities.isEmpty()) {
            Vec3d playerCameraPos = player.getCameraPosVec(1.0F);

            for (Entity entity : entities) {
                Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
                if (box.contains(playerCameraPos)) {
                    return new TypedActionResult<>(ActionResult.PASS, stack);
                }
            }
        }

        EMBoatEntity boat = createBoat(world, hit.getPos().x, hit.getPos().y, hit.getPos().z);

        boat.setYaw(player.getYaw());

        if (!world.isSpaceEmpty(boat, boat.getBoundingBox().expand(-0.1D))) {
            return new TypedActionResult<>(ActionResult.FAIL, stack);
        }

        if (!world.isClient) {
            world.spawnEntity(boat);
        }

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }


    private EMBoatEntity createBoat(World world, double x, double y, double z) {
        EMBoatEntity entity = boatSupplier.get().create(world);
        if (entity != null) {
            entity.setPos(x, y, z);
            entity.updatePosition(x, y, z);
            entity.setVelocity(Vec3d.ZERO);
            entity.prevX = x;
            entity.prevY = y;
            entity.prevZ = z;
        }
        return entity;
    }
}
