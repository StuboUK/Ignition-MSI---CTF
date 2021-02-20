package uk.txsn.stubouk.ignition.msi.ignitionmsi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;


public class gameDirector {



    boolean redTeamFlagTaken = false;
    boolean blueTeamFlagTaken = false;
    String redTeamFlagCarrier = null;
    String blueTeamFlagCarrier = null;
    int redTeamKills = 0;
    int blueTeamKills = 0;

    int respawnTimer = 30;
    String gameState = "WARMUP";
    World mainArena = Bukkit.getWorld("world");

    Location blueTeamRefLoc = new Location(mainArena, 0.0, 0.0, 0.0);
    Location blueTeamXPGenLoc = new Location(mainArena, 0.0, 0.0, 0.0);
    Location blueTeamCarePackageLoc = new Location(mainArena, 0.0, 0.0, 0.0);
    Location blueTeamRespawnPoint = new Location(mainArena, 0.0, 0.0, 0.0);
    Location blueTeamSpawnPoint = new Location(mainArena, 0.0, 0.0, 0.0);
    Location blueTeamFlagLocation = new Location(mainArena, 0.0, 0.0, 0.0);

    Location redTeamCarePackageLoc = new Location(mainArena, 0.0, 0.0, 0.0);
    Location redTeamRefLoc = new Location(mainArena, 0.0, 0.0, 0.0);
    Location redTeamXPGenLoc = new Location(mainArena, 0.0, 0.0, 0.0);
    Location redTeamRespawnPoint = new Location(mainArena, 0.0, 0.0, 0.0);
    Location redTeamSpawnPoint = new Location(mainArena, 0.0, 0.0, 0.0);
    Location redTeamFlagLocation = new Location(mainArena, 0.0, 0.0, 0.0);

    public void broadcastMessage(String msg, ChatColor color) {
        Bukkit.broadcastMessage(
                ChatColor.DARK_GREEN + "" +
                ChatColor.BOLD +
                "[" +
                        ChatColor.GREEN +
                        ChatColor.BOLD +
                "IGN " +
                ChatColor.GOLD +
                ChatColor.BOLD +
                "GameDirector" +
                ChatColor.DARK_GREEN + ChatColor.BOLD +
                "]" +
                ChatColor.RED + ChatColor.BOLD +
                "#> " +
                color + ChatColor.BOLD +
                msg);
    }
}
