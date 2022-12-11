package net.viciscat.dimensionbreathnt.commands;

import com.google.common.primitives.Ints;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.viciscat.dimensionbreathnt.DimensionBreathnt;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CommandNobreath extends CommandBase {

    private <T> boolean process(String action, EntityPlayer player, List<T> prop, T data, String message) {
        switch (action) {
            case "add":
                if (prop.contains(data)) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + message + " already blacklisted! (" + data + ")"));
                    return false;
                }

                prop.add(data);
                player.sendMessage(new TextComponentString(TextFormatting.WHITE + message + " has been added! (" + data + ")"));
                break;
            case "remove":
                if (!prop.contains(data)) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + message + " is already not blacklisted! (" + data + ")"));
                    return false;
                }

                prop.remove(data);
                player.sendMessage(new TextComponentString(TextFormatting.WHITE + message + " has been removed! (" + data + ")"));
                break;
            default:
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Syntax: /nobreath " + message.toLowerCase() + " (add|remove)"));
                return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "nobreath";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "nobreath (biome|dimension) (add|remove)";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Command must be executed by a Player!"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Command Syntax: /nobreath (biome|dimension) (add|remove)"));
            return;
        }
        ;
        EntityPlayer player = ((EntityPlayer) sender);

        String type = args[0];
        String action = args[1];
        Integer biome_id = Biome.getIdForBiome(player.world.getBiome(sender.getPosition()));

        if (Objects.equals(type, "biome")) {
            if (process(action, player, DimensionBreathnt.Biomes_no_breath, biome_id, "Biome")) {
                DimensionBreathnt.Biomes_no_breath_prop.set(Ints.toArray(DimensionBreathnt.Biomes_no_breath));
            }


        } else if (Objects.equals(type, "dimension")) {
            if (process(action, player, DimensionBreathnt.Dimensions_no_breath, player.dimension, "Dimension")) {
                DimensionBreathnt.Dimensions_no_breath_prop.set(Ints.toArray(DimensionBreathnt.Dimensions_no_breath));
            }
        } else {
            sender.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Command Syntax: /nobreath (biome|dimension) (add|remove)"));
        }

        DimensionBreathnt.config.save();


    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, java.lang.String[] args, @Nullable BlockPos targetPos){
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "biome", "dimension"): (
                args.length == 2 ? getListOfStringsMatchingLastWord(args, "add", "remove"): null
                );
    }
}
