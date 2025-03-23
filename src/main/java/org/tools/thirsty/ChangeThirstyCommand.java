package org.tools.thirsty;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChangeThirstyCommand implements CommandExecutor {
    Plugin plugin;
    HashMap<Player,Integer> map;

    public ChangeThirstyCommand(Plugin plg, HashMap<Player,Integer> tmap) {
        plugin=plg;
        map=tmap;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            try {
                String plrname = strings[0];
                Integer amount = Integer.valueOf(strings[1]);


                Player target = plugin.getServer().getPlayerExact(plrname);
                if (target != null) {
                    if (amount <= 24 && amount > 0) {
                        map.replace(target,amount);
                    } else if (amount < 0) {
                        map.replace(target,0);
                    } else if (amount > 24) {
                        map.replace(target,24);
                    }
                }


            } catch (Exception e) {
                player.sendMessage("오류! 울바른 사용법 : thirsty <플레이어 이름> <숫자 값>");
                plugin.getLogger().info(e.getMessage());
            }

            return true;
        }
        return false;
    }
}
