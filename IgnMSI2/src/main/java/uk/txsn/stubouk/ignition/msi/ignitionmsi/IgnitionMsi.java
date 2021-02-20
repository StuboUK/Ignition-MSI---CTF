package uk.txsn.stubouk.ignition.msi.ignitionmsi;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.xezard.glow.data.glow.Glow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class IgnitionMsi extends JavaPlugin implements Listener{



    String host, port, database, username, password;
    static Connection connection;
    public void openConnection() throws SQLException,
            ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://"
                        + this.host+ ":" + this.port + "/" + this.database,
                this.username, this.password);
    }

    List<Player> deadPlayers = new ArrayList<Player>();
    List<txsnMember> txsnPlayers = new ArrayList<txsnMember>();
    Material[] matList = Material.values();
    gameDirector gameDirector = new gameDirector();
    @Override
    public void onEnable() {
        host = "localhost";
        port = "3306";
        database = "txsn";
        username = "root";
        password = "x";

        try {
            openConnection();
            Statement statement = connection.createStatement();
            getLogger().info("Connected to toxicSynergy Database!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(this, this);
        gameDirector = new gameDirector();

        gameDirector.mainArena = Bukkit.getWorld("world");

        gameDirector.blueTeamRefLoc = (Location) getConfig().get("blueTeamRefLoc");
        gameDirector.blueTeamXPGenLoc = (Location) getConfig().get("blueTeamXPGenLoc");
        gameDirector.blueTeamCarePackageLoc = (Location) getConfig().get("blueTeamCarePackageLoc");
        gameDirector.blueTeamRespawnPoint = (Location) getConfig().get("blueTeamRespawnPoint");
        gameDirector.blueTeamSpawnPoint = (Location) getConfig().get("blueTeamSpawnPoint");

        gameDirector.redTeamRefLoc = (Location) getConfig().get("redTeamRefLoc");
        gameDirector.redTeamXPGenLoc = (Location) getConfig().get("redTeamXPGenLoc");
        gameDirector.redTeamCarePackageLoc = (Location) getConfig().get("redTeamCarePackageLoc");
        gameDirector.redTeamRespawnPoint = (Location) getConfig().get("redTeamRespawnPoint");
        gameDirector.redTeamSpawnPoint = (Location) getConfig().get("redTeamSpawnPoint");

        respawnPlayers();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                spawnRedRefineryItem();
                spawnBlueRefineryItem();
            }
        }, 520, 520);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                spawnRedXPGenOrb();
                spawnBlueXPGenOrb();
            }
        }, 0, 225);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                updateTXSNMembers();
            }
        }, 20000, 20000);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if(gameDirector.gameState.equals("LIVE")) {
                    gameDirector.broadcastMessage("Care Packages are being dropped, listen for them!", ChatColor.GREEN);
                    spawnBlueCarePackage();
                    spawnRedCarePackage();
                }
            }
        }, 6000, 6000);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                slowRedFlagCarrier();
                slowBlueFlagCarrier();
            }
        }, 40, 40);
    }


    Glow redGlow = Glow.builder()
            .animatedColor(ChatColor.RED)
            .name("RedGlow")
            .build();

    Glow blueGlow = Glow.builder()
            .animatedColor(ChatColor.BLUE)
            .name("BlueGlow")
            .build();

    public txsnMember getProfile(String name){
        txsnMember p = txsnPlayers.stream().filter(txsnMember -> txsnMember.mcname.equals(name)).findFirst().orElse(null);
        return p;
    }
    public void updateTXSNMembers() {
        txsnPlayers.clear();                                    //Remove everyone from the cached txsnPlayers list table
        for(Player p : Bukkit.getOnlinePlayers()){
            txsnMember tp = new txsnMember(p.getName());        //Get the users profile.
            txsnPlayers.add(tp);                                //Readd this txsnPlayer to the global txsnPlayers table
        }
    }

    private int schedulerId = -1;
    public void spawnRedCarePackage() {
        if(gameDirector.gameState == "LIVE") {
            Location currentPosition = gameDirector.redTeamCarePackageLoc;
            int x = new Random().nextInt(50);
            int z = new Random().nextInt(50);
            currentPosition.add(x, 0, z);
            Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(255,0,0), 1);
            Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(0,255,0), 1);
            schedulerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()  {
                @Override
                public void run() {
                    currentPosition.setY(currentPosition.getY() - 2);
                    if (!currentPosition.getBlock().getType().isAir()) {
                        currentPosition.setY(currentPosition.getY() + 2);
                        currentPosition.getBlock().setType(Material.GLOWSTONE);
                        gameDirector.mainArena.playSound(currentPosition, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 10, 1);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, 1,0,0, green);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,0, green);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,-1, green);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,1, green);
                        currentPosition.setY(currentPosition.getY() - 1);
                        Chest chest = (Chest) currentPosition.getBlock().getState();
                        Inventory chestinv = chest.getBlockInventory();
                        chestinv.addItem(new ItemStack(Material.COAL,32));
                        chestinv.addItem(new ItemStack(Material.COOKED_BEEF,16));
                        chestinv.addItem(new ItemStack(Material.DARK_OAK_LOG,16));
                        Bukkit.getServer().getScheduler().cancelTask(schedulerId);
                        return;
                    }
                    currentPosition.getBlock().setType(Material.CHEST);
                    currentPosition.setY(currentPosition.getY() + 1);
                    currentPosition.getBlock().setType(Material.AIR);

                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, 1,0,0, red);
                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,0, red);
                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,-1, red);
                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,1, red);
                    gameDirector.mainArena.playSound(currentPosition, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 5, 1);

                }
            }, 20, 20);
        }
    }
    public void spawnRedXPGenOrb() {
        if(gameDirector.gameState.equals("LIVE")){
            ExperienceOrb orb = gameDirector.mainArena.spawn(gameDirector.redTeamXPGenLoc, ExperienceOrb.class);
            orb.setExperience(5);
        }
    }
    public void spawnRedRefineryItem() {
        if(gameDirector.gameState.equals("LIVE")) {
            int random = new Random().nextInt(matList.length);
            Material item = matList[random];
            if(item == Material.RED_WOOL ||
                    item == Material.BLUE_WOOL ||
                    item == Material.BARRIER ||
                    item == Material.BEDROCK ||
                    item == Material.COMMAND_BLOCK ||
                    item == Material.CHAIN_COMMAND_BLOCK ||
                    item == Material.ENDER_CHEST
            ) {
                spawnRedRefineryItem();
                return;
            }
            try {
                gameDirector.mainArena.dropItemNaturally(gameDirector.redTeamRefLoc, new ItemStack(item, 1));
            } catch (Exception e) {
                spawnRedRefineryItem();
            }
        }
    }

    public void spawnBlueXPGenOrb() {
        if(gameDirector.gameState.equals("LIVE")){
            ExperienceOrb orb = gameDirector.mainArena.spawn(gameDirector.blueTeamXPGenLoc, ExperienceOrb.class);
            orb.setExperience(5);
        }
    }
    public void spawnBlueRefineryItem() {
        if(gameDirector.gameState.equals("LIVE")) {
            int random = new Random().nextInt(matList.length);
            Material item = matList[random];
            if(item == Material.RED_WOOL ||
                    item == Material.BLUE_WOOL ||
                    item == Material.BARRIER ||
                    item == Material.BEDROCK ||
                    item == Material.COMMAND_BLOCK ||
                    item == Material.ENDER_CHEST ||
                    item == Material.CHAIN_COMMAND_BLOCK
            ) {
                spawnBlueRefineryItem();
                return;
            }
            try {
                gameDirector.mainArena.dropItemNaturally(gameDirector.blueTeamRefLoc, new ItemStack(item, 1));
            } catch (Exception e) {
                spawnBlueRefineryItem();
            }
        }
    }
    private int schedulerId1 = -1;
    public void spawnBlueCarePackage() {
        if(gameDirector.gameState.equals("LIVE")) {
            Location currentPosition = gameDirector.blueTeamCarePackageLoc;
            int x = new Random().nextInt(300);
            int z = new Random().nextInt(160);
            currentPosition.add(x, 0, z);
            Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(0,0,255), 1);
            Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(0,255,0), 1);
            schedulerId1 = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()  {
                @Override
                public void run() {
                    currentPosition.setY(currentPosition.getY() - 2);
                    if (!currentPosition.getBlock().getType().isAir()) {
                        currentPosition.setY(currentPosition.getY() + 2);
                        currentPosition.getBlock().setType(Material.GLOWSTONE);
                        gameDirector.mainArena.playSound(currentPosition, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 10, 1);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, 1,0,0, green);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,0, green);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,-1, green);
                        currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,1, green);
                        currentPosition.setY(currentPosition.getY() - 1);
                        Chest chest = (Chest) currentPosition.getBlock().getState();
                        Inventory chestinv = chest.getBlockInventory();
                        chestinv.addItem(new ItemStack(Material.COAL,32));
                        chestinv.addItem(new ItemStack(Material.COOKED_BEEF,16));
                        chestinv.addItem(new ItemStack(Material.DARK_OAK_LOG,16));
                        Bukkit.getServer().getScheduler().cancelTask(schedulerId1);
                        return;
                    }
                    currentPosition.getBlock().setType(Material.CHEST);
                    currentPosition.setY(currentPosition.getY() + 1);
                    currentPosition.getBlock().setType(Material.AIR);

                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, 1,0,0, blue);
                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,0, blue);
                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,-1, blue);
                    currentPosition.getWorld().spawnParticle(Particle.REDSTONE, currentPosition, 1, -1,0,1, blue);
                    gameDirector.mainArena.playSound(currentPosition, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 5, 1);
                }
            }, 20, 20);
        }
    }
    public int timer = 30;
    public void respawnPlayers() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if(timer == 0) {
                    for(Player p : deadPlayers) {
                        txsnMember tp = getProfile(p.getName());
                        if(tp.team.equals("Red")){
                            if(p.getBedSpawnLocation() == null){
                                p.teleport(gameDirector.redTeamSpawnPoint);
                            } else {
                                p.teleport(p.getBedSpawnLocation());
                            }
                        } else {
                            if(p.getBedSpawnLocation() == null){
                                p.teleport(gameDirector.blueTeamSpawnPoint);
                            } else {
                                p.teleport(p.getBedSpawnLocation());
                            }
                        }
                    }
                    timer = 30;
                    deadPlayers.clear();
                } else if (timer == 3) {
                    for(Player p: deadPlayers) {
                        p.sendMessage("You will be respawned in 3...");
                    }
                    timer--;
                } else if (timer == 2) {
                    for(Player p: deadPlayers) {
                        p.sendMessage("2...");
                    }
                    timer--;
                } else if (timer == 1) {
                    for(Player p : deadPlayers) {
                        p.sendMessage("1...");
                    }
                    timer--;
                } else {
                    timer--;
                }
            }
        }, 20, 20);

    }
    public void respawnFlag(String team){
        if(team.equals("Red")){
            gameDirector.redTeamFlagLocation.getBlock().setType(Material.RED_WOOL);

            gameDirector.redTeamFlagTaken = false;
            gameDirector.redTeamFlagCarrier = "NONE";

            Shulker shulker = (Shulker) gameDirector.mainArena.spawnEntity(gameDirector.redTeamFlagLocation, EntityType.SHULKER);
            LivingEntity glower = (LivingEntity) shulker;
            glower.setSilent(true);
            glower.setInvulnerable(true);
            glower.setAI(false);
            glower.setCollidable(false);
            glower.setGravity(false);
            glower.setInvisible(true);

            redGlow.addHolders(glower);
            redGlow.display(Bukkit.getOnlinePlayers());
            gameDirector.broadcastMessage("Nostromo Flag Reset", ChatColor.RED);
        }

        if(team.equals("Blue")){
            gameDirector.blueTeamFlagLocation.getBlock().setType(Material.BLUE_WOOL);

            gameDirector.blueTeamFlagTaken = false;
            gameDirector.blueTeamFlagCarrier = "NONE";

            Shulker blueshulker = (Shulker) gameDirector.mainArena.spawnEntity(gameDirector.blueTeamFlagLocation, EntityType.SHULKER);
            LivingEntity blueglower = (LivingEntity) blueshulker;

            blueglower.setSilent(true);
            blueglower.setInvulnerable(true);
            blueglower.setAI(false);
            blueglower.setCollidable(false);
            blueglower.setGravity(false);
            blueglower.setInvisible(true);

            blueGlow.addHolders(blueglower);
            blueGlow.display(Bukkit.getOnlinePlayers());

            gameDirector.broadcastMessage("Elysium Flag Reset", ChatColor.BLUE);
        }
    }

    public void slowRedFlagCarrier() {
        if(gameDirector.gameState.equals("LIVE")) {
            if(gameDirector.redTeamFlagTaken == false || gameDirector.redTeamFlagCarrier.equals(null) || gameDirector.redTeamFlagCarrier.equals("DROPPED")) {
                return;
            } else {
                Player p = Bukkit.getPlayer(gameDirector.redTeamFlagCarrier);
                if (p != null) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 2));
                }
            }
        }
    }
    public void slowBlueFlagCarrier() {
        if(gameDirector.gameState.equals("LIVE")) {
            if(gameDirector.blueTeamFlagTaken == false || gameDirector.blueTeamFlagCarrier.equals(null) || gameDirector.blueTeamFlagCarrier.equals("DROPPED")) {
                return;
            } else {
                Player p = Bukkit.getPlayer(gameDirector.blueTeamFlagCarrier);
                if (p != null) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 2));
                }
            }
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setredref")) {
            Player p = (Player) sender;
            gameDirector.redTeamRefLoc = p.getEyeLocation();
            p.sendMessage("Nostromo Refinery Pos set to: " + gameDirector.redTeamRefLoc.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setredxp")) {
            Player p = (Player) sender;
            gameDirector.redTeamXPGenLoc = p.getEyeLocation();
            p.sendMessage("Nostromo XPGen Pos set to: " + gameDirector.redTeamXPGenLoc.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setredcarepackage")) {
            Player p = (Player) sender;
            gameDirector.redTeamCarePackageLoc = p.getEyeLocation();
            p.sendMessage("Nostromo Care Package Pos set to: " + gameDirector.redTeamCarePackageLoc.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setredrespawnpoint")) {
            Player p = (Player) sender;
            gameDirector.redTeamRespawnPoint = p.getEyeLocation();
            p.sendMessage("Nostromo Respawn Holder set to: " + gameDirector.redTeamRespawnPoint.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setredspawnpoint")) {
            Player p = (Player) sender;
            gameDirector.redTeamSpawnPoint = p.getEyeLocation();
            p.sendMessage("Nostromo Default Spawn set to: " + gameDirector.redTeamSpawnPoint.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setbluerespawnpoint")) {
            Player p = (Player) sender;
            gameDirector.blueTeamRespawnPoint = p.getEyeLocation();
            p.sendMessage("Elysium Respawn Holder set to: " + gameDirector.blueTeamRespawnPoint.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setbluespawnpoint")) {
            Player p = (Player) sender;
            gameDirector.blueTeamSpawnPoint = p.getEyeLocation();
            p.sendMessage("Elysium Default Spawn set to: " + gameDirector.blueTeamSpawnPoint.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setblueref")) {
            Player p = (Player) sender;
            gameDirector.blueTeamRefLoc = p.getEyeLocation();
            p.sendMessage("Elysium Refinery Pos set to: " + gameDirector.blueTeamRefLoc.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setbluecarepackage")) {
            Player p = (Player) sender;
            gameDirector.blueTeamCarePackageLoc = p.getEyeLocation();
            p.sendMessage("Elysium Care Package Pos set to: " + gameDirector.blueTeamCarePackageLoc.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setbluexp")) {
            Player p = (Player) sender;
            gameDirector.blueTeamXPGenLoc = p.getEyeLocation();
            p.sendMessage("Elysium XPGen Pos set to: " + gameDirector.blueTeamXPGenLoc.toString());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("forcecarepackage")) {
            Player p = (Player) sender;
            spawnRedCarePackage();
            spawnBlueCarePackage();
            p.sendMessage("Forced Care Package");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("saveconfig")) {
            getConfig().set("blueTeamRefLoc", gameDirector.blueTeamRefLoc);
            getConfig().set("blueTeamXPGenLoc", gameDirector.blueTeamXPGenLoc);
            getConfig().set("blueTeamCarePackageLoc", gameDirector.blueTeamCarePackageLoc);
            getConfig().set("blueTeamRespawnPoint", gameDirector.blueTeamRespawnPoint);
            getConfig().set("blueTeamSpawnPoint", gameDirector.blueTeamSpawnPoint);
            getConfig().set("blueTeamFlagLocation", gameDirector.blueTeamFlagLocation);

            getConfig().set("redTeamRefLoc", gameDirector.redTeamRefLoc);
            getConfig().set("redTeamXPGenLoc", gameDirector.redTeamXPGenLoc);
            getConfig().set("redTeamCarePackageLoc", gameDirector.redTeamCarePackageLoc);
            getConfig().set("redTeamRespawnPoint", gameDirector.redTeamRespawnPoint);
            getConfig().set("redTeamSpawnPoint", gameDirector.redTeamSpawnPoint);
            getConfig().set("redTeamFlagLocation", gameDirector.redTeamFlagLocation);
            saveConfig();
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("gamestate")) {
            Player p = (Player) sender;
            gameDirector.gameState = args[0];
            p.sendMessage("Set Gamestate to: " + args[0]);
            if(args[0].equals("LIVE")){
                for(Player pl : Bukkit.getOnlinePlayers()) {
                    if(pl.getName().equals("StuboUK")) {
                        continue;
                    } else {
                        pl.setHealth(0.00);
                    }
                }
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setblueteamflag")) {
            Player p = (Player) sender;
            Location oldLoc = gameDirector.blueTeamFlagLocation;
            if(oldLoc.getBlock().getType().equals(Material.BLUE_WOOL)) {
                List<Entity> nearbyEntites = (List<Entity>) oldLoc.getNearbyEntities(1, 1, 1);
                for(Entity ent : nearbyEntites){
                    if(ent.getType() == EntityType.SHULKER) {
                        blueGlow.removeHolders(ent);
                        ent.remove();
                        blueGlow.display(Bukkit.getOnlinePlayers());
                        gameDirector.blueTeamFlagTaken = false;
                        gameDirector.blueTeamFlagCarrier = "NONE";
                    }
                }
                oldLoc.getBlock().setType(Material.AIR);
            }
            gameDirector.blueTeamFlagLocation = p.getLocation();
            gameDirector.blueTeamFlagLocation.getBlock().setType(Material.BLUE_WOOL);

            gameDirector.blueTeamFlagTaken = false;
            gameDirector.blueTeamFlagCarrier = "NONE";

            Shulker blueshulker = (Shulker) p.getWorld().spawnEntity(gameDirector.blueTeamFlagLocation, EntityType.SHULKER);
            LivingEntity blueglower = (LivingEntity) blueshulker;

            blueglower.setSilent(true);
            blueglower.setInvulnerable(true);
            blueglower.setAI(false);
            blueglower.setCollidable(false);
            blueglower.setGravity(false);
            blueglower.setInvisible(true);

            blueGlow.addHolders(blueglower);
            blueGlow.display(Bukkit.getOnlinePlayers());

            p.sendMessage("Set Blue Team Flag Location to: " + p.getLocation());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setredteamflag")) {
            Player p = (Player) sender;
            Location oldLoc = gameDirector.redTeamFlagLocation;
            if(oldLoc.getBlock().getType().equals(Material.RED_WOOL)) {
                List<Entity> nearbyEntites = (List<Entity>) oldLoc.getNearbyEntities(1, 1, 1);
                for(Entity ent : nearbyEntites){
                    if(ent.getType() == EntityType.SHULKER) {
                        redGlow.removeHolders(ent);
                        ent.remove();
                        redGlow.display(Bukkit.getOnlinePlayers());
                        gameDirector.redTeamFlagTaken = false;
                        gameDirector.redTeamFlagCarrier = "NONE";
                    }
                }
                oldLoc.getBlock().setType(Material.AIR);
            }
            gameDirector.redTeamFlagLocation = p.getLocation();
            gameDirector.redTeamFlagLocation.getBlock().setType(Material.RED_WOOL);

            gameDirector.redTeamFlagTaken = false;
            gameDirector.redTeamFlagCarrier = "NONE";

            Shulker shulker = (Shulker) p.getWorld().spawnEntity(gameDirector.redTeamFlagLocation, EntityType.SHULKER);
            LivingEntity glower = (LivingEntity) shulker;
            glower.setSilent(true);
            glower.setInvulnerable(true);
            glower.setAI(false);
            glower.setCollidable(false);
            glower.setGravity(false);
            glower.setInvisible(true);

            redGlow.addHolders(glower);
            redGlow.display(Bukkit.getOnlinePlayers());

            p.sendMessage("Set Red Team Flag Location to: " + p.getLocation());
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        deadPlayers.add(e.getPlayer());
        txsnMember p = getProfile(e.getPlayer().getName());
        if (p.team.equals("Red")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    e.getPlayer().teleport(gameDirector.redTeamRespawnPoint);
                }
            }, 3L); //20 Tick (1 Second) delay before run() is called

        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    e.getPlayer().teleport(gameDirector.blueTeamRespawnPoint);
                }
            }, 3L); //20 Tick (1 Second) delay before run() is called
        }

    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        try {
            Player p = event.getPlayer();
            txsnMember memJoin = new txsnMember(p.getName());
            updateTXSNMembers();
            if (memJoin.team == null)
                p.kickPlayer("Please do the following in Ignition Chat: !mcusername " + p.getName());

            event.setJoinMessage("Level " + memJoin.level + " " + memJoin.position + " " + memJoin.username + " has joined from " + memJoin.printableTeam());
            p.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "================= " + ChatColor.DARK_RED + ChatColor.BOLD + "[ IGNITION MSI ]" + ChatColor.DARK_GREEN + " ==================");
            p.sendMessage(ChatColor.DARK_GREEN + "=");
            p.sendMessage(ChatColor.DARK_GREEN + "= [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "USERNAME: " + ChatColor.GOLD + memJoin.username + ChatColor.DARK_GREEN + " ] " + ChatColor.DARK_GREEN + " [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "POSITION: " + ChatColor.GOLD + memJoin.position + ChatColor.DARK_GREEN + " ]");
            p.sendMessage(ChatColor.DARK_GREEN + "= [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL: " + ChatColor.GOLD + memJoin.level + ChatColor.DARK_GREEN + " ] " + ChatColor.DARK_GREEN + " [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "TXSNCOIN: " + ChatColor.GOLD + memJoin.txsncoin + ChatColor.DARK_GREEN + " ] " + "[ " + ChatColor.GREEN + "" + ChatColor.BOLD + "XP: " + ChatColor.GOLD + memJoin.xp + ChatColor.DARK_GREEN + " ]");
            p.sendMessage(ChatColor.DARK_GREEN + "=");
            p.sendMessage(ChatColor.DARK_GREEN + "= [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "TEAM: " + memJoin.printableTeam() + ChatColor.DARK_GREEN + " ] " + ChatColor.DARK_GREEN + " [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "TP: " + ChatColor.GOLD + memJoin.teampower + ChatColor.DARK_GREEN + " ]");
            p.sendMessage(ChatColor.DARK_GREEN + "=");
            p.sendMessage(ChatColor.DARK_GREEN + "= [ " + ChatColor.GREEN + "" + ChatColor.BOLD + "Welcome to Operation: CTF - You are integrated" + ChatColor.DARK_GREEN + " ]");
            p.sendMessage(ChatColor.DARK_GREEN + "=");
            p.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "===================================================");

            if(memJoin.team.equals("Red")){
                getServer().dispatchCommand(getServer().getConsoleSender(), "coloredplayernames set " + p.getName() + " red");
            }
            if(memJoin.team.equals("Blue")){
                getServer().dispatchCommand(getServer().getConsoleSender(), "coloredplayernames set " + p.getName() + " blue");
            }
            if(memJoin.team.equals("Green")){
                getServer().dispatchCommand(getServer().getConsoleSender(), "coloredplayernames set " + p.getName() + " green");
            }
        } catch(Exception e) { getLogger().severe("Error in onPlayerJoin!"); }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = (Player) e.getPlayer();
        txsnMember member = getProfile(player.getName());
        if(member.team.equals("Green")) {
            Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(0,255,0), 1);
            Location loc = player.getLocation().add(0, 1, 0);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0,0,0, green);
        }
        if(member.team.equals("Blue")) {
            Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(0,0,255), 1);
            Location loc = player.getLocation().add(0, 1, 0);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0,0,0, blue);
        }
        if(member.team.equals("Red")) {
            Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(255,0,0), 1);
            Location loc = player.getLocation().add(0, 1, 0);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0,0,0, red);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player p = event.getPlayer();
        Item i = event.getItem();

        if(i.getName().equals("Red Wool")) {
            if(getProfile(p.getName()).team.equals("Red")) {
                i.remove();
                respawnFlag("Red");
                return;
            }
            gameDirector.broadcastMessage("NOSTROMO'S FLAG TAKEN BY " + txsn.getNameColored(p.getName()), ChatColor.RED);
            redGlow.addHolders(p);
            redGlow.display(Bukkit.getOnlinePlayers());
            gameDirector.redTeamFlagTaken = true;
            gameDirector.redTeamFlagCarrier = p.getName();
        } //When Red Team's Flag Gets Picked Up.
        if(i.getName().equals("Blue Wool")) {
            if(getProfile(p.getName()).team.equals("Blue")) {
                i.remove();
                respawnFlag("Blue");
                return;
            }
            gameDirector.broadcastMessage("ELYSIUM'S FLAG TAKEN BY " + txsn.getNameColored(p.getName()), ChatColor.BLUE);
            blueGlow.addHolders(p);
            blueGlow.display(Bukkit.getOnlinePlayers());
            gameDirector.blueTeamFlagTaken = true;
            gameDirector.blueTeamFlagCarrier = p.getName();
        } //When Blue Team's Flag Gets Picked Up.
    }

    @EventHandler
    public void onEntityDamageByBlockEvent(EntityDamageByBlockEvent event) {
        if(((Item) event.getEntity()).getItemStack().getType().equals(Material.BLUE_WOOL)) {
            respawnFlag("Blue");
        }
        if(((Item) event.getEntity()).getItemStack().getType().equals(Material.RED_WOOL)) {
            respawnFlag("Red");
        }
    }

    @EventHandler
    public void onItemDrop (PlayerDropItemEvent e) {

        Player p = e.getPlayer();
        Item i = e.getItemDrop();

        if (i.getItemStack().getType() == Material.RED_WOOL) {
            gameDirector.broadcastMessage("NOSTROMO'S FLAG HAS BEEN DROPPED BY: " + txsn.getNameColored(p.getName()), ChatColor.RED);
            gameDirector.redTeamFlagTaken = false;
            gameDirector.redTeamFlagCarrier = "DROPPED";
            redGlow.removeHolders(p);
            redGlow.addHolders(i);
            redGlow.display(Bukkit.getOnlinePlayers());
        }
        if (i.getItemStack().getType() == Material.BLUE_WOOL) {
            gameDirector.broadcastMessage("ELYSIUM'S FLAG HAS BEEN DROPPED BY: " + txsn.getNameColored(p.getName()), ChatColor.BLUE);
            gameDirector.blueTeamFlagTaken = false;
            gameDirector.blueTeamFlagCarrier = "DROPPED";
            blueGlow.removeHolders(p);
            blueGlow.addHolders(i);
            blueGlow.display(Bukkit.getOnlinePlayers());
        }

    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        Player p = e.getPlayer();
        int radius = 1;
        if (block.getType() == Material.RED_WOOL) {
            gameDirector.redTeamFlagTaken = false;
            gameDirector.redTeamFlagCarrier = "NONE";

            Shulker shulker = (Shulker) p.getWorld().spawnEntity(block.getLocation(), EntityType.SHULKER);
            LivingEntity glower = (LivingEntity) shulker;

            glower.setSilent(true);
            glower.setInvulnerable(true);
            glower.setAI(false);
            glower.setCollidable(false);
            glower.setGravity(false);
            glower.setInvisible(true);

            redGlow.removeHolders(p);
            redGlow.addHolders(glower);
            redGlow.display(Bukkit.getOnlinePlayers());
            for (int x = -(radius); x <= radius; x ++)
            {
                for (int y = -(radius); y <= radius; y ++)
                {
                    for (int z = -(radius); z <= radius; z ++)
                    {
                        if (block.getRelative(x,y,z).getType() == Material.BLUE_WOOL)
                        {
                            gameDirector.gameState = "ELYWIN";
                            gameDirector.broadcastMessage("Elysium has won the operation!", ChatColor.BLUE);
                        }
                    }
                }
            }
        }
        if (block.getType() == Material.BLUE_WOOL) {
            gameDirector.blueTeamFlagTaken = false;
            gameDirector.blueTeamFlagCarrier = "NONE";

            Shulker blueshulker = (Shulker) p.getWorld().spawnEntity(block.getLocation(), EntityType.SHULKER);
            LivingEntity blueglower = (LivingEntity) blueshulker;
            blueglower.setSilent(true);
            blueglower.setInvulnerable(true);
            blueglower.setAI(false);
            blueglower.setCollidable(false);
            blueglower.setGravity(false);
            blueglower.setInvisible(true);
            blueGlow.removeHolders(p);
            blueGlow.addHolders(blueglower);
            blueGlow.display(Bukkit.getOnlinePlayers());
            for (int x = -(radius); x <= radius; x++) {
                for (int y = -(radius); y <= radius; y++) {
                    for (int z = -(radius); z <= radius; z++) {
                        if (block.getRelative(x, y, z).getType() == Material.RED_WOOL) {
                            gameDirector.gameState = "NOSWIN";
                            gameDirector.broadcastMessage("Nostromo has won the operation!", ChatColor.RED);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void BlockBreakEvent(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player p = e.getPlayer();
        if (block.getType() == Material.RED_WOOL) {
            List<Entity> nearbyEntites = (List<Entity>) block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1);
            for(Entity ent : nearbyEntites){
                if(ent.getType() == EntityType.SHULKER) {
                    redGlow.removeHolders(ent);
                    ent.remove();
                    redGlow.display(Bukkit.getOnlinePlayers());
                    gameDirector.redTeamFlagTaken = false;
                    gameDirector.redTeamFlagCarrier = "NONE";
                }
            }
        }
        if (block.getType() == Material.BLUE_WOOL) {
            List<Entity> nearbyEntites = (List<Entity>) block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1);
            for(Entity ent : nearbyEntites){
                if(ent.getType() == EntityType.SHULKER) {
                    blueGlow.removeHolders(ent);
                    ent.remove();
                    blueGlow.display(Bukkit.getOnlinePlayers());
                    gameDirector.blueTeamFlagTaken = false;
                    gameDirector.blueTeamFlagCarrier = "NONE";
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

