package com.thevoxelbox.voxelgadget.modifier;

import com.thevoxelbox.voxelgadget.Processor;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SetInventoryModifier extends AbstractModeModifier {

    @Override
    public int modify(Processor p) {
        p.setMode(ModifierType.SET_INVENTORY);
        Block behind = p.getDispenser().getRelative(p.getTrain(), p.getCurrent() + 1);
        if (behind.getState() instanceof InventoryHolder) {
            p.setInvOverride(((InventoryHolder) behind.getState()).getInventory());
            return 1;
        }
        return 0;
    }

    @Override
    public int modeModify(Processor p) {
        Block existing = p.getDispenser().getRelative(p.getTrain().getOppositeFace(), p.getOffset());
        if (p.getOffset3D() != null) {
            existing = p.getOffset3D().getBlock();
        }
        if (existing.getState() instanceof InventoryHolder) {
            Inventory target = ((InventoryHolder) existing.getState()).getInventory();
            Inventory disp = ((p.getInvOverride() == null) ? ((Dispenser) p.getDispenser().getState()).getInventory() : p.getInvOverride());
            if (p.getInvOverride() != null) {
                if (p.getOverride() != null) {
                    if (disp.contains(p.getOverride().getType()) && target.contains(p.getOverride().getType())) {
                        target.setItem(target.first(p.getOverride().getType()), disp.getItem(disp.first(p.getOverride().getType())));
                    }
                } else {
                    target.setContents(disp.getContents());
                }
            } else {
                if (p.getOverride() != null) {
                    if (disp.contains(p.getOverride().getType()) && target.contains(p.getOverride().getType())) {
                        target.setItem(target.first(p.getOverride().getType()), disp.getItem(disp.first(p.getOverride().getType())));
                        if (p.getOverride().getType().equals(p.getBlock().getType())) {
                            target.addItem(p.getBlock());
                        }
                    }
                } else {
                    target.setContents(disp.getContents());
                    target.addItem(p.getBlock());
                }
            }
        }
        return 0;
    }
}
