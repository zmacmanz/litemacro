package com.alfre.macrobuilder.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeSlotClickInvoker {
   @Invoker("slotClicked")
   void litemacro$invokeSlotClicked(Slot slot, int slotId, int button, ClickType input);
}
