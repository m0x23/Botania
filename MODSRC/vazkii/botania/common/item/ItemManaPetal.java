/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Feb 2, 2014, 12:57:22 PM (GMT)]
 */
package vazkii.botania.common.item;

import vazkii.botania.common.lib.LibItemIDs;
import vazkii.botania.common.lib.LibItemNames;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemManaPetal extends Item16Colors {

	public ItemManaPetal() {
		super(LibItemIDs.idManaPetal, LibItemNames.MANA_PETAL);
	}
	
	@Override
	public boolean hasEffect(ItemStack par1ItemStack, int pass) {
		return true;
	}
	
	@Override
	public Icon getIconFromDamage(int par1) {
		return ModItems.petal.getIconFromDamage(par1);
	}

}
