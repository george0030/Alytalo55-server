package me.hihii55.alytalo55;


import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;


//Huolehtii konfiguraatioiden ja muiden pluginin käyttämien tiedostojen lukemisesta.
public class ResourceManager {

    private AlytaloMain plugin;

    private String lang;


    private ArrayList<Cuboid> weatherArea;
    private ArrayList<Cuboid> accessibleArea;

    private File regionsFile;
    private FileConfiguration regionsConfig;

    private HashMap<String, FileConfiguration> langConfigs;

    private String defaultLang;

    private String weatherQueryUrl;
    private String weatherTownName;
    private long weatherInterval;
    private int weatherArchiveSize;
    private String localTimeZone;
    private double rainThreshold;
    private boolean winter;
    private double maxPrecipitation;

    private File weatherGadgetsFile;
    private FileConfiguration weatherGadgetsConfig;

    private HashMap<String, Location> weatherGadgetsLocations;

    private long lunarMonthDuration;
    private long firstFullMoon;
    private String astronomyQueryUrl;
    private String latitude;
    private String longitude;
    private int astronomyRetryInterval;

    private String talo55ProviderAddress;
    private int talo55Port;

    private long doorbellCooldown;

    private File talo55GadgetsFile;
    private FileConfiguration talo55GadgetsConfig;

    private HashMap<String, Location> talo55GadgetsLocations;




    public ResourceManager(AlytaloMain plugin){

        this.plugin = plugin;
        langConfigs = new HashMap<String, FileConfiguration>();
        weatherArea = new ArrayList<Cuboid>();
        accessibleArea = new ArrayList<Cuboid>();
        weatherGadgetsLocations = new HashMap<String, Location>();
        talo55GadgetsLocations = new HashMap<String, Location>();

    }

    //Lukee konfiguraation ja sijoittaa luetut tiedot ajonaikaisiin muuttujiin
    public void loadConfig(){

        this.plugin.saveDefaultConfig();

        defaultLang = this.plugin.getConfig().getString("default-lang");
        weatherQueryUrl = this.plugin.getConfig().getString("weather-query-url");
        weatherTownName = this.plugin.getConfig().getString("weather-townname");
        weatherInterval = this.plugin.getConfig().getLong("weather-interval");
        weatherArchiveSize = this.plugin.getConfig().getInt("store-old-weather-data");
        localTimeZone = this.plugin.getConfig().getString("local-timezone");
        rainThreshold = this.plugin.getConfig().getDouble("rain-threshold");
        winter = this.plugin.getConfig().getBoolean("winter");
        maxPrecipitation = this.plugin.getConfig().getDouble("max-precipitation") < 0.0 ? 0.0 : this.plugin.getConfig().getDouble("max-precipitation");
        lunarMonthDuration = this.plugin.getConfig().getLong("lunar-month-duration");
        firstFullMoon = this.plugin.getConfig().getLong("first-full-moon");
        astronomyQueryUrl = this.plugin.getConfig().getString("astronomy-query-url");
        latitude = this.plugin.getConfig().getString("latitude");
        longitude = this.plugin.getConfig().getString("longitude");
        astronomyRetryInterval = this.plugin.getConfig().getInt("astronomy-retry-interval-minutes");
        talo55ProviderAddress = this.plugin.getConfig().getString("talo55-provider-address");
        talo55Port = this.plugin.getConfig().getInt("talo55-port");
        doorbellCooldown = this.plugin.getConfig().getLong("doorbell-cooldown");

    }

    //Lukee kielitiedoston
    public void loadLang(String lang){

       File langFile = new File(plugin.getDataFolder(), "lang_"+lang+".yml");

       if(!langFile.exists()){
           langFile.getParentFile().mkdirs();
           plugin.saveResource("lang_"+lang+".yml",false);
       }

       FileConfiguration langConfig = new YamlConfiguration();

       try{
           langConfig.load(langFile);
       }
       catch(IOException | InvalidConfigurationException e){
           plugin.getLogger().log(Level.SEVERE, "Could not load the language file lang_"+lang+".yml . Plugin will not function properly.");
           e.printStackTrace();
       }

       langConfigs.put(lang, langConfig);
       plugin.getLogger().log(Level.INFO, "Successfully loaded language file lang_"+lang+".yml");

    }

    //Antaa tietyn kielisen lausunnon
    public String getCaption(String caption, String lang){

        return langConfigs.get(lang).getString(caption, caption);
    }

    public void readConfig(){



    }

    //Lataa tiedoston, joka sisältää suorakulmaiset särmiöt siitä, missä pelaajat saavat liikkua
    public void loadRegions(){

        regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        if(!regionsFile.exists()){
            regionsFile.getParentFile().mkdirs();
            plugin.saveResource("regions.yml",false);

        }

        regionsConfig = new YamlConfiguration();

        try {
            regionsConfig.load(regionsFile);
        }
        catch(IOException | InvalidConfigurationException e){
            plugin.getLogger().log(Level.SEVERE, "Could not load regions.yml . Plugin will not function properly.");
            e.printStackTrace();
        }

        //TODO: Lisää regions-tekstin varsinainen lukeminen ja tallentaminen muuttujaan

        ConfigurationSection wA = regionsConfig.getConfigurationSection("weather-area");

        //TODO: Weather-Area

        ConfigurationSection aA = regionsConfig.getConfigurationSection("accessible-area");
        Set<String> aACuboids = aA.getKeys(false);
        Iterator<String> aACuboidIterator = aACuboids.iterator();
        int successfulCuboids = 0;

        while(aACuboidIterator.hasNext()){

            ConfigurationSection aACuboid = aA.getConfigurationSection(aACuboidIterator.next());


            int x1;
            int y1;
            int z1;
            int x2;
            int y2;
            int z2;

            if(aACuboid.isInt("x1"))
                x1 = aACuboid.getInt("x1");
            else{
                plugin.getLogger().warning("Invalid x1 coordinate at "+aACuboid.getCurrentPath()+". The cuboid in question could not be loaded. Please take a look at the regions.yml file.");
                continue;

            }

            if(aACuboid.isInt("y1"))
                y1 = aACuboid.getInt("y1");
            else{
                plugin.getLogger().warning("Invalid y1 coordinate at "+aACuboid.getCurrentPath()+". The cuboid in question could not be loaded. Please take a look at the regions.yml file.");
                continue;

            }

            if(aACuboid.isInt("z1"))
                z1 = aACuboid.getInt("z1");
            else{
                plugin.getLogger().warning("Invalid z1 coordinate at "+aACuboid.getCurrentPath()+". The cuboid in question could not be loaded. Please take a look at the regions.yml file.");
                continue;

            }

            if(aACuboid.isInt("x2"))
                x2 = aACuboid.getInt("x2");
            else{
                plugin.getLogger().warning("Invalid x2 coordinate at "+aACuboid.getCurrentPath()+". The cuboid in question could not be loaded. Please take a look at the regions.yml file.");
                continue;

            }

            if(aACuboid.isInt("y2"))
                y2 = aACuboid.getInt("y2");
            else{
                plugin.getLogger().warning("Invalid y2 coordinate at "+aACuboid.getCurrentPath()+". The cuboid in question could not be loaded. Please take a look at the regions.yml file.");
                continue;

            }

            if(aACuboid.isInt("z2"))
                z2 = aACuboid.getInt("z2");
            else{
                plugin.getLogger().warning("Invalid z2 coordinate at "+aACuboid.getCurrentPath()+". The cuboid in question could not be loaded. Please take a look at the regions.yml file.");
                continue;

            }


            accessibleArea.add(new Cuboid(x1,y1,z1,x2,y2,z2));
            successfulCuboids++;

        }

        plugin.getLogger().log(Level.INFO, "Successfully loaded "+successfulCuboids+" cuboids.");



    }


// Lataa tiedoston, joka ilmaisee säädataa hyödyntävien "laitteiden", kuten sademittarin tai lämpömittarin sijainnit pelissä.
    public void loadWeatherGadgets(){

        weatherGadgetsFile = new File(plugin.getDataFolder(), "weather_gadgets.yml");
        if(!weatherGadgetsFile.exists()){
            weatherGadgetsFile.getParentFile().mkdirs();
            plugin.saveResource("weather_gadgets.yml",false);

        }

        weatherGadgetsConfig = new YamlConfiguration();

        try {
            weatherGadgetsConfig.load(weatherGadgetsFile);
        }
        catch(IOException | InvalidConfigurationException e){
            plugin.getLogger().log(Level.SEVERE, "Could not load weather_gadgets.yml . Plugin will not function properly.");
            e.printStackTrace();
        }

        Set<String> gadgetsKeys = weatherGadgetsConfig.getKeys(false);
        Iterator<String> gadgetsIterator = gadgetsKeys.iterator();
        int successfulCoordinates = 0;

        while(gadgetsIterator.hasNext()){

            String weatherGadgetName = gadgetsIterator.next();
            ConfigurationSection wgSection = weatherGadgetsConfig.getConfigurationSection(weatherGadgetName);

            double x;
            double y;
            double z;

            if(wgSection.isInt("x"))
                x = wgSection.getInt("x");
            else {
                plugin.getLogger().warning("Invalid x coordinate at " + wgSection.getCurrentPath() + ". The coordinates in question could not be loaded. Please take a look at the weather_gadgets.yml file.");
                continue;
            }

            if(wgSection.isInt("y"))
                y = wgSection.getInt("y");
            else {
                plugin.getLogger().warning("Invalid y coordinate at " + wgSection.getCurrentPath() + ". The coordinates in question could not be loaded. Please take a look at the weather_gadgets.yml file.");
                continue;
            }

            if(wgSection.isInt("z"))
                z = wgSection.getInt("z");
            else {
                plugin.getLogger().warning("Invalid z coordinate at " + wgSection.getCurrentPath() + ". The coordinates in question could not be loaded. Please take a look at the weather_gadgets.yml file.");
                continue;
            }

            weatherGadgetsLocations.put(weatherGadgetName, new Location(plugin.getServer().getWorlds().get(0), x, y, z));
            successfulCoordinates++;

        }

        plugin.getLogger().log(Level.INFO, "Successfully loaded "+successfulCoordinates+" coordinates for weather gadgets.");

    }
//Vastaavasti lataa Talo55-laitteiden sijainnin (kattovalo, LED-valoa säätelevä nappi, ovikello, potentiometri).
    public void loadTalo55Gadgets(){

        talo55GadgetsFile = new File(plugin.getDataFolder(), "talo55_gadgets.yml");
        if(!talo55GadgetsFile.exists()){
            talo55GadgetsFile.getParentFile().mkdirs();
            plugin.saveResource("talo55_gadgets.yml",false);

        }

        talo55GadgetsConfig = new YamlConfiguration();

        try {
            talo55GadgetsConfig.load(talo55GadgetsFile);
        }
        catch(IOException | InvalidConfigurationException e){
            plugin.getLogger().log(Level.SEVERE, "Could not load talo55_gadgets.yml . Plugin will not function properly.");
            e.printStackTrace();
        }

        Set<String> gadgetsKeys = talo55GadgetsConfig.getKeys(false);
        Iterator<String> gadgetsIterator = gadgetsKeys.iterator();
        int successfulCoordinates = 0;

        while(gadgetsIterator.hasNext()){

            String gadgetName = gadgetsIterator.next();
            ConfigurationSection wgSection = talo55GadgetsConfig.getConfigurationSection(gadgetName);

            double x;
            double y;
            double z;

            if(wgSection.isInt("x"))
                x = wgSection.getInt("x");
            else {
                plugin.getLogger().warning("Invalid x coordinate at " + wgSection.getCurrentPath() + ". The coordinates in question could not be loaded. Please take a look at the talo55_gadgets.yml file.");
                continue;
            }

            if(wgSection.isInt("y"))
                y = wgSection.getInt("y");
            else {
                plugin.getLogger().warning("Invalid y coordinate at " + wgSection.getCurrentPath() + ". The coordinates in question could not be loaded. Please take a look at the talo55_gadgets.yml file.");
                continue;
            }

            if(wgSection.isInt("z"))
                z = wgSection.getInt("z");
            else {
                plugin.getLogger().warning("Invalid z coordinate at " + wgSection.getCurrentPath() + ". The coordinates in question could not be loaded. Please take a look at the talo55_gadgets.yml file.");
                continue;
            }

            talo55GadgetsLocations.put(gadgetName, new Location(plugin.getServer().getWorlds().get(0), x, y, z));
            successfulCoordinates++;

        }

        plugin.getLogger().log(Level.INFO, "Successfully loaded "+successfulCoordinates+" coordinates for weather gadgets.");

    }



    public String getDefaultLang() { return this.defaultLang; }

    public ArrayList<Cuboid> getWeatherArea() {
        return weatherArea;
    }

    public ArrayList<Cuboid> getAccessibleArea() {
        return accessibleArea;
    }

    public String getWeatherQueryUrl() {
        return weatherQueryUrl;
    }

    public String getWeatherTownName() {
        return weatherTownName;
    }

    public long getWeatherInterval() { return weatherInterval; }

    public int getWeatherArchiveSize() { return weatherArchiveSize; }

    public HashMap<String, Location> getWeatherGadgetsLocations() {
        return weatherGadgetsLocations;
    }

    public HashMap<String, Location> getTalo55GadgetsLocations() {
        return talo55GadgetsLocations;
    }

    public String getLocalTimeZone() {
        return localTimeZone;
    }

    public double getRainThreshold() {
        return rainThreshold;
    }

    public boolean isWinter(){
        return winter;
    }

    public double getMaxPrecipitation() {
        return maxPrecipitation;
    }

    public long getLunarMonthDuration() {
        return lunarMonthDuration;
    }

    public long getFirstFullMoon() {
        return firstFullMoon;
    }

    public String getAstronomyQueryUrl() {
        return astronomyQueryUrl;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getAstronomyRetryInterval(){
        return astronomyRetryInterval;
    }

    public String getTalo55ProviderAddress() {
        return talo55ProviderAddress;
    }

    public int getTalo55Port() {
        return talo55Port;
    }

    public long getDoorbellCooldown() {
        return doorbellCooldown;
    }
}
