package com.smokeythebandicoot.zencloche;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = ZenCloche.MODID,
        acceptedMinecraftVersions = "[1.12.2]")
public class ZenCloche {

    public static final String MODID = "zencloche";

    public static Logger logger;
    @Instance(value = MODID)
    public static ZenCloche instance;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }
}
