package com.thevoxelbox.voxelgadget.modifier;

import com.thevoxelbox.voxelgadget.Processor;

/**
 * A special Modifier to easily create multiple OffsetModifiers which all do the same thing with different values.
 */
public class OffsetModifier extends AbstractModifier {

    final private int offset;

    public OffsetModifier(int offset) {
        this.offset = offset;
    }

    @Override
    public int modify(Processor p) {
        p.addOffset(offset);
        if (p.getOffset() > 100) p.setOffset(100);
        else if (p.getOffset() < 1) p.setOffset(1);
        return 0;
    }

}
