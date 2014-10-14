package com.karhaan.chunkgenerator;

import net.minecraft.init.Blocks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = ChunkGenerator.MODID, name = ChunkGenerator.NAME, version = ChunkGenerator.VERSION)
public class ChunkGenerator
{
    public static final String MODID = "ChunkPregenerator";
    public static final String NAME = "Chunk Pregenerator";
    public static final String VERSION = "1.7.10-1.0";
    
//    @EventHandler
//    public void init(FMLInitializationEvent event)
//    {
//		// some example code
//        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
//    }
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
    	event.registerServerCommand(new GenerateChunkCommand());
    }
}
