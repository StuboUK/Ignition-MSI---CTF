package uk.txsn.stubouk.ignition.msi.ignitionmsi;

import org.bukkit.ChatColor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class txsnMember {
    String team;
    String username;
    String discordID;
    String level;
    String xp;
    String bio;
    String position;
    String txsncoin;
    String teampower;
    String mcname;
    public txsnMember(String mcname) {
        this.mcname = mcname;
        try {
            Statement statement = IgnitionMsi.connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM txsn.members mem\n" +
                    "INNER JOIN\n" +
                    "teams t \n" +
                    "INNER JOIN\n" +
                    "discorduser du \n" +
                    "WHERE mem.did = du.userid AND du.userid = t.userid AND mem.mcusername = \"" + mcname + "\";");
            while (result.next()) {
                this.discordID = result.getString("did");
                this.username = result.getString("name");
                this.team = result.getString("team");
                this.level = result.getString("level");
                this.xp = result.getString("xp");
                this.bio = result.getString("bio");
                this.position = result.getString("position");
                this.txsncoin = result.getString("txsncoin");
                this.teampower = result.getString("teampower");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int permLevel(String mcname) {
        int permLevel = 1;
        try {
            Statement statement = IgnitionMsi.connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM txsn.modlist WHERE userid = '" + this.discordID + "'");
            while (result.next()) {
                if(result.getString("COUNT(*)") == "1"){ permLevel = 2; }
            }
            result = statement.executeQuery("SELECT COUNT(*) FROM txsn.corememberlist WHERE userid = '" + this.discordID + "'");
            while (result.next()) {
                if(result.getString("COUNT(*)") == "1"){ permLevel = 3; }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return permLevel;
    }

    public String printableTeam(){
        String teamName = "Unknown";

        if(team.equals("Green")){
            teamName = ChatColor.GREEN + "" + ChatColor.BOLD + "PanaceaGC";
        }
        if(team.equals("Blue")){
            teamName = ChatColor.BLUE + "" + ChatColor.BOLD + "Elysium";
        }
        if(team.equals("Red")){
            teamName = ChatColor.RED + "" + ChatColor.BOLD + "Nostromo";
        }
        System.out.println(team);
        return teamName;
    }
}
