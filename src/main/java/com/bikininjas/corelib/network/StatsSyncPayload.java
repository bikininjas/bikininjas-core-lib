package com.bikininjas.corelib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Server-to-client payload that syncs a player's current stats and HUD prefs.
 * <p>
 * Sent periodically by {@code PlayerStatsManager} for every online player
 * who has the overlay enabled.
 *
 * @param visible       whether the stats overlay should be displayed
 * @param fields        the stat field names to show (e.g. {@code ["deaths", "kills"]})
 * @param deaths        current death count
 * @param kills         current kill count
 * @param blocksBroken  current block-break count
 * @param crafts        current craft count
 */
public record StatsSyncPayload(
        boolean visible,
        Set<String> fields,
        int deaths,
        int kills,
        int blocksBroken,
        int crafts
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StatsSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.parse("core_lib:stats_sync"));

    public static final StreamCodec<FriendlyByteBuf, StatsSyncPayload> STREAM_CODEC =
            StreamCodec.ofMember(StatsSyncPayload::write, StatsSyncPayload::new);

    public StatsSyncPayload(FriendlyByteBuf buf) {
        this(
                buf.readBoolean(),
                buf.readCollection(HashSet::new, FriendlyByteBuf::readUtf),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(visible);
        buf.writeCollection(fields, FriendlyByteBuf::writeUtf);
        buf.writeInt(deaths);
        buf.writeInt(kills);
        buf.writeInt(blocksBroken);
        buf.writeInt(crafts);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
