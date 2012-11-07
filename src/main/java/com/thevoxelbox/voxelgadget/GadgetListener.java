package com.thevoxelbox.voxelgadget;

import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.block.*;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.Iterator;

/**
 *
 * @author Gavjenks
 */
public class GadgetListener implements Listener {

    public static int bx;
    public static int by;
    public static int bz;
    public static Inventory inv;
    public static Inventory tinv;
    public static boolean infinite;
    public static boolean line;
    public static ItemStack currentStack;
    public static ItemStack backup;
    public static int GiveBlockID;
    public static int TakeBlockID;
    public static byte OffsetOneData;
    public static byte OffsetFiveData;
    public static byte OffsetTwentyFiveData;
    public static byte OverrideData;
    public static byte FiniteData;
    public static byte SkipData;
    public static byte LineData;
    public static byte NeedsAirData;
    public static byte OverridingData;
    public static int OverridingID;
    public static byte FilterData;
    public static byte FilteringData;
    public static int FilteringID;
    public static int CollectID;

    //tools
    protected static final Logger log = Logger.getLogger("Minecraft");


    //special crap like tools or seeds, only gets checked for ids over 254
    public void toolUse(Block targetBlock, Block thisBlock, int toolID, short toolData ) {
        if (toolID == 347 && targetBlock.getTypeId() == 35){
            targetBlock.getWorld().getBlockAt(targetBlock.getX(),targetBlock.getY()+2,targetBlock.getZ()).setTypeIdAndData(35,(byte)targetBlock.getRelative(BlockFace.UP).getLightLevel(),true);
            if (targetBlock.getRelative(BlockFace.UP).getLightLevel() < targetBlock.getData()){
                targetBlock.getWorld().getBlockAt(targetBlock.getX(),targetBlock.getY()+1,targetBlock.getZ()).setTypeId(76);
            } else {
                targetBlock.getWorld().getBlockAt(targetBlock.getX(),targetBlock.getY()+1,targetBlock.getZ()).setTypeId(0);
            }
        } else if (toolID == 351) { //various dyes / voxelfoods
            if (toolData == 13) { //poison vial + offset invisistair
                Iterator it = targetBlock.getWorld().getLivingEntities().iterator();
                LivingEntity victim;
                while (it.hasNext()) {
                    victim = (LivingEntity) it.next();
                    if (victim.getLocation().distance(targetBlock.getLocation()) < 3) {
                        victim.setNoDamageTicks(0);
                        victim.setHealth(0);

                    }
                }
            }
        } else if (toolID == 368) { //tactical bacon bomb + offset invisistair
            targetBlock.getWorld().createExplosion(targetBlock.getLocation(), 0);
            Iterator it = targetBlock.getWorld().getLivingEntities().iterator();
            LivingEntity victim;
            while (it.hasNext()) {
                victim = (LivingEntity) it.next();
                if (victim.getLocation().distance(targetBlock.getLocation()) < 6) {
                    victim.setNoDamageTicks(0);
                    victim.setHealth(0);
                }
            }
        } else if (toolID == 369) { //blaze rod + target is wooden stair (can be invis)
            targetBlock.getWorld().strikeLightning(targetBlock.getLocation());
        } else if (toolID == 326 && targetBlock.getTypeId() == 53) { //water bucket + target is wooden stair (can be invis)
            targetBlock.getWorld().setStorm(true);
        } else if (toolID == 325 && targetBlock.getTypeId() == 53) { //empty bucket + target is wooden stair (can be invis)
            targetBlock.getWorld().setStorm(false);
        }

    }


    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        //log.warning("himom"); //Woah...EVERY time? - Giltwist
        try {
            infinite = true;
            line = false;
            int lineOffset = 0;
            OverridingData = -1;
            OverridingID = -1;
            FilteringID = -1;
            Block thisBlock;
            Block b = event.getBlock();
            currentStack = event.getItem();
            bx = b.getX();
            by = b.getY();
            bz = b.getZ();
            World w = b.getWorld();

            try {
                if (w.getBlockAt(bx, by - 1, bz).getTypeId() == CollectID) {
                    event.setCancelled(true);
                    Entity[] toCollect = b.getChunk().getEntities();
                    ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                    for (int i = 0; i < toCollect.length; i++) {
                        if (toCollect[i] instanceof Item) {
                            if (toCollect[i].getLocation().distance(b.getLocation()) < 2) {
                                ItemStack toAdd = ((Item) toCollect[i]).getItemStack();
                                bc.getInventory().addItem(toAdd);
                                ((Entity) toCollect[i]).remove();
                            }
                        }
                    }
                }
            } catch (Exception c) {
            }

            int counter = 0;
            //******************Main First Example.  Stuff between Asterisks is just copied and pasted more or less for rest of code **********************
            thisBlock = w.getBlockAt(bx - 1, by, bz);
            if (thisBlock.getTypeId() == GiveBlockID && event.getItem().getTypeId() != 23) { //iron by default, placement / giver block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                counter = 0;
                while (w.getBlockAt((bx - 1 - modifier), by, bz).getTypeId() == 35 && counter <64) {
                counter++;
                    //options for different special blocks, in order of how often I think they would probably be used (for practical speed)
                    if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx - 2 - modifier, by, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx - 2 - modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx - 2 - modifier, by, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx - 2 - modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx + 1 + (line?lineOffset:offset), by, bz);
                //log.warning("filteringID = " + FilteringID);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, place item in its inventory.
                        ContainerBlock tc = (ContainerBlock) targetBlock.getState();
                        tinv = tc.getInventory();
                        if (OverridingID < 0) {
                            try {
                                ItemStack backup = currentStack;
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else {
                            try {
                                currentStack.setTypeId(OverridingID);
                                currentStack.setDurability(OverridingData);
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    } else { //any block other than a dispenser in target zone, activate the gadget and place a block there.

                        if (OverridingID > -1) {
                            if (line){
                                if (offset < 1){
                                    for(int i = 0; i < lineOffset; i++){
                                        w.getBlockAt(targetBlock.getX() - i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                } else {
                                    for(int i = 0; i < offset; i++){
                                        w.getBlockAt(targetBlock.getX() + i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                }
                            } else {
                                targetBlock.setTypeIdAndData(OverridingID, OverridingData, true);
                            }
                        } else {
                            if (currentStack.getTypeId() < 256) {
                                if (line) {
                                    if (offset < 1) {
                                        for (int i = 0; i < lineOffset; i++) {
                                            w.getBlockAt(targetBlock.getX() - i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    } else {
                                        for (int i = 0; i < offset; i++) {
                                            w.getBlockAt(targetBlock.getX() + i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    }
                                } else {
                                    targetBlock.setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                }

                            } else {
                                toolUse(targetBlock, thisBlock,currentStack.getTypeId(), currentStack.getDurability());
                            }
                        }
                    }

                    if (line){
                        infinite = true;
                    }

                    if (!infinite) {
                        inv = bc.getInventory();
                        currentStack = inv.getItem(inv.first(currentStack.getTypeId()));
                        if (currentStack.getAmount() > 1) {
                            currentStack.setAmount(currentStack.getAmount() - 1);
                        } else {
                            inv.removeItem(currentStack);
                        }
                    }
                }
            } else if (thisBlock.getTypeId() == TakeBlockID) { //diamond block by default, destroyer block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx - 1 - modifier), by, bz).getTypeId() == 35) {
                    if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx - 2 - modifier, by, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx - 2 - modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx - 2 - modifier, by, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx - 2 - modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    } else if (w.getBlockAt((bx - 1 - modifier), by, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx + 1 + (line?lineOffset:offset), by, bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (line) {
                        infinite = true;
                    }
                    if (!infinite) { //then either place the overriding block into the dispenser, or whatever it sucked up if no override
                        inv = bc.getInventory();
                        if (OverridingID > -1 && OverridingID != 0) {
                            try {
                                ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else if (targetBlock.getTypeId() != 0) { //game does not like stacks of air being placed in inventories...
                            try {
                                ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    }

                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, do nothing
                    } else {
                        if (line) {
                            if (offset < 1) {
                                for (int i = 0; i < lineOffset; i++) {
                                    w.getBlockAt(targetBlock.getX() - i, targetBlock.getY(), targetBlock.getZ()).setTypeId(0);
                                }
                            } else {
                                for (int i = 0; i < offset; i++) {
                                    w.getBlockAt(targetBlock.getX() + i, targetBlock.getY(), targetBlock.getZ()).setTypeId(0);
                                }
                            }
                        } else {
                            targetBlock.setTypeId(0); //destroy it.
                        }
                    }
                }
            }
            //******************From here up, Main First Example.  Stuff between Asterisks is just copied and pasted for rest of code **********************

            thisBlock = w.getBlockAt(bx + 1, by, bz);

            counter = 0;
            if (thisBlock.getTypeId() == GiveBlockID && event.getItem().getTypeId() != 23){ //iron by default, placement / giver block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx + 1 + modifier), by, bz).getTypeId() == 35 && counter < 64) {
                counter++;
                    //options for different special blocks, in order of how often I think they would probably be used (for practical speed)
                    if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx + 2 + modifier, by, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx + 2 + modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx + 2 + modifier, by, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx + 2 + modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx - 1 - (line?lineOffset:offset), by, bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, place item in its inventory.
                        ContainerBlock tc = (ContainerBlock) targetBlock.getState();
                        tinv = tc.getInventory();
                        if (OverridingID < 0) {
                            try {
                                ItemStack backup = currentStack;
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else {
                            try {
                                currentStack.setTypeId(OverridingID);
                                currentStack.setDurability(OverridingData);
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    } else { //any block other than a dispenser in target zone, activate the gadget and place a block there.
                        if (OverridingID > -1) {
                            if (line){
                                if (offset < 1){
                                    for(int i = 0; i < lineOffset; i++){
                                        w.getBlockAt(targetBlock.getX() + i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                } else {
                                    for(int i = 0; i < offset; i++){
                                        w.getBlockAt(targetBlock.getX() - i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                }
                            } else {
                                targetBlock.setTypeIdAndData(OverridingID, OverridingData, true);
                            }
                        } else {
                            if (currentStack.getTypeId() < 256) {
                                if (line) {
                                    if (offset < 1) {
                                        for (int i = 0; i < lineOffset; i++) {
                                            // log.warning("himom"); //Ridge said there were 42K of these in the log... - Gilt
                                            w.getBlockAt(targetBlock.getX() + i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    } else {
                                        for (int i = 0; i < offset; i++) {
                                            log.warning("hidad");
                                            w.getBlockAt(targetBlock.getX() - i, targetBlock.getY(), targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    }
                                } else {
                                    targetBlock.setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                }

                            } else {
                                toolUse(targetBlock, thisBlock,currentStack.getTypeId(), currentStack.getDurability());
                            }
                        }
                    }

                    if (line){
                        infinite = true;
                    }


                    if (!infinite) {
                        inv = bc.getInventory();
                        currentStack = inv.getItem(inv.first(currentStack.getTypeId()));
                        if (currentStack.getAmount() > 1) {
                            currentStack.setAmount(currentStack.getAmount() - 1);
                        } else {
                            inv.removeItem(currentStack);
                        }
                    }

                }
            } else if (thisBlock.getTypeId() == TakeBlockID) { //diamond block by default, destroyer block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx + 1 + modifier), by, bz).getTypeId() == 35) {
                    if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx + 2 + modifier, by, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx + 2 + modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx + 2 + modifier, by, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx + 2 + modifier, by, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx + 1 + modifier), by, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx - 1 - (line?lineOffset:offset), by, bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (line) {
                        infinite = true;
                    }
                    if (!infinite) { //then either place the overriding block into the dispenser, or whatever it sucked up if no override
                        inv = bc.getInventory();
                        if (OverridingID > -1 && OverridingID != 0) {
                            try {
                                ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else if (targetBlock.getTypeId() != 0) { //game does not like stacks of air being placed in inventories...
                            try {
                                ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    }

                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, do nothing
                    } else {
                        if (line) {
                            if (offset < 1) {
                                for (int i = 0; i < lineOffset; i++) {
                                    w.getBlockAt(targetBlock.getX() + i, targetBlock.getY(), targetBlock.getZ()).setTypeId(0);
                                }
                            } else {
                                for (int i = 0; i < offset; i++) {
                                    w.getBlockAt(targetBlock.getX() - i, targetBlock.getY(), targetBlock.getZ()).setTypeId(0);
                                }
                            }
                        } else {
                            targetBlock.setTypeId(0); //destroy it.
                        }
                    }
                }
            }


            counter = 0;
            thisBlock = w.getBlockAt(bx, by, bz - 1);
            if (thisBlock.getTypeId() == GiveBlockID && event.getItem().getTypeId() != 23) { //iron by default, placement / giver block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by, bz - 1 - modifier).getTypeId() == 35 && counter < 64) {
                counter++;
                    //options for different special blocks, in order of how often I think they would probably be used (for practical speed)
                    if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by, bz - 2 - modifier).getTypeId();
                        OverridingData = w.getBlockAt(bx, by, bz - 2 - modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by, bz - 2 - modifier).getTypeId();
                        FilteringData = w.getBlockAt(bx, by, bz - 2 - modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by, bz + 1 + (line?lineOffset:offset));
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, place item in its inventory.
                        ContainerBlock tc = (ContainerBlock) targetBlock.getState();
                        tinv = tc.getInventory();
                        if (OverridingID < 0) {
                            try {
                                ItemStack backup = currentStack;
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else {
                            try {
                                currentStack.setTypeId(OverridingID);
                                currentStack.setDurability(OverridingData);
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    } else { //any block other than a dispenser in target zone, activate the gadget and place a block there.
                        if (OverridingID > -1) {
                            if (line){
                                if (offset < 1){
                                    for(int i = 0; i < lineOffset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() - i).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                } else {
                                    for(int i = 0; i < offset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() + i).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                }
                            } else {
                                targetBlock.setTypeIdAndData(OverridingID, OverridingData, true);
                            }
                        } else {
                            if (currentStack.getTypeId() < 256) {
                                if (line) {
                                    if (offset < 1) {
                                        for (int i = 0; i < lineOffset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() - i).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    } else {
                                        for (int i = 0; i < offset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() + i).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    }
                                } else {
                                    targetBlock.setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                }

                            } else {
                                toolUse(targetBlock, thisBlock,currentStack.getTypeId(), currentStack.getDurability());
                            }
                        }
                    }

                    if (line){
                        infinite = true;
                    }


                    if (!infinite) {
                        inv = bc.getInventory();
                        currentStack = inv.getItem(inv.first(currentStack.getTypeId()));
                        if (currentStack.getAmount() > 1) {
                            currentStack.setAmount(currentStack.getAmount() - 1);
                        } else {
                            inv.removeItem(currentStack);
                        }
                    }

                }
            } else if (thisBlock.getTypeId() == TakeBlockID) { //diamond block by default, destroyer block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by, bz - 1 - modifier).getTypeId() == 35) {
                    if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by, - 1 - modifier).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by, bz - 2 - modifier).getTypeId();
                        OverridingData = w.getBlockAt(bx, by, bz - 2 - modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by, bz - 2 - modifier).getTypeId();
                        FilteringData = w.getBlockAt(bx, by, bz - 2 - modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by, bz - 1 - modifier).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by, bz + 1 + (line?lineOffset:offset));
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (line) {
                        infinite = true;
                    }
                    if (!infinite) { //then either place the overriding block into the dispenser, or whatever it sucked up if no override
                        inv = bc.getInventory();
                        if (OverridingID > -1 && OverridingID != 0) {
                            try {
                                ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else if (targetBlock.getTypeId() != 0) { //game does not like stacks of air being placed in inventories...
                            try {
                                ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    }

                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, do nothing
                    } else {
                       if (line) {
                            if (offset < 1) {
                                for (int i = 0; i < lineOffset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() - i).setTypeId(0);
                                }
                            } else {
                                for (int i = 0; i < offset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() + i).setTypeId(0);
                                }
                            }
                        } else {
                            targetBlock.setTypeId(0); //destroy it.
                        }
                    }
                }
            }


            counter = 0;
            thisBlock = w.getBlockAt(bx, by, bz + 1);
            if (thisBlock.getTypeId() == GiveBlockID && event.getItem().getTypeId() != 23) { //iron by default, placement / giver block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by, bz + 1 + modifier).getTypeId() == 35 && counter < 64) {
                counter++;
                    //options for different special blocks, in order of how often I think they would probably be used (for practical speed)
                    if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by, bz + 2 + modifier).getTypeId();
                        OverridingData = w.getBlockAt(bx, by, bz + 2 + modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by, bz + 2 + modifier).getTypeId();
                        FilteringData = w.getBlockAt(bx, by, bz + 2 + modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by, bz - 1 - (line?lineOffset:offset));
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, place item in its inventory.
                        ContainerBlock tc = (ContainerBlock) targetBlock.getState();
                        tinv = tc.getInventory();
                        if (OverridingID < 0) {
                            try {
                                ItemStack backup = currentStack;
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else {
                            try {
                                currentStack.setTypeId(OverridingID);
                                currentStack.setDurability(OverridingData);
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    } else { //any block other than a dispenser in target zone, activate the gadget and place a block there.
                        if (OverridingID > -1) {
                            if (line){
                                if (offset < 1){
                                    for(int i = 0; i < lineOffset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() + i).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                } else {
                                    for(int i = 0; i < offset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() - i).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                }
                            } else {
                                targetBlock.setTypeIdAndData(OverridingID, OverridingData, true);
                            }
                        } else {
                            if (currentStack.getTypeId() < 256) {
                                if (line) {
                                    if (offset < 1) {
                                        for (int i = 0; i < lineOffset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() + i).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    } else {
                                        for (int i = 0; i < offset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() - i).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    }
                                } else {
                                    targetBlock.setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                }

                            } else {
                                toolUse(targetBlock, thisBlock,currentStack.getTypeId(), currentStack.getDurability());
                            }
                        }
                    }

                    if (line){
                        infinite = true;
                    }

                    if (!infinite) {
                        inv = bc.getInventory();
                        currentStack = inv.getItem(inv.first(currentStack.getTypeId()));
                        if (currentStack.getAmount() > 1) {
                            currentStack.setAmount(currentStack.getAmount() - 1);
                        } else {
                            inv.removeItem(currentStack);
                        }
                    }

                }
            } else if (thisBlock.getTypeId() == TakeBlockID) { //diamond block by default, destroyer block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by, bz + 1 + modifier).getTypeId() == 35) {
                    if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by, +1 + modifier).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by, bz + 2 + modifier).getTypeId();
                        OverridingData = w.getBlockAt(bx, by, bz + 2 + modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by, bz + 2 + modifier).getTypeId();
                        FilteringData = w.getBlockAt(bx, by, bz + 2 + modifier).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by, bz + 1 + modifier).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by, bz - 1 - (line?lineOffset:offset));
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (line) {
                        infinite = true;
                    }
                    if (!infinite) { //then either place the overriding block into the dispenser, or whatever it sucked up if no override
                        inv = bc.getInventory();
                        if (OverridingID > -1 && OverridingID != 0) {
                            try {
                                ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else if (targetBlock.getTypeId() != 0) { //game does not like stacks of air being placed in inventories...
                            try {
                                ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    }

                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, do nothing
                    } else {
                        if (line) {
                            if (offset < 1) {
                                for (int i = 0; i < lineOffset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() + i).setTypeId(0);
                                }
                            } else {
                                for (int i = 0; i < offset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() - i).setTypeId(0);
                                }
                            }
                        } else {
                            targetBlock.setTypeId(0); //destroy it.
                        }
                    }
                }
            }

            counter = 0;
            thisBlock = w.getBlockAt(bx, by - 1, bz);
            if (thisBlock.getTypeId() == GiveBlockID && event.getItem().getTypeId() != 23) { //iron by default, placement / giver block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by - 1 - modifier, bz).getTypeId() == 35 && counter < 64) {
                counter++;
                    //options for different special blocks, in order of how often I think they would probably be used (for practical speed)
                    if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by - 2 - modifier, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx, by - 2 - modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by - 2 - modifier, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx, by - 2 - modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by + 1 + (line?lineOffset:offset), bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, place item in its inventory.
                        ContainerBlock tc = (ContainerBlock) targetBlock.getState();
                        tinv = tc.getInventory();
                        if (OverridingID < 0) {
                            try {
                                ItemStack backup = currentStack;
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else {
                            try {
                                currentStack.setTypeId(OverridingID);
                                currentStack.setDurability(OverridingData);
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    } else { //any block other than a dispenser in target zone, activate the gadget and place a block there.
                        if (OverridingID > -1) {
                            if (line){
                                if (offset < 1){
                                    for(int i = 0; i < lineOffset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY() - i, targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                } else {
                                    for(int i = 0; i < offset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY() + i, targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                }
                            } else {
                                targetBlock.setTypeIdAndData(OverridingID, OverridingData, true);
                            }
                        } else {
                            if (currentStack.getTypeId() < 256) {
                                if (line) {
                                    if (offset < 1) {
                                        for (int i = 0; i < lineOffset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY() - i, targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    } else {
                                        for (int i = 0; i < offset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY() + i, targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    }
                                } else {
                                    targetBlock.setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                }

                            } else {
                                toolUse(targetBlock, thisBlock,currentStack.getTypeId(), currentStack.getDurability());
                            }
                        }
                    }

                    if (line){
                        infinite = true;
                    }

                    if (!infinite) {
                        inv = bc.getInventory();
                        currentStack = inv.getItem(inv.first(currentStack.getTypeId()));
                        if (currentStack.getAmount() > 1) {
                            currentStack.setAmount(currentStack.getAmount() - 1);
                        } else {
                            inv.removeItem(currentStack);
                        }
                    }
                }
            } else if (thisBlock.getTypeId() == TakeBlockID) { //diamond block by default, destroyer block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by - 1 - modifier, bz).getTypeId() == 35) {
                    if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by - 2 - modifier, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx, by - 2 - modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by - 2 - modifier, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx, by - 2 - modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by - 1 - modifier, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by  - 1 - modifier, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by + 1 + (line?lineOffset:offset), bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (line) {
                        infinite = true;
                    }
                    if (!infinite) { //then either place the overriding block into the dispenser, or whatever it sucked up if no override
                        inv = bc.getInventory();
                        if (OverridingID > -1 && OverridingID != 0) {
                            try {
                                ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else if (targetBlock.getTypeId() != 0) { //game does not like stacks of air being placed in inventories...
                            try {
                                ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    }

                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, do nothing
                    } else {
                        if (line) {
                            if (offset < 1) {
                                for (int i = 0; i < lineOffset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY() - i, targetBlock.getZ()).setTypeId(0);
                                }
                            } else {
                                for (int i = 0; i < offset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY() + i, targetBlock.getZ()).setTypeId(0);
                                }
                            }
                        } else {
                            targetBlock.setTypeId(0); //destroy it.
                        }
                    }
                }
            }


            counter = 0;
            thisBlock = w.getBlockAt(bx, by + 1, bz);
            if (thisBlock.getTypeId() == GiveBlockID && event.getItem().getTypeId() != 23) { //iron by default, placement / giver block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by + 1 + modifier, bz).getTypeId() == 35 && counter < 64) {
                counter++;
                    //options for different special blocks, in order of how often I think they would probably be used (for practical speed)
                    if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by + 2 + modifier, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx, by + 2 + modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by + 2 + modifier, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx, by + 2 + modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by - 1 - (line?lineOffset:offset), bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, place item in its inventory.
                        ContainerBlock tc = (ContainerBlock) targetBlock.getState();
                        tinv = tc.getInventory();
                        if (OverridingID < 0) {
                            try {
                                ItemStack backup = currentStack;
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else {
                            try {
                                currentStack.setTypeId(OverridingID);
                                currentStack.setDurability(OverridingData);
                                currentStack.setAmount(1);
                                tinv.setItem((tinv.firstEmpty()), (currentStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    tinv.addItem(currentStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    } else { //any block other than a dispenser in target zone, activate the gadget and place a block there.
                        if (OverridingID > -1) {
                            if (line){
                                if (offset < 1){
                                    for(int i = 0; i < lineOffset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY() + i, targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                } else {
                                    for(int i = 0; i < offset; i++){
                                        w.getBlockAt(targetBlock.getX(), targetBlock.getY() - i, targetBlock.getZ()).setTypeIdAndData(OverridingID, OverridingData, true);
                                    }
                                }
                            } else {
                                targetBlock.setTypeIdAndData(OverridingID, OverridingData, true);
                            }
                        } else {
                            if (currentStack.getTypeId() < 256) {
                                if (line) {
                                    if (offset < 1) {
                                        for (int i = 0; i < lineOffset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY() + i, targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    } else {
                                        for (int i = 0; i < offset; i++) {
                                            w.getBlockAt(targetBlock.getX(), targetBlock.getY() - i, targetBlock.getZ()).setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                        }
                                    }
                                } else {
                                    targetBlock.setTypeIdAndData(currentStack.getTypeId(), (byte) currentStack.getDurability(), true);
                                }

                            } else {
                                toolUse(targetBlock, thisBlock,currentStack.getTypeId(), currentStack.getDurability());
                            }
                        }
                    }

                    if (line){
                        infinite = true;
                    }

                    if (!infinite) {
                        inv = bc.getInventory();
                        currentStack = inv.getItem(inv.first(currentStack.getTypeId()));
                        if (currentStack.getAmount() > 1) {
                            currentStack.setAmount(currentStack.getAmount() - 1);
                        } else {
                            inv.removeItem(currentStack);
                        }
                    }
                }
            } else if (thisBlock.getTypeId() == TakeBlockID) { //diamond block by default, destroyer block
                int modifier = 1;
                int offset = 0;
                ContainerBlock bc = (ContainerBlock) event.getBlock().getState();
                event.setCancelled(true);

                while (w.getBlockAt((bx), by + 1 + modifier, bz).getTypeId() == 35) {
                    if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OffsetOneData) {
                        offset++;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == FiniteData) {
                        infinite = false;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OverrideData) {
                        OverridingID = w.getBlockAt(bx, by + 2 + modifier, bz).getTypeId();
                        OverridingData = w.getBlockAt(bx, by + 2 + modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OffsetFiveData) {
                        offset = offset + 5;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == FilterData) {
                        FilteringID = w.getBlockAt(bx, by + 2 + modifier, bz).getTypeId();
                        FilteringData = w.getBlockAt(bx, by + 2 + modifier, bz).getData();
                        modifier++;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == OffsetTwentyFiveData) {
                        offset = offset + 25;
                    } else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == SkipData) { // will cause the program to look for the next special block in the line 2 away from here instead of 1.  Allows you to put pistons and things in the middle of the offsets, pass wires through, etc.
                        modifier++;
                    }else if (w.getBlockAt((bx), by + 1 + modifier, bz).getData() == LineData) {
                        line = true;
                        lineOffset = offset;
                        offset = 0;
                    }
                    modifier++;
                }

                if (line && ((lineOffset > 100) || (offset > 100))){
                    return;
                }

                Block targetBlock = w.getBlockAt(bx, by - 1 - (line?lineOffset:offset), bz);
                if (FilteringID == -1 || (targetBlock.getTypeId() == FilteringID && targetBlock.getData() == FilteringData)) {
                    if (line) {
                        infinite = true;
                    }
                    if (!infinite) { //then either place the overriding block into the dispenser, or whatever it sucked up if no override
                        inv = bc.getInventory();
                        if (OverridingID > -1 && OverridingID != 0) {
                            try {
                                ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(OverridingID, 1, OverridingData);
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        } else if (targetBlock.getTypeId() != 0) { //game does not like stacks of air being placed in inventories...
                            try {
                                ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                inv.setItem((inv.firstEmpty()), (newStack));

                            } catch (Exception e) { //no stack with that type of item exists
                                try {
                                    ItemStack newStack = new ItemStack(targetBlock.getTypeId(), 1, targetBlock.getData());
                                    inv.addItem(newStack);
                                } catch (Exception f) { //no room in inventory, either
                                    //do nothing
                                }
                            }
                        }
                    }

                    if (targetBlock.getTypeId() == 23 || targetBlock.getTypeId() == 54) { //if another dispenser or chest, do nothing
                    } else {
                        if (line) {
                            if (offset < 1) {
                                for (int i = 0; i < lineOffset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY() + i, targetBlock.getZ()).setTypeId(0);
                                }
                            } else {
                                for (int i = 0; i < offset; i++) {
                                    w.getBlockAt(targetBlock.getX(), targetBlock.getY() - i, targetBlock.getZ()).setTypeId(0);
                                }
                            }
                        } else {
                            targetBlock.setTypeId(0); //destroy it.
                        }
                    }
                }
            }
        } catch (Exception ee) {
            //generic exceptions not caught above
            log.warning("[VoxelGadget] generic exception in main body of code.");
        }
    }

    public void saveConfig() {
        try {
            File f = new File("plugins/VoxelGadget/gadgetconfig.txt");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                PrintWriter pw = new PrintWriter(f);
                pw.write("GiveBlockID:" + 42 + "\r\n");  //iron block
                pw.write("TakeBlockID:" + 57 + "\r\n");     //diamond block
                pw.write("OffsetOneData:" + 9 + "\r\n");   //cyan
                pw.write("OffsetFiveData:" + 11 + "\r\n");  //blue
                pw.write("OffsetTwentyFiveData:" + 3 + "\r\n"); //blue fleur
                pw.write("OverrideData:" + 14 + "\r\n");  //red
                pw.write("FiniteData:" + 13 + "\r\n");  //green
                pw.write("SkipData:" + 0 + "\r\n");  //white
                pw.write("LineData:" + 2 + "\r\n");  //wine
                pw.write("FilterData:" + 4 + "\r\n");  //white
                pw.write("CollectID:" + 41 + "\r\n");  //white

                pw.close();
                log.info("[VoxelGadget] Config saved");
                loadConfig();
            }
        } catch (Exception e) {
            log.warning("[VoxelGadget] Error while saving gadgetconfig.txt");
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            File f = new File("plugins/VoxelGadget/gadgetconfig.txt");
            if (f.exists()) {
                Scanner snr = new Scanner(f);
                int type = 0;

                GiveBlockID = Integer.parseInt(snr.nextLine().split(":")[1]);
                TakeBlockID = Integer.parseInt(snr.nextLine().split(":")[1]);
                OffsetOneData = Byte.parseByte(snr.nextLine().split(":")[1]);
                OffsetFiveData = Byte.parseByte(snr.nextLine().split(":")[1]);
                OffsetTwentyFiveData = Byte.parseByte(snr.nextLine().split(":")[1]);
                OverrideData = Byte.parseByte(snr.nextLine().split(":")[1]);
                FiniteData = Byte.parseByte(snr.nextLine().split(":")[1]);
                SkipData = Byte.parseByte(snr.nextLine().split(":")[1]);
                LineData = Byte.parseByte(snr.nextLine().split(":")[1]);
                FilterData = Byte.parseByte(snr.nextLine().split(":")[1]);
                CollectID = Integer.parseInt(snr.nextLine().split(":")[1]);

                snr.close();
                log.info("[VoxelGadget] Config loaded");
            } else {
                saveConfig();
            }
        } catch (Exception e) {
            log.warning("[VoxelGadget] Error while loading gadgetconfig.txt");
            e.printStackTrace();
        }
    }

    
}
