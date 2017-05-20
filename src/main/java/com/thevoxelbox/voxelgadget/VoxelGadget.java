package com.thevoxelbox.voxelgadget;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Plugin(id = "voxelgadget")
public class VoxelGadget {

    @Inject
    private PluginContainer container;

    private final Cause plugin_cause = Cause.of(NamedCause.of("plugin", this.container));

    @Listener
    public void onEnable(GameInitializationEvent e) {
    }

    @Listener
    public void onDispense(SpawnEntityEvent event, @First BlockSpawnCause spawn) {
        if (spawn.getType() != SpawnTypes.DISPENSE) {
            return;
        }
        BlockSnapshot dispenser_block = spawn.getBlockSnapshot();
        Optional<Player> notifier = event.getCause().get(NamedCause.NOTIFIER, Player.class);

        Item item = (Item) event.getEntities().get(0);
        ItemStackSnapshot items = item.item().get();

        ItemType i = items.getType();
        if (!i.getBlock().isPresent()) {
            return;
        }
        BlockType item_block = i.getBlock().get();

        Direction dir = dispenser_block.get(Keys.DIRECTION).get();
        Action action = null;

        int x = dispenser_block.getPosition().getX();
        int y = dispenser_block.getPosition().getY();
        int z = dispenser_block.getPosition().getZ();

        World world = Sponge.getServer().getWorld(dispenser_block.getWorldUniqueId()).get();
        TailState state = TailState.START;
        tail_search: while (true) {
            x += dir.asBlockOffset().getX();
            y += dir.asBlockOffset().getY();
            z += dir.asBlockOffset().getZ();

            BlockState block = world.getBlock(x, y, z);
            switch (state) {
            case START:
                if (block.getType() == BlockTypes.IRON_BLOCK) {
                    action = Action.PLACE;
                    state = TailState.END;
                } else {
                    return;
                }
                break;
            default:
                break tail_search;
            }
        }
        if (state != TailState.END) {
            return;
        }

        int target_x = dispenser_block.getPosition().getX();
        int target_y = dispenser_block.getPosition().getY();
        int target_z = dispenser_block.getPosition().getZ();
        target_x += dir.asBlockOffset().getX();
        target_y += dir.asBlockOffset().getY();
        target_z += dir.asBlockOffset().getZ();

        event.setCancelled(true);
        if (action == Action.PLACE) {
            world.setBlockType(target_x, target_y, target_z, item_block, this.plugin_cause);
        }
    }

    private static enum TailState {
        START,
        END,
    }

    private static enum Action {
        PLACE,
    }
}
