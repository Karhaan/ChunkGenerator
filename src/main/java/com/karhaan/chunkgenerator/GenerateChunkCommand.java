package com.karhaan.chunkgenerator;

import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.DimensionManager;

public class GenerateChunkCommand extends CommandBase {

	public GenerateChunkCommand()
	{
	}
	
	@Override
	public String getCommandName() {
		return "generatechunk";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "commands.generatechunk.usage";
	}

	@Override
	public List getCommandAliases() {
		return null;
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] args) {
		if(args.length != 2 && args.length != 4)
			throw new WrongUsageException("commands.generatechunk.usage", new Object[0]);

		int startX = this.parseInt(commandSender, args[0]);
		int startZ = this.parseInt(commandSender, args[1]);
		int endX;
		int endZ;
		if(args.length == 4)
		{
			// Make a rectangle out of this.
			endX = this.parseInt(commandSender, args[2]);
			endZ = this.parseInt(commandSender, args[3]);
		}
		else
		{
			// Make a square.
			int x = Math.abs(startX) / 2;
			int z = Math.abs(startZ) / 2;

			startX = -x;
			startZ = -z;
			endX = x;
			endZ = z;
		}

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		long lastTime = server.getSystemTimeMillis(); 
		
		final long chunkPerWorld = ((endX>>4)- (startX>>4) + 1) * ((endZ >> 4) - (startZ >> 4) + 1);
		final long chunkTotal = server.worldServers.length * chunkPerWorld;
		long chunkDone = 0;
		WorldServer[] servers = DimensionManager.getWorlds(); // In theory, this should work with MystCraft.
		
		for(int worldIndex = 0; worldIndex < servers.length; worldIndex++)
		{
			WorldServer world = servers[worldIndex];
			PlayerManager playerManager = world.getPlayerManager();
			
			commandSender.addChatMessage(new ChatComponentText("Generate " + getWorldDimensionName(world) + " (" + chunkDone + "/" + chunkTotal + ")."));
			
			for(int x = startX; x <= endX; x++)
			{
				for(int z = startZ; z <= endZ; z++)
				{
					long currentTime = server.getSystemTimeMillis();
					
					if(currentTime - lastTime > 1000L) { // This will flood the console eventually but at least you'll see other messages related to chunk creation.
						commandSender.addChatMessage(new ChatComponentText("Generate " + getWorldDimensionName(world) + " (" + chunkDone + "/" + chunkTotal + ")."));
						lastTime = currentTime;
					}
					
					boolean chunkExists = false;
					if (world.theChunkProviderServer.currentChunkLoader instanceof AnvilChunkLoader)
			        {
			            AnvilChunkLoader loader = (AnvilChunkLoader) world.theChunkProviderServer.currentChunkLoader;
			            chunkExists |= loader.chunkExists(world, x, z);
			        }
					if(!chunkExists)
					{
						world.theChunkProviderServer.loadChunk(x, z);
						if(!playerManager.func_152621_a(x, z)) // We wouldn't want to unload a chunk where a player is actually in.
							world.theChunkProviderServer.unloadChunksIfNotNearSpawn(x, z);
					}
					
					// Cleanup loaded chunks so we don't overload memory.
					if(chunkDone % 100 == 0) { // FIXME: The 100 is based on the value in the unloadQueuedChunks loop.
						world.provider.worldChunkMgr.cleanupCache();
						boolean previousLevelSaving = world.levelSaving;
						world.levelSaving = false;
						world.theChunkProviderServer.unloadQueuedChunks();
						world.levelSaving = previousLevelSaving;
						try {
							// We create chunks faster than they are saved, so we wait for everything to be written before continuing.
							ThreadedFileIOBase.threadedIOInstance.waitForFinish();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					chunkDone ++;
				}
			}
			
			world.theChunkProviderServer.unloadAllChunks();
		}
		
		func_152373_a(commandSender, this, "commands.generatechunk.success", new Object[] { startX, startZ, endX, endZ });
	}

	private String getWorldDimensionName(WorldServer worldserver) {
		return "\'" + worldserver.getWorldInfo().getWorldName() + "\'/" + worldserver.provider.getDimensionName();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return MinecraftServer.getServer().getConfigurationManager().func_152608_h().func_152689_b() && super.canCommandSenderUseCommand(p_71519_1_);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
		return null;
	}
}
