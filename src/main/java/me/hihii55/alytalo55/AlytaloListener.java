package me.hihii55.alytalo55;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;


//Tämä luokka sisältää metodeita, joita kutsutaan kun jokin tapahtuma tapahtuu pelissä (esim. pelaaja liikkuu,
//kirjoittaa viestin, rikkoo palikan...)
public class AlytaloListener implements Listener {


    private AlytaloMain plugin;
    private boolean buttonState;



    public AlytaloListener(AlytaloMain plugin){

        this.plugin = plugin;
        buttonState = false;

    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){


    }

    // Kun pelaaja liikkuu, katsotaan onko pelaajan uusi sijainti sallittujen suorakulmaisten särmiöiden sisällä.
    // Jos ei, liikkuminen estetään, paitsi jos pelaajalla on erikoisoikeus.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event){

        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;

        ArrayList<Cuboid> cuboids = this.plugin.getResourceManager().getAccessibleArea();
        Location to = event.getTo();

        for(Cuboid cuboid : cuboids){
            if(!cuboid.isInside(to))
                event.setCancelled(true);
        }


    }

    // Estetään pelaajaa muuttamasta ympäristöä (rakentaminen ja tuhoaminen), paitsi jos hänellä on siihen oikeudet.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent event) {

        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;

        event.setCancelled(true);
      //  event.getPlayer().sendMessage(NO_GRIEF);
    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onBlockPlaceEvent(BlockPlaceEvent event){

        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;

        event.setCancelled(true);
     //   event.getPlayer().sendMessage(NO_GRIEF);

    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event){

        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;

        event.setCancelled(true);
    //    event.getPlayer().sendMessage(NO_GRIEF);

    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event){

        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;

        event.setCancelled(true);
      //  event.getPlayer().sendMessage(NO_GRIEF);

    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onBlockIgniteEvent(BlockIgniteEvent event){
        if(event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL){
            event.setCancelled(true);
            return;
        }

        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;
        event.setCancelled(true);
       // event.getPlayer().sendMessage(NO_GRIEF);


    }

    // Tämä metodi tutkii sitä, kun pelaaja klikkaa jotain esinettä pelissä. Tässä tutkitaan esimerkiksi,
    // osuiko pelaajan klikkaus LED-valoa säätelevään nappiin. Jos näin oli, muutetaan seuraavan Arduinolle lähtevän paketin
    // sisältöä uuden tilan mukaiseksi.
    @EventHandler
    public void onBlockClick(PlayerInteractEvent e){
        if(e.getClickedBlock().getLocation().equals(plugin.getResourceManager().getTalo55GadgetsLocations().get("table-lamp-switch"))){
            buttonState = !buttonState;
            plugin.getTalo55().setOutboundField(6, buttonState?(byte)1:(byte)0);
        }
        Player p = e.getPlayer();
        if(!(plugin.getCommands().isEditingPlayer(e.getPlayer().getName())))
            return;
        if(p.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SPADE)
            p.sendMessage("x: "+e.getClickedBlock().getX()+" y: "+e.getClickedBlock().getY()+" z: "+e.getClickedBlock().getZ());
        else if(p.getInventory().getItemInMainHand().getType() == Material.DIAMOND_AXE){
            p.sendMessage(Byte.toString(e.getClickedBlock().getRelative(BlockFace.DOWN).getData()));
            p.sendMessage(Byte.toString(e.getClickedBlock().getRelative(BlockFace.DOWN).getState().getRawData()));
            p.sendMessage(e.getClickedBlock().getRelative(BlockFace.DOWN).getType().toString());
            e.getClickedBlock().getRelative(BlockFace.DOWN).setType(Material.STATIONARY_WATER);
            e.getClickedBlock().getRelative(BlockFace.DOWN).setData((byte)7);
        }
    }


    // Liittyy taas ympäristön muokkauksen estämiseen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        if(plugin.getCommands().isEditingPlayer(event.getPlayer().getName()))
            return;
        if(event.getRightClicked() instanceof ItemFrame)
             event.setCancelled(true);

    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onDamageEvent(EntityDamageEvent e){

        if(e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
            if (ee.getDamager() instanceof Player) {
                Player p = (Player) ee.getDamager();
                if(plugin.getCommands().isEditingPlayer(p.getName()))
                    return;
            }
        }
        e.setCancelled(true);
    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onFrameBreak(HangingBreakEvent e){


        if(e.getCause() == HangingBreakEvent.RemoveCause.ENTITY){
            if(e instanceof HangingBreakByEntityEvent) {
                HangingBreakByEntityEvent ee = (HangingBreakByEntityEvent) e;
                if(ee.getRemover() instanceof Player){
                    Player p = (Player) ee.getRemover();
                    if(plugin.getCommands().isEditingPlayer(p.getName()))
                        return;
                }
            }
        }

        e.setCancelled(true);


    }

    // sama kuin edellinen
    @EventHandler(priority=EventPriority.LOWEST)
    public void onFramePlace(HangingPlaceEvent e){

        if(plugin.getCommands().isEditingPlayer(e.getPlayer().getName()))
            return;

        e.setCancelled(true);
    }

    // estetään sään muuttuminen (=sateen alkaminen tai päättyminen) itsestään.
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e){

        if(e.toWeatherState() != plugin.getWeather().isRaining())
            e.setCancelled(true);

    }

    // estetään salamaniskut
    @EventHandler
    public void onLightningStrike(LightningStrikeEvent e){

        if(plugin.getWeather().isThunder())
            return;
        e.setCancelled(true);
    }


    // estetään ympäristön muuttuminen
    @EventHandler
    public void onBlockFade(BlockFadeEvent e){

        e.setCancelled(true);

    }


    // sama kuin edellinen
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeavesDecay(LeavesDecayEvent e){

        e.setCancelled(true);

    }

    // sama kuin edellinen
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e){

        e.setCancelled(true);
    }








}
