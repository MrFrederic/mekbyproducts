package com.cfa.mekbyproducts.tile;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;

/** Implemented by our replacement crusher and crushing factories. GUI/container mixins use it to spot them. */
public interface ByproductMachine {

    int CRUSHER_SLOTS = 4;
    int FACTORY_SLOTS = 6;

    List<IInventorySlot> getByproductSlots();

    /**
     * Re-registers the item side config so byproduct slots count as outputs (pipes can pull them and the ejector pushes them). {@code ConfigInfo.addSlotInfo} is
     * a map put, so this replaces what the base class registered.
     */
    static void registerOutputs(TileComponentConfig config, List<IInventorySlot> inputs, List<IInventorySlot> outputs) {
        ConfigInfo itemConfig = config.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputs));
            List<IInventorySlot> io = new ArrayList<>(inputs);
            io.addAll(outputs);
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, io));
        }
    }
}
