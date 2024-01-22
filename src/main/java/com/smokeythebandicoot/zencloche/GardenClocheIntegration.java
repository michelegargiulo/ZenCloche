package com.smokeythebandicoot.zencloche;

import blusunrize.immersiveengineering.api.ComparableItemStack;
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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@ModOnly(value="immersiveengineering")
@ZenClass(value="mods.smokeythebandicoot.zencloche.GardenCloche")
@ZenRegister
public class GardenClocheIntegration {

    private static Field BJHfluidFertilizersField = null;
    private static Field BJHitemFertilizersField = null;

    private static Field BJHseedSoilMapField = null;
    private static Field BJHseedOutputMapField = null;
    private static Field BJHseedRenderMapField = null;
    private static Field BJHplantHandlersField = null;

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
    @ZenDoc(value="Register the given fluid as fertilizer with the given growth multiplier.")
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
    @ZenDoc(value="Removes a registered fluid fertilizer.")
    public static void removeFluidFertilizer(final ILiquidStack fluid) {

        // Try to init the field
        if (BJHfluidFertilizersField == null) {
            BJHfluidFertilizersField = getBJHfluidFertilizersField();
        }

        // If the field is still null, abort and log the error
        if (BJHfluidFertilizersField == null) {
            ZenCloche.logger.error("Could not retrieve fluidFertilizer from the BellJarHandler class. Removing fluid fertilizers will not be available");
        }

        try {
            HashSet<BelljarHandler.FluidFertilizerHandler> fluidFertilizers = (HashSet<BelljarHandler.FluidFertilizerHandler>)BJHfluidFertilizersField.get(null);
            BelljarHandler.FluidFertilizerHandler fluidHandler = BelljarHandler.getFluidFertilizerHandler(CraftTweakerMC.getLiquidStack(fluid));
            if (fluidHandler == null) {
                ZenCloche.logger.warn("Could find fluid fertilizer: " + fluid.getName() + "; Skipping...");
                return;
            }
            fluidFertilizers.remove(fluidHandler);
            BJHfluidFertilizersField.set(null, fluidFertilizers);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @ZenMethod
    @ZenDoc(value="Removes all registered item fertilizers.")
    public static void removeAllItemFertilizers() {

        // Try to init the field
        if (BJHitemFertilizersField == null) {
            BJHitemFertilizersField = getBJHitemFertilizersField();
        }

        // If the field is still null, abort and log the error
        if (BJHitemFertilizersField == null) {
            ZenCloche.logger.error("Could not retrieve itemFertilizer from the BellJarHandler class. Could not clear all Item Fertilizers");
        }

        try {
            BJHitemFertilizersField.set(null, new HashSet<BelljarHandler.ItemFertilizerHandler>());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @ZenMethod
    @ZenDoc(value="Lists all registered fluid fertilizers.")
    public static List<String> listFluidFertilizers() {
        return FluidRegistry.getRegisteredFluids().values().stream().map(fluid -> {
            BelljarHandler.FluidFertilizerHandler ffh = BelljarHandler.getFluidFertilizerHandler(new FluidStack(fluid, 1000));
            return ffh == null ? null : fluid.getName();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @ZenMethod
    @ZenDoc(value="Lists all registered crops.")
    public static List<String> listCrops() {

        // Try to init the fields
        if (BJHseedOutputMapField == null) {
            BJHseedOutputMapField = getBJHseedOutputMapField();
        }
        if (BJHseedRenderMapField == null) {
            BJHseedRenderMapField = getBJHseedRenderMapField();
        }
        if (BJHseedSoilMapField == null) {
            BJHseedSoilMapField = getBJHseedSoilMapField();
        }
        if (BJHplantHandlersField == null) {
            BJHplantHandlersField = getBJHplantHandlersField();
        }

        // If the field is still null, abort and log the error
        if (BJHseedOutputMapField == null || BJHseedRenderMapField == null || BJHseedSoilMapField == null) {
            ZenCloche.logger.error("Could not retrieve plantHandlers, seedOutputMap, seedRenderMap or seedSoilMap fields from the BellJarHandler class. Could not list crops");
        }

        try {
            HashSet<BelljarHandler.IPlantHandler> plantHandlers = (HashSet<BelljarHandler.IPlantHandler>)BJHplantHandlersField.get(null);
            HashMap<ComparableItemStack, IngredientStack> seedSoilMap = (HashMap<ComparableItemStack, IngredientStack>)BJHseedSoilMapField.get(null);
            HashMap<ComparableItemStack, ItemStack[]> seedOutputMap = (HashMap<ComparableItemStack, ItemStack[]>)BJHseedOutputMapField.get(null);
            HashMap<ComparableItemStack, IBlockState[]> seedRenderMap = (HashMap<ComparableItemStack, IBlockState[]>)BJHseedRenderMapField.get(null);

            List<String> registeredCrops = new ArrayList<>();

            // Iterates over all Handlers (seed, stem, etc)
            for (BelljarHandler.IPlantHandler handler : plantHandlers) {
                Method getSeedSetMethod = handler.getClass().getDeclaredMethod("getSeedSet");
                getSeedSetMethod.setAccessible(true);
                HashSet<ComparableItemStack> validSeeds = (HashSet<ComparableItemStack>)getSeedSetMethod.invoke(handler);

                // Iterates over all the validSeeds
                for (ComparableItemStack validSeed : validSeeds) {
                    IngredientStack soil = seedSoilMap.get(validSeed);
                    ItemStack[] outputs = seedOutputMap.get(validSeed);
                    IBlockState[] display = seedRenderMap.get(validSeed);

                    // Build the string with all the valid seeds
                    String soilStr;
                    ItemStack soilStack = soil.stack;
                    if (soilStack.getItem().getRegistryName().equals("minecraft:air")) {
                        soilStr = soil.oreName == null ? "<IIngredient>" : "<ore:" + soil.oreName;
                    } else {
                        soilStr = soil.stack.getItem().getRegistryName() + ":" + soil.stack.getMetadata();
                    }

                    registeredCrops.add(new StringBuilder()
                            .append("§2")
                            .append(validSeed.stack.getItem().getRegistryName())
                            .append(":")
                            .append(validSeed.stack.getMetadata())
                            .append("§r - §3[")
                            .append(Arrays.stream(outputs).map(stack -> stack.getItem().getRegistryName() + ":" + stack.getMetadata()).collect(Collectors.joining(", ")))
                            .append("]§r - §4")
                            .append(soilStr)
                            .append("§r")
                            .toString());
                }
            }

            return registeredCrops;

        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return Collections.singletonList("An error occurred while performing the request: " + e.getMessage());
        }
    }

    private static Field getBJHfluidFertilizersField() {
        try {
            BJHfluidFertilizersField = BelljarHandler.class.getDeclaredField("fluidFertilizers");
        } catch (NoSuchFieldException e) {
            ZenCloche.logger.error(e.getMessage());
            return null;
        }
        BJHfluidFertilizersField.setAccessible(true);
        return BJHfluidFertilizersField;
    }

    private static Field getBJHitemFertilizersField() {
        try {
            BJHfluidFertilizersField = BelljarHandler.class.getDeclaredField("itemFertilizers");
        } catch (NoSuchFieldException e) {
            ZenCloche.logger.error(e.getMessage());
            return null;
        }
        BJHfluidFertilizersField.setAccessible(true);
        return BJHfluidFertilizersField;
    }

    private static Field getBJHseedSoilMapField() {
        try {
            BJHseedSoilMapField = BelljarHandler.class.getDeclaredField("seedSoilMap");
        } catch (NoSuchFieldException e) {
            ZenCloche.logger.error(e.getMessage());
            return null;
        }
        BJHseedSoilMapField.setAccessible(true);
        return BJHseedSoilMapField;
    }

    private static Field getBJHseedOutputMapField() {
        try {
            BJHseedOutputMapField = BelljarHandler.class.getDeclaredField("seedOutputMap");
        } catch (NoSuchFieldException e) {
            ZenCloche.logger.error(e.getMessage());
            return null;
        }
        BJHseedOutputMapField.setAccessible(true);
        return BJHseedOutputMapField;
    }

    private static Field getBJHseedRenderMapField() {
        try {
            BJHseedRenderMapField = BelljarHandler.class.getDeclaredField("seedRenderMap");
        } catch (NoSuchFieldException e) {
            ZenCloche.logger.error(e.getMessage());
            return null;
        }
        BJHseedRenderMapField.setAccessible(true);
        return BJHseedRenderMapField;
    }

    private static Field getBJHplantHandlersField() {
        try {
            BJHplantHandlersField = BelljarHandler.class.getDeclaredField("plantHandlers");
        } catch (NoSuchFieldException e) {
            ZenCloche.logger.error(e.getMessage());
            return null;
        }
        BJHplantHandlersField.setAccessible(true);
        return BJHplantHandlersField;
    }

    @ZenMethod
    @ZenDoc(value="Register the given seed so it can be grown in the Graden Cloche. <br>Specifying a list of drops, the needed soil and optionally a block to display visually inside the cloche. <br>If no soil is specified, dirt is used instead. <br>If no display block is specified, the seed is used as block. <br>This may result in texture errors if the item can't be a block.")
    public static void registerCrop(IItemStack seed, IItemStack[] drops) {
        Block block = CraftTweakerMC.getBlock(seed);
        ItemStack[] output = Arrays.stream(drops).map(CraftTweakerMC::getItemStack).toArray(ItemStack[]::new);
        IngredientStack soilStack = new IngredientStack(new ItemStack(Blocks.DIRT));
        BelljarHandler.cropHandler.register(CraftTweakerMC.getItemStack(seed), output, soilStack, block.getDefaultState());
    }

    @ZenMethod
    @ZenDoc(value="Register the given seed so it can be grown in the Graden Cloche. <br>Specifying a list of drops, the needed soil and optionally a block to display visually inside the cloche. <br>If no soil is specified, dirt is used instead. <br>If no display block is specified, the seed is used as block. <br>This may result in texture errors if the item can't be a block.")
    public static void registerCrop(IItemStack seed, IItemStack[] drops, @Optional IIngredient soil) {
        Block block = CraftTweakerMC.getBlock(seed);
        ItemStack[] output = Arrays.stream(drops).map(CraftTweakerMC::getItemStack).toArray(ItemStack[]::new);
        IngredientStack soilStack = soil != null ? ImmersiveAdapter.ingredient(soil) : new IngredientStack(new ItemStack(Blocks.DIRT));
        BelljarHandler.cropHandler.register(CraftTweakerMC.getItemStack(seed), output, soilStack, block.getDefaultState());
    }

    @ZenMethod
    @ZenDoc(value="Register the given seed so it can be grown in the Graden Cloche. <br>Specifying a list of drops, the needed soil and optionally a block to display visually inside the cloche. <br>If no soil is specified, dirt is used instead. <br>If no display block is specified, the seed is used as block. <br>This may result in texture errors if the item can't be a block.")
    public static void registerCrop(IItemStack seed, IItemStack[] drops, IIngredient soil, IBlock display) {
        Block block = display != null ? CraftTweakerMC.getBlock(display) : CraftTweakerMC.getBlock(seed);
        ItemStack[] output = Arrays.stream(drops).map(CraftTweakerMC::getItemStack).toArray(ItemStack[]::new);
        IngredientStack soilStack = soil != null ? ImmersiveAdapter.ingredient(soil) : new IngredientStack(new ItemStack(Blocks.DIRT));
        BelljarHandler.cropHandler.register(CraftTweakerMC.getItemStack(seed), output, soilStack, block.getDefaultState());
    }

    @ZenMethod
    @ZenDoc(value="Register the given seed so it can be grown in the Graden Cloche. <br>Specifying a list of drops, the needed soil and optionally a block to display visually inside the cloche. <br>If no soil is specified, dirt is used instead. <br>If no display block is specified, the seed is used as block. <br>This may result in texture errors if the item can't be a block.")
    public static void registerCrop(IItemStack seed, IItemStack[] drops, IIngredient soil, crafttweaker.api.block.IBlockState display) {
        IBlockState blockstate = display != null ? CraftTweakerMC.getBlockState(display) : CraftTweakerMC.getBlock(seed).getDefaultState();
        ItemStack[] output = Arrays.stream(drops).map(CraftTweakerMC::getItemStack).toArray(ItemStack[]::new);
        IngredientStack soilStack = soil != null ? ImmersiveAdapter.ingredient(soil) : new IngredientStack(new ItemStack(Blocks.DIRT));
        BelljarHandler.cropHandler.register(CraftTweakerMC.getItemStack(seed), output, soilStack, blockstate);
    }

    @ZenMethod
    @ZenDoc(value="Removes a registered crop.")
    public static void removeCrop(IItemStack seed) {
        // Try to init the fields
        if (BJHseedOutputMapField == null) {
            BJHseedOutputMapField = getBJHseedOutputMapField();
        }
        if (BJHseedRenderMapField == null) {
            BJHseedRenderMapField = getBJHseedRenderMapField();
        }
        if (BJHseedSoilMapField == null) {
            BJHseedSoilMapField = getBJHseedSoilMapField();
        }
        if (BJHplantHandlersField == null) {
            BJHplantHandlersField = getBJHplantHandlersField();
        }

        // If the field is still null, abort and log the error
        if (BJHseedOutputMapField == null || BJHseedRenderMapField == null || BJHseedSoilMapField == null) {
            ZenCloche.logger.error("Could not retrieve plantHandlers, seedOutputMap, seedRenderMap or seedSoilMap fields from the BellJarHandler class. Removing crops will not be possible");
        }

        ComparableItemStack comp = new ComparableItemStack(CraftTweakerMC.getItemStack(seed), false, false);
        ItemStack seedStack = CraftTweakerMC.getItemStack(seed);

        try {
            HashSet<BelljarHandler.IPlantHandler> plantHandlers = (HashSet<BelljarHandler.IPlantHandler>)BJHplantHandlersField.get(null);
            HashMap<ComparableItemStack, IngredientStack> seedSoilMap = (HashMap<ComparableItemStack, IngredientStack>)BJHseedSoilMapField.get(null);
            HashMap<ComparableItemStack, ItemStack[]> seedOutputMap = (HashMap<ComparableItemStack, ItemStack[]>)BJHseedOutputMapField.get(null);
            HashMap<ComparableItemStack, IBlockState[]> seedRenderMap = (HashMap<ComparableItemStack, IBlockState[]>)BJHseedRenderMapField.get(null);

            for (BelljarHandler.IPlantHandler handler : plantHandlers) {
                if (handler.isValid(seedStack)) {
                    try {
                        Method getSeedSetMethod = handler.getClass().getDeclaredMethod("getSeedSet");
                        getSeedSetMethod.setAccessible(true);
                        HashSet<ComparableItemStack> validSeeds = (HashSet<ComparableItemStack>)getSeedSetMethod.invoke(handler);
                        validSeeds.remove(comp);
                    } catch (NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            seedSoilMap.remove(comp);
            seedOutputMap.remove(comp);
            seedRenderMap.remove(comp);

            // BJHplantHandlersField.set(null, plantHandlers);
            BJHseedSoilMapField.set(null, seedSoilMap);
            BJHseedOutputMapField.set(null, seedOutputMap);
            BJHseedRenderMapField.set(null, seedRenderMap);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}
