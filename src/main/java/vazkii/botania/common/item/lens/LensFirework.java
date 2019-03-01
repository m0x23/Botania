/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [15/11/2015, 19:12:41 (GMT)]
 */
package vazkii.botania.common.item.lens;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.common.core.helper.ItemNBTHelper;

public class LensFirework extends Lens {

	@Override
	public boolean collideBurst(IManaBurst burst, EntityThrowable entity, RayTraceResult pos, boolean isManaBlock, boolean dead, ItemStack stack) {
		if(!entity.world.isRemote && !burst.isFake()) {
			BlockPos coords = burst.getBurstSourceBlockPos();
			if(pos.entity == null && !isManaBlock && !pos.getBlockPos().equals(coords)) {
				ItemStack fireworkStack = generateFirework(burst.getColor());

				EntityFireworkRocket rocket = new EntityFireworkRocket(entity.world, entity.posX, entity.posY, entity.posZ, fireworkStack);
				entity.world.spawnEntity(rocket);
			}
		} else dead = false;

		return dead;
	}

	private ItemStack generateFirework(int color) {
		ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
		NBTTagCompound explosion = new NBTTagCompound();
		explosion.putIntArray("Colors", new int[] { color });

		int type = 1;
		double rand = Math.random();
		if(rand > 0.25) {
			if(rand > 0.9)
				type = 2;
			else type = 0;
		}

		explosion.putInt("Type", type);

		if(Math.random() < 0.05)
			if(Math.random() < 0.5)
				explosion.putBoolean("Flicker", true);
			else explosion.putBoolean("Trail", true);

		ItemNBTHelper.setCompound(stack, "Explosion", explosion);

		NBTTagCompound fireworks = new NBTTagCompound();
		fireworks.putInt("Flight", (int) (Math.random() * 3 + 2));

		NBTTagList explosions = new NBTTagList();
		explosions.add(explosion);
		fireworks.put("Explosions", explosions);

		ItemNBTHelper.setCompound(stack, "Fireworks", fireworks);

		return stack;
	}

}
