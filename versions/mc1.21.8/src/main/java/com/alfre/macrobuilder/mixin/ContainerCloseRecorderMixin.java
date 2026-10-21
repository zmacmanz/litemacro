package com.alfre.macrobuilder.mixin;

import com.alfre.macrobuilder.MacroBuilderClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class ContainerCloseRecorderMixin {
   @Inject(method = "closeContainer", at = @At("HEAD"))
   private void litemacro$recordContainerClose(CallbackInfo callback) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      if (player.containerMenu != null) {
         MacroBuilderClient.recordContainerClose(player.containerMenu.slots.size(), player.containerMenu.getClass().getName());
      }
   }
}
