package net.mctoolbox.mixin.client;

import net.minecraft.client.gui.hud.debug.PieChart;
import net.minecraft.util.profiler.ProfileResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PieChart.class)
public interface PieChartAccessor {

	@Accessor("profileResult")
	ProfileResult getProfileResult();
}