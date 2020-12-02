package me.hihii55.alytalo55;

import me.hihii55.alytalo55.astronomy.AlytaloAstronomy;
import me.hihii55.alytalo55.talo55.Talo55;
import me.hihii55.alytalo55.weather.AlytaloWeather;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;



//Alla olevasta luokasta luotu olio toimii pluginin runkona. Kun plugin käynnistetään, suoritetaan onEnable()-metodi.
//Vastaavasti suljettaessa kutsutaan onDisable()-metodia.

public final class AlytaloMain extends JavaPlugin {

    private AlytaloListener listener;
    private ResourceManager resourceManager;
    private AlytaloCommand commands;
    private AlytaloWeather weather;
    private AlytaloAstronomy astronomy;
    private Talo55 talo55;

    //Käynnistää kaikki pluginin toiminnan kannalta tärkeät palvelut (ResourceManager, AlytaloWeather, AlytaloAstronomy, Talo55)
    @Override
    public void onEnable() {
        listener = new AlytaloListener(this);
        resourceManager = new ResourceManager(this);
        commands = new AlytaloCommand(this);
        resourceManager.loadConfig();
        resourceManager.loadLang(resourceManager.getDefaultLang());
        resourceManager.loadRegions();
        resourceManager.loadWeatherGadgets();
        resourceManager.loadTalo55Gadgets();
        getCommand("alytalo").setExecutor(commands);
        getServer().getPluginManager().registerEvents(listener,this);
        astronomy = new AlytaloAstronomy(this, resourceManager.getAstronomyQueryUrl(), resourceManager.getLatitude(), resourceManager.getLongitude());
        astronomy.start();
        weather = new AlytaloWeather(this,resourceManager.getWeatherQueryUrl(), resourceManager.getWeatherTownName(), resourceManager.getWeatherInterval(), resourceManager.getWeatherArchiveSize());
        weather.start();
        talo55 = new Talo55(this, resourceManager.getTalo55ProviderAddress(), resourceManager.getTalo55Port());
        talo55.start();


    }

    //Sulkee kaikki pluginin toiminnan kannalta tärkeät palvelut
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Shutting down weather service");
        astronomy.stop();
        weather.stop();
        talo55.stop();
    }

    public AlytaloListener getListener(){
        return this.listener;
    }

    public ResourceManager getResourceManager(){
        return this.resourceManager;
    }

    public AlytaloCommand getCommands() { return this.commands; }

    public AlytaloWeather getWeather() { return this.weather; }

    public Talo55 getTalo55(){ return this.talo55; }
}
