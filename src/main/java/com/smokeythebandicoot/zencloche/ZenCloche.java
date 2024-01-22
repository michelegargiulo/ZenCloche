package com.smokeythebandicoot.zencloche;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import crafttweaker.mc1120.CraftTweaker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = ZenCloche.MODID,
        name = "Zen cloche",
        version = "${version}",
        acceptedMinecraftVersions = "${mcversion}",
        dependencies = ZenCloche.DEPS
)
public class ZenCloche {

    public static final String MODID = "zencloche";
    public static final String DEPS = "required-after:" + ImmersiveEngineering.MODID + ";required-after:" + CraftTweaker.MODID;
    public static Logger logger;
    @Instance(value = MODID)
    public static ZenCloche instance;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new ListCommand());
    }
}
