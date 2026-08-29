package fr.duelplugin.managers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.npc.NPC;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.Arena;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DuelBotManager implements Listener {

    private final DuelPlugin plugin;
    private final Map<UUID, BotState> botDuels = new HashMap<>();
    private final Map<UUID, UUID> entityToPlayer = new HashMap<>();
    private final Map<UUID, Integer> attackTicks = new HashMap<>();

    private static final double BOT_SPEED = 0.22;
    private static final double ATTACK_RANGE = 2.4;
    private static final double BOT_DAMAGE = 7.0;
    private static final int ATTACK_INTERVAL = 26;

    private static final String SKIN_UUID = "853c80ef-3c37-49fd-aa49-938b674adae6";
    private static final Pattern JSON_EXTRACT = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"signature\"\\s*:\\s*\"([^\"]+)\"");

    public DuelBotManager(DuelPlugin plugin) {
        this.plugin = plugin;
        startAITask();
    }

    public boolean startBotDuel(Player player) {
        if (plugin.getDuelManager().isInDuel(player)) return false;
        if (plugin.getQueueManager().isInAnyQueue(player)) return false;

        Arena arena = plugin.getArenaManager().getAvailableArena(DuelGameMode.SWORD);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "queue_no_arena", "%mode%", DuelGameMode.SWORD.getDisplayName()));
            return false;
        }

        UUID botUuid = UUID.randomUUID();
        plugin.getDuelManager().startBotDuel(player, botUuid, arena);
        spawnBot(player, arena, botUuid);
        return true;
    }

    private void spawnBot(Player player, Arena arena, UUID botUuid) {
        Location loc = arena.resolveSpawn2();
        if (loc == null || loc.getWorld() == null) return;
        World world = loc.getWorld();

        String botName = strip(plugin.getLanguageManager().msgRaw(player, "bot_name"));

        Zombie bot = world.spawn(loc, Zombie.class, z -> {
            z.setAI(false);
            z.setBaby(false);
            z.setSilent(true);
            z.setCanPickupItems(false);
            z.setRemoveWhenFarAway(false);
            z.setPersistent(true);
            z.setCollidable(false);
            z.setShouldBurnInDay(false);
            z.setInvisible(true);
            z.setCustomNameVisible(false);
            z.setCustomName(null);
        });

        bot.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        bot.setHealth(20.0);

        EntityEquipment eq = bot.getEquipment();
        if (eq != null) {
            eq.setItemInMainHand(ench(Material.DIAMOND_SWORD, Enchantment.SWEEPING_EDGE, 3));
            eq.setBoots(ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 3));
            eq.setLeggings(ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 3));
            eq.setChestplate(ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 3));
            eq.setHelmet(ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 3));
            eq.setItemInMainHandDropChance(0);
            eq.setBootsDropChance(0);
            eq.setLeggingsDropChance(0);
            eq.setChestplateDropChance(0);
            eq.setHelmetDropChance(0);
        }

        UserProfile profile = new UserProfile(botUuid, botName, Collections.emptyList());
        NPC npc = new NPC(profile, bot.getEntityId());
        npc.setGameMode(GameMode.SURVIVAL);
        npc.spawn(PacketEvents.getAPI().getPlayerManager().getChannel(player));

        BotState state = new BotState(bot.getUniqueId(), arena, npc);
        botDuels.put(player.getUniqueId(), state);
        entityToPlayer.put(bot.getUniqueId(), player.getUniqueId());
        attackTicks.put(player.getUniqueId(), 0);

        fetchSkinAsync(state, player);
    }

    private void fetchSkinAsync(BotState state, Player viewer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<TextureProperty> textures = fetchSkinTextures();
            if (textures == null || textures.isEmpty()) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                BotState current = botDuels.get(viewer.getUniqueId());
                if (current == null || current.npc == null || current.npc.getId() != state.npc.getId()) return;
                try {
                    current.npc.changeSkin(UUID.randomUUID(), textures);
                } catch (Exception ignored) {
                }
            });
        });
    }

    private List<TextureProperty> fetchSkinTextures() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + SKIN_UUID + "?unsigned=false");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("User-Agent", "DuelPlugin/1.12");
            int code = conn.getResponseCode();
            if (code != 200) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            Matcher m = JSON_EXTRACT.matcher(sb.toString());
            if (m.find()) {
                List<TextureProperty> list = new ArrayList<>();
                list.add(new TextureProperty("textures", m.group(1), m.group(2)));
                return list;
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static String strip(String s) {
        if (s == null) return "Bot";
        return s.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }

    public boolean isBotDuelEntity(Entity entity) {
        return entityToPlayer.containsKey(entity.getUniqueId());
    }

    private void startAITask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerUuid : new ArrayList<>(botDuels.keySet())) {
                    tickBot(playerUuid);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void tickBot(UUID playerUuid) {
        Player p = Bukkit.getPlayer(playerUuid);
        BotState state = botDuels.get(playerUuid);
        if (p == null || !p.isOnline() || state == null) {
            cleanup(playerUuid);
            return;
        }

        if (!plugin.getDuelManager().isInDuel(playerUuid)) {
            cleanup(playerUuid);
            return;
        }

        Entity entity = Bukkit.getEntity(state.botEntityUuid);
        if (entity == null || entity.isDead() || !(entity instanceof Zombie bot)) {
            cleanup(playerUuid);
            return;
        }

        if (plugin.getDuelManager().isFrozen(p)) return;

        Vector to = p.getLocation().toVector().subtract(bot.getLocation().toVector());
        double dist = to.length();
        double dy = p.getLocation().getY() - bot.getLocation().getY();
        if (dist < 0.01) return;

        Vector dir = to.clone().normalize();

        Location look = bot.getLocation().clone();
        look.setDirection(dir);
        bot.teleport(look);
        syncNpc(state.npc, bot);

        if (dist > ATTACK_RANGE) {
            Location next = bot.getLocation().clone().add(dir.multiply(BOT_SPEED));
            next.setY(bot.getLocation().getY() + dir.getY() * BOT_SPEED + dy * 0.1);
            bot.teleport(next);
            syncNpc(state.npc, bot);
        }

        if (dist <= ATTACK_RANGE) {
            int tick = attackTicks.getOrDefault(playerUuid, 0) + 1;
            attackTicks.put(playerUuid, tick);
            if (tick >= ATTACK_INTERVAL) {
                attackTicks.put(playerUuid, 0);
                if (p.isOnline() && !p.isDead()) {
                    p.damage(BOT_DAMAGE, bot);
                    Vector kb = dir.setY(0).normalize().multiply(0.5).add(new Vector(0, 0.35, 0));
                    p.setVelocity(kb);
                    playSwing(state.npc, p);
                }
            }
        } else {
            attackTicks.put(playerUuid, 0);
        }
    }

    private void syncNpc(NPC npc, Zombie bot) {
        try {
            Location location = bot.getLocation();
            npc.updateRotation(location.getYaw(), location.getPitch());
            npc.teleport(SpigotConversionUtil.fromBukkitLocation(location));
        } catch (Exception ignored) {
        }
    }

    private void playSwing(NPC npc, Player viewer) {
        try {
            WrapperPlayServerEntityAnimation animation =
                    new WrapperPlayServerEntityAnimation(npc.getId(), WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM);
            PacketEvents.getAPI().getPlayerManager().sendPacket(
                    PacketEvents.getAPI().getPlayerManager().getChannel(viewer), animation);
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UUID playerUuid = entityToPlayer.remove(event.getEntity().getUniqueId());
        if (playerUuid == null) return;

        event.setDroppedExp(0);
        event.getDrops().clear();

        UUID botUuid = event.getEntity().getUniqueId();
        plugin.getDuelManager().endDuel(playerUuid, playerUuid, botUuid);
    }

    public void cleanup(UUID playerUuid) {
        BotState state = botDuels.remove(playerUuid);
        attackTicks.remove(playerUuid);
        if (state != null) {
            entityToPlayer.remove(state.botEntityUuid);
            Entity entity = Bukkit.getEntity(state.botEntityUuid);
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
            try {
                state.npc.despawnAll();
            } catch (Exception ignored) {
            }
        }
    }

    public void cleanup() {
        for (UUID playerUuid : new ArrayList<>(botDuels.keySet())) {
            cleanup(playerUuid);
        }
    }

    private ItemStack ench(Material mat, Enchantment ench, int level) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(ench, level, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static class BotState {
        final UUID botEntityUuid;
        final Arena arena;
        final NPC npc;

        BotState(UUID botEntityUuid, Arena arena, NPC npc) {
            this.botEntityUuid = botEntityUuid;
            this.arena = arena;
            this.npc = npc;
        }
    }
}