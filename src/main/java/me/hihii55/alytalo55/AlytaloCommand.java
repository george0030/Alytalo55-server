package me.hihii55.alytalo55;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;


// Tämä luokka kuuntelee pelaajien suorittamia komentoja
public class AlytaloCommand implements CommandExecutor{

    private AlytaloMain plugin;
    private HashSet<String> editingPlayers;


   public AlytaloCommand(AlytaloMain plugin){

       this.plugin = plugin;
       this.editingPlayers = new HashSet<String>();


   }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (!editingPlayers.contains(sender.getName())){
                    editingPlayers.add(sender.getName());
                    sender.sendMessage(plugin.getResourceManager().getCaption("EDIT_ENABLED",plugin.getResourceManager().getDefaultLang()));
                    return true;
                }
                else {
                    editingPlayers.remove(sender.getName());
                    sender.sendMessage(plugin.getResourceManager().getCaption("EDIT_DISABLED",plugin.getResourceManager().getDefaultLang()));
                    return true;
                }
            }

        }

        return false;
    }

    public HashSet<String> getEditingPlayers(){
       return this.editingPlayers;
    }

    public boolean isEditingPlayer(String playername){
        return this.editingPlayers.contains(playername);
    }

}
