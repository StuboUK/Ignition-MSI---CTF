package uk.txsn.stubouk.ignition.msi.ignitionmsi;

import org.bukkit.ChatColor;

public class txsn {

    public static String getNameColored(String mcname) {
        txsnMember member = new txsnMember(mcname);
        if(member.team.equals("Green")){
            return ChatColor.GREEN + mcname;
        }
        if(member.team.equals("Blue")){
            return ChatColor.BLUE + mcname;
        }
        if(member.team.equals("Red")){
            return ChatColor.RED + mcname;
        }
        return mcname;
    }

}
