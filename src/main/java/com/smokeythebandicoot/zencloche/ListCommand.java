package com.smokeythebandicoot.zencloche;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.*;

public class ListCommand implements ICommand {

    @Override
    public String getName()
    {
        return "zenclocheList";
    }

    public int compareTo(ICommand arg0) {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("zcList");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {

        if (sender.getEntityWorld().isRemote) return;

        if (args.length != 1) {
            sender.sendMessage(new TextComponentString("Command requires one argument: " +
                    "\nUse 'fluidFertilizers' to print fluid fertilizers" +
                    "\nUse 'crops' to print crops"));
            return;
        }

        switch (args[0]) {
            case "fluidFertilizers":
                sender.sendMessage(new TextComponentString(String.join("\n", GardenClocheIntegration.listFluidFertilizers())));
                break;
            case "crop":
                sender.sendMessage(new TextComponentString(String.join("\n", GardenClocheIntegration.listCrops())));
                break;
            default:
                sender.sendMessage(new TextComponentString("Could not recognize argument. Valid arguments are, 'fluidFertilizers' and 'crop' (without quotes)"));
        }

    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender.canUseCommand(2, getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {

        List<String> tabs = new ArrayList<>();
        Set<String> possibleCommands = new HashSet<String>() {{
            add("fluidFertilizers");
            add("crop");
        }};

        if (args.length == 1) {
            if (args[0].isEmpty())
                return new ArrayList<>(possibleCommands);
            for (String cmd : possibleCommands) {
                if (cmd.startsWith(args[0]))
                    tabs.add(cmd);
            }
        }
        return tabs;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }
}
