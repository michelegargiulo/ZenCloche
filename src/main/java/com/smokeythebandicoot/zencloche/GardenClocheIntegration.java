package com.smokeythebandicoot.zencloche;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.item.MCItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Arrays;

@ModOnly(value="immersiveengineering")
@ZenClass(value="mods.smokeythebandicoot.zencloche.GardenCloche")
@ZenRegister
public class GardenClocheIntegration {
    @ZenMethod
    @ZenDoc(value="Register the given ingredient as fertilizer with the given growth multiplier. <br>Will throw an exeception for ingredients such as <*> that can't be resolved to a list of items.")
    public static void registerItemFertilizer(IIngredient fertilizer, float multiplier) {
        IItemStack[] items = fertilizer.getItemArray();
        if (items == null) {
            throw new IllegalArgumentException("unsupported ingredient: " + fertilizer.toCommandString());
        }
        for (IItemStack item : items) {
            BelljarHandler.registerBasicItemFertilizer(CraftTweakerMC.getItemStack(item), multiplier);
        }
    }

    @ZenMethod
    @ZenDoc(value="Register the given fluid as fertilizer with the given growth multiplier. ")
    public static void registerFluidFertilizer(final ILiquidStack fluid, final float multiplier) {
        BelljarHandler.registerFluidFertilizer(new BelljarHandler.FluidFertilizerHandler(){

            public boolean isValid(FluidStack fertilizer) {
                return fertilizer.getFluid().equals(CraftTweakerMC.getLiquidStack(fluid).getFluid());
            }

            public float getGrowthMultiplier(FluidStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile) {
                return multiplier;
            }
        });
    }

    @ZenMethod
    @ZenDoc(value="Register the given seed so it can be grown in the Graden Cloche. <br>Specifying a list of drops, the needed soil and optionally a block to display visually inside the cloche. <br>If no soil is specified, dirt is used instead. <br>If no display block is specified, the seed is used as block. <br>This may result in texture errors if the item can't be a block.")
    public static void registerCrop(IItemStack seed, IItemStack[] drops, @Optional IIngredient soil, @Optional IBlock display) {
        Block block = display != null ? CraftTweakerMC.getBlock(display) : CraftTweakerMC.getBlock(seed);
        ItemStack[] output = Arrays.stream(drops).map(CraftTweakerMC::getItemStack).toArray(ItemStack[]::new);
        IngredientStack soilStack = soil != null ? ImmersiveAdapter.ingredient(soil) : new IngredientStack(new ItemStack(Blocks.DIRT));
        BelljarHandler.cropHandler.register(CraftTweakerMC.getItemStack(seed), output, soilStack, block.getDefaultState());
    }
}
