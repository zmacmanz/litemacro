package com.alfre.macrobuilder.mixin;

import com.alfre.macrobuilder.MacroBuilderClient;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
abstract class CreativeSlotClickRecorderMixin {
   @Inject(method = "slotClicked", at = @At("HEAD"))
   private void litemacro$recordCreativeSlotClick(Slot slot, int slotId, int button, ClickType input, CallbackInfo callback) {
      CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen)(Object)this;
      MacroBuilderClient.recordContainerInput(slotId, button, input.name(), screen.getMenu().slots.size(), screen.getMenu().getClass().getName());
   }
}
