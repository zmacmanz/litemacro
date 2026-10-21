package com.alfre.macrobuilder.mixin;

import com.alfre.macrobuilder.MacroBuilderClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
abstract class ContainerInputRecorderMixin {
   @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
   private void litemacro$recordContainerInput(int containerId, int slotId, int button, ClickType input, Player player, CallbackInfo callback) {
      if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
         return;
      }

      if (player != null && player.containerMenu != null) {
         MacroBuilderClient.recordContainerInput(slotId, button, input.name(), player.containerMenu.slots.size(), player.containerMenu.getClass().getName());
      }
   }
}
