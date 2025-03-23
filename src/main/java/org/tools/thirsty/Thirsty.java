package org.tools.thirsty;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Thirsty extends JavaPlugin implements Listener {
    HashMap<Player,Integer> PlayerThirstyMap = new HashMap<>();

    public void AddThirsty(Player player, Integer amount) {
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            Integer thirsty = PlayerThirstyMap.get(player);
            if (thirsty != null) {
                PlayerThirstyMap.put(player,thirsty+amount);

                if (PlayerThirstyMap.get(player) < 0) {
                    PlayerThirstyMap.put(player,0);
                }
                if (PlayerThirstyMap.get(player) > 24) {
                    PlayerThirstyMap.put(player,24);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("갈증 플러그인 시작");
        getServer().getPluginManager().registerEvents(this,this);

        getServer().getPluginCommand("thirsty").setExecutor(new ChangeThirstyCommand(this,PlayerThirstyMap));

        // 갈증 초기화
        for (Player p : getServer().getOnlinePlayers()) {
            PlayerThirstyMap.putIfAbsent(p, 20);
        }


        // 조합 결과물
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionType(PotionType.WATER);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("정화됨!");
        meta.setLore(lore);
        item.setItemMeta(meta);

        // 조합 추가
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(this, "purification"),item);
        recipe.addIngredient(Material.CHARCOAL);
        recipe.addIngredient(Material.POTION);

        getServer().addRecipe(recipe);


        // 액션바
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                        Integer plrThirsty = PlayerThirstyMap.get(player);
                        if (plrThirsty != null) {
                            StringBuilder bar = new StringBuilder("§9");

                            if (plrThirsty <= 20) {
                                for (int i = 0; i < (plrThirsty / 2); i++) {
                                    bar.append("■");
                                }

                                if (plrThirsty % 2 == 1) {
                                    bar.append("□");
                                }
                            } else {
                                bar.append("■■■■■■■■■■");
                            }

                            TextComponent actionbar = new TextComponent(bar.toString());
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,actionbar);

                            if (plrThirsty <= 0) {
                                player.damage(1, DamageSource.builder(DamageType.DRY_OUT).build());
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this,0,1);
    }



    @Override
    public void onDisable() {
        getLogger().info("갈증 플러그인 종료");
    }


    // 플레이어 접속 시 갈증 설정
    @EventHandler
    public void AddJoinedPlayerMap(PlayerJoinEvent event) {
        PlayerThirstyMap.putIfAbsent(event.getPlayer(), 20);
    }

    @EventHandler
    public void ResetThirsty(PlayerRespawnEvent event) {
        PlayerThirstyMap.replace(event.getPlayer(), 20);
    }

    // 갈증이 차지 않았으면 체력 회복 못함
    @EventHandler
    public void RegenerateEvent(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                if (PlayerThirstyMap.get(player) < 18) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // 물 마셨을때
    @EventHandler
    public void EatWater(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Integer plrThirsty = PlayerThirstyMap.get(player);
        if (plrThirsty != null && event.getItem().getType() == Material.POTION) {
            PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
            if (meta.getCustomEffects().isEmpty() && meta.getBasePotionType() == PotionType.WATER) {
                if (meta.getLore() == null || meta.getLore().isEmpty()) { // 정화되지 않은 물
                    AddThirsty(player,1);
                    player.damage(2);
                } else { // 아니면
                    AddThirsty(player,3);
                }
            }
        }
    }

    @EventHandler
    public void ThirstyEvent(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            Integer plrThirsty = PlayerThirstyMap.get(player);
            if (plrThirsty != null && player.getFoodLevel() > event.getFoodLevel()) {
                AddThirsty(player,-(player.getFoodLevel() - event.getFoodLevel()));
            }
        }
    }
}
