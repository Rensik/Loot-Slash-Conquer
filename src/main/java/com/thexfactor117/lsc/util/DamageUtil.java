package com.thexfactor117.lsc.util;

import com.thexfactor117.lsc.LootSlashConquer;
import com.thexfactor117.lsc.capabilities.implementation.LSCPlayerCapability;
import com.thexfactor117.lsc.util.misc.LSCDamageSource;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 *
 * @author TheXFactor117
 *
 */
public class DamageUtil
{
	/**
	 * Applies damage modifiers to the damage passed in. This adds additional damage based on
	 * stats like Strength, Dexterity, and Intelligence, where applicable.
	 * @param playerInfo
	 * @param damage
	 * @param type
	 * @return
	 */
	public static double applyDamageModifiers(LSCPlayerCapability cap, double damage, DamageType type)
	{
		switch (type)
		{
			case PHYSICAL_MELEE:
				return damage + cap.getPhysicalPower();
			case PHYSICAL_RANGED:
				return damage + cap.getRangedPower();
			case MAGICAL:
				return damage + cap.getMagicalPower();
			default:
				return damage;
		}
	}
	
	/**
	 * Applies critical damage to the passed in damage value. This effectively increases the damage
	 * of the attack based on the player's critical stats.
	 * @param stats
	 * @param damage
	 * @param nbt
	 * @return
	 */
	public static double applyCriticalModifier(LSCPlayerCapability cap, double damage)
	{
		double damageBeforeCrit = damage;
		
		if (cap.getCriticalChance() > 0)
		{
			if (Math.random() < cap.getCriticalChance())
			{
				damage = (cap.getCriticalDamage() * damageBeforeCrit) + damageBeforeCrit;
			}
		}
		
		return damage;
	}
	
	/**
	 * Applies custom armor reductions to the passed in damage value. This uses a custom algorithm separate from Vanilla's,
	 * allowing us to fully customize the damage algorithm and force all damage to use this instead of Vanilla's.
	 * @param damage
	 * @param player
	 * @param playerInfo
	 * @return
	 */
	public static double applyArmorReductions(double damage, EntityPlayer player, LSCPlayerCapability cap)
	{	
		LootSlashConquer.LOGGER.info("Total Armor: " + getTotalArmor(player, cap));
		return damage * (damage / (damage + getTotalArmor(player, cap)));
	}
	
	/**
	 * Applies elemental resistances to the damage passed in. Note, this method only handles elemental resistances for the player.
	 * @param damage
	 * @param source
	 * @param player
	 * @return
	 */
	public static double applyElementalResistance(double damage, LSCDamageSource source, LSCPlayerCapability cap)
	{
		double reducedDamage = damage;
		
		if (source.isFireDamage()) reducedDamage = damage * (damage / (damage + cap.getFireResistance()));
		else if (source.isFrostDamage()) reducedDamage = damage * (damage / (damage + cap.getFrostResistance()));
		else if (source.isLightningDamage()) reducedDamage = damage * (damage / (damage + cap.getLightningResistance()));
		else if (source.isPoisonDamage()) reducedDamage = damage * (damage / (damage + cap.getPoisonResistance()));
		
		return reducedDamage >= 0 ? reducedDamage : 0;
	}
	
	// TODO: if power is less than 1 (decimal), set to zero to prevent the decimal being added on as extra damage.
	// not too important but still a thing.
	//
	// Wtf was the written for???
	
	public static double getPhysicalResistance(LSCPlayerCapability cap)
	{
		return (Math.pow(1.05, cap.getPlayerLevel()) + cap.getTotalStrength()) * (0.85 * 0.8);
	}
	
	/**
	 * Returns the total number of Armor Points on all the equipped pieces of armor the player currently has.
	 * @param player
	 * @param playerInfo
	 * @return
	 */
	public static double getEquippedArmor(EntityPlayer player, LSCPlayerCapability cap)
	{
		double totalArmorPoints = 0;
		
		// checks total armor points
		for (ItemStack stack : player.getArmorInventoryList())
		{
			if (stack.getItem() instanceof ItemArmor)
			{
				if (ItemUtil.getItemLevel(stack) <= cap.getPlayerLevel())
				{
					totalArmorPoints += ItemUtil.getItemArmor(stack);
				}
			}
		}
		
		return totalArmorPoints;
	}
	
	/**
	 * Returns the total amount of Armor Points for a player, includding equipped Armor and Physical Resistance.
	 * @param player
	 * @param playerInfo
	 * @return
	 */
	public static double getTotalArmor(EntityPlayer player, LSCPlayerCapability cap)
	{
		return getEquippedArmor(player, cap) + getPhysicalResistance(cap);
	}
	
	public static enum DamageType
	{
		PHYSICAL_MELEE(),
		PHYSICAL_RANGED(),
		MAGICAL();
	}
}
