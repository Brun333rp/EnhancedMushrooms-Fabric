package com.github.teamaurora.enhanced_mushrooms.util;

import com.github.teamaurora.enhanced_mushrooms.block.EMBoatEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class BoatSpawnNetworkHandler {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(EMBoatEntity.SPAWN_BOAT_CHANNEL, BoatSpawnNetworkHandler::accept);
    }

    public static void accept(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt();
        UUID uuid = buffer.readUuid();
        EntityType<?> type = Registry.ENTITY_TYPE.get(buffer.readVarInt());
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        byte pitch = buffer.readByte();
        byte yaw = buffer.readByte();

        if (client.isOnThread()) {
            spawn(client, id, uuid, type, x, y, z, pitch, yaw);
        } else {
            client.execute(() -> spawn(client, id, uuid, type, x, y, z, pitch, yaw));
        }
    }

    private static void spawn(MinecraftClient client, int id, UUID uuid, EntityType type, double x, double y, double z, byte pitch, byte yaw) {
        ClientWorld world = client.world;

        if (world == null) {
            return;
        }

        Entity entity = type.create(world);

        if (entity == null) {
            return;
        }

        entity.setId(id);
        entity.setUuid(uuid);
        entity.updatePosition(x, y, z);
        entity.updateTrackedPosition(x, y, z);
        entity.setPitch(pitch * 360 / 256F);
        entity.setYaw(yaw * 360 / 256F);

        world.addEntity(entity.getId(), entity);
    }
}
