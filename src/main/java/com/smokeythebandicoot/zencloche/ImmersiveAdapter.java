package com.smokeythebandicoot.zencloche;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;

import java.util.List;
import java.util.stream.Collectors;

public interface ImmersiveAdapter {
    public static IngredientStack ingredient(IIngredient ingredient) {
        if (ingredient instanceof IItemStack) {
            return new IngredientStack(CraftTweakerMC.getItemStack((IItemStack)((IItemStack)ingredient)));
        }
        if (ingredient instanceof ILiquidStack) {
            return new IngredientStack(CraftTweakerMC.getLiquidStack((ILiquidStack)((ILiquidStack)ingredient)));
        }
        if (ingredient instanceof IOreDictEntry) {
            IOreDictEntry entry = (IOreDictEntry)ingredient;
            return new IngredientStack(entry.getName());
        }
        List<IItemStack> items = ingredient.getItems();
        if (items == null) {
            throw new IllegalArgumentException("illegal ingredient: " + ingredient.toCommandString());
        }

        return new IngredientStack(items.stream().map(CraftTweakerMC::getItemStack).collect(Collectors.toList()));
    }
}
