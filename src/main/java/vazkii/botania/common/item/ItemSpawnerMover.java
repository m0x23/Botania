/**
 * This class was created by <PowerCrystals>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [? (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.Botania;
import vazkii.botania.common.advancements.UseItemSuccessTrigger;
import vazkii.botania.common.lib.LibItemNames;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSpawnerMover extends ItemMod {

	private static final String TAG_SPAWNER = "spawner";
	private static final String TAG_SPAWN_DATA = "SpawnData";
	private static final String TAG_ID = "id";

	public ItemSpawnerMover(Properties props) {
		super(props);
		addPropertyOverride(new ResourceLocation("botania", "full"), (stack, worldIn, entityIn) -> hasData(stack) ? 1 : 0);
	}

	@Nullable
	private static NBTTagCompound getSpawnerTag(ItemStack stack) {
		NBTTagCompound tag = stack.getTag();
		if(tag != null && tag.contains(TAG_SPAWNER)) {
			return tag.getCompound(TAG_SPAWNER);
		}

		return null;
	}

	@Nullable
	private static ResourceLocation getEntityId(ItemStack stack) {
		NBTTagCompound tag = getSpawnerTag(stack);
		if(tag != null && tag.contains(TAG_SPAWN_DATA)) {
			tag = tag.getCompound(TAG_SPAWN_DATA);
			if(tag.contains(TAG_ID)) {
				return ResourceLocation.makeResourceLocation(tag.getString(TAG_ID));
			}
		}

		return null;
	}

	private static boolean hasData(ItemStack stack) {
		return getEntityId(stack) != null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag flags) {
		ResourceLocation id = getEntityId(stack);
		if (id != null) {
			EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
			if (type != null) {
				infoList.add(type.func_212546_e());
			}
		}
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx) {
		if(getEntityId(ctx.getItem()) == null) {
			return captureSpawner(ctx) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
		} else {
			return placeSpawner(ctx);
		}
	}

	private EnumActionResult placeSpawner(ItemUseContext ctx) {
		ItemStack spawner = new ItemStack(Blocks.SPAWNER);
		ItemUseContext placeCtx = new BlockItemUseContext(ctx.getWorld(), ctx.getPlayer(), spawner, ctx.getPos(), ctx.getFace(), ctx.getHitX(), ctx.getHitY(), ctx.getHitZ());
		EnumActionResult res = spawner.onItemUse(placeCtx);

		if(res == EnumActionResult.SUCCESS) {
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();
			ItemStack mover = ctx.getItem();

			if(!world.isRemote) {
				TileEntity te = world.getTileEntity(pos);
				NBTTagCompound tag = mover.getTag();
				if (te instanceof TileEntityMobSpawner && tag.contains(TAG_SPAWNER)) {
					tag = tag.getCompound(TAG_SPAWNER);
					tag.putInt("x", pos.getX());
					tag.putInt("y", pos.getY());
					tag.putInt("z", pos.getZ());
					te.read(tag);
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
				}

				if(ctx.getPlayer() != null)
					ctx.getPlayer().renderBrokenItemStack(mover);
				mover.shrink(1);
			} else {
				for(int i = 0; i < 100; i++)
					Botania.proxy.sparkleFX(pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.45F + 0.2F * (float) Math.random(), 6);
			}
		}

		return res;
	}

	private boolean captureSpawner(ItemUseContext ctx) {
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		ItemStack stack = ctx.getItem();
		EntityPlayer player = ctx.getPlayer();

		if(world.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
			if(!world.isRemote) {
				TileEntity te = world.getTileEntity(pos);
				NBTTagCompound tag = new NBTTagCompound();
				tag.put(TAG_SPAWNER, te.write(new NBTTagCompound()));
				stack.setTag(tag);
				world.destroyBlock(pos, false);
				if(player != null) {
					player.getCooldownTracker().setCooldown(this, 20);
					UseItemSuccessTrigger.INSTANCE.trigger((EntityPlayerMP) player, stack, (WorldServer) world, pos.getX(), pos.getY(), pos.getZ());
					player.renderBrokenItemStack(stack);
				}
			} else {
				for(int i = 0; i < 50; i++) {
					float red = (float) Math.random();
					float green = (float) Math.random();
					float blue = (float) Math.random();
					Botania.proxy.wispFX(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, red, green, blue, (float) Math.random() * 0.1F + 0.05F, (float) (Math.random() - 0.5F) * 0.15F, (float) (Math.random() - 0.5F) * 0.15F, (float) (Math.random() - 0.5F) * 0.15F);
				}
			}
			return true;
		} else return false;
	}
}
