package me.leo21.sneaky_names.mixin;

import me.leo21.sneaky_names.SneakyPlayerNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In 1.21 this cancelled EntityRenderer#render. Since the render-state rewrite, name tags are
 * gated by shouldShowName, so we veto it there instead.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class PlayerNamesRendering {
	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void sneaky_player_names$hideName(LivingEntity entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
		if (!(entity instanceof Player target)) return;

		LocalPlayer me = Minecraft.getInstance().player;
		if (me == null || target == me) return;

		// 0 = never show names, negative = unlimited distance
		int maxDistance = SneakyPlayerNames.CONFIG.MAX_DISTANCE;
		if (maxDistance == 0 || (maxDistance > 0 && me.distanceToSqr(target) > Math.pow(maxDistance, 2))) {
			cir.setReturnValue(false);
			return;
		}

		if (!SneakyPlayerNames.CONFIG.HIDE_PLAYER_NAMES) return;

		// Hide the name if a block is in the way between us and the other player's eyes
		ClipContext context = new ClipContext(
				me.getEyePosition(), target.getEyePosition(),
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, me);
		if (me.level().clip(context).getType() == HitResult.Type.BLOCK) {
			cir.setReturnValue(false);
		}
	}
}
