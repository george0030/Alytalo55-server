package me.hihii55.alytalo55.weather;

import me.hihii55.alytalo55.AlytaloMain;
import me.hihii55.alytalo55.ResourceManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UpdateWeather extends BukkitRunnable {

    private AlytaloMain plugin;
    private WeatherData weather;

    public UpdateWeather(AlytaloMain plugin, WeatherData weather) {

        this.plugin = plugin;
        this.weather = weather;

    }


    @Override
    public void run() {
        thermometerSign();
        barometerSign();
        precipitationSign();
        rain();
        rainGauge();

    }


    private void thermometerSign() {

        ResourceManager resourceManager = this.plugin.getResourceManager();

        Location signLocation = resourceManager.getWeatherGadgetsLocations().get("thermometer-sign");
        if (signLocation == null)
            return;
        Block block = signLocation.getBlock();
        if (!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST))
            return;
        Sign sign = (Sign) block.getState();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date measurementTime = null;
        try {
            measurementTime = formatter.parse(weather.getT2mTime());
        } catch (ParseException e) {
            measurementTime = null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(measurementTime);
        calendar.setTimeZone(TimeZone.getTimeZone(resourceManager.getLocalTimeZone()));


        sign.setLine(0, resourceManager.getCaption("THERMOMETER_LINE0", resourceManager.getDefaultLang()));
        sign.setLine(1, resourceManager.getCaption("THERMOMETER_LINE1", resourceManager.getDefaultLang()).replace("{T}", weather.getT2m()));
        sign.setLine(2, resourceManager.getCaption("THERMOMETER_LINE2", resourceManager.getDefaultLang()).replace("{H}", Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)))
                .replace("{M}", (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + Integer.toString(calendar.get(Calendar.MINUTE))));
        sign.setLine(3, resourceManager.getCaption("THERMOMETER_LINE3", resourceManager.getDefaultLang()));
        sign.update();

    }

    private void rain() {
        boolean rains = Double.parseDouble(weather.getRh()) >= plugin.getResourceManager().getRainThreshold();
        plugin.getWeather().raining = rains;
        plugin.getServer().getWorlds().get(0).setStorm(rains);

    }

    private void barometerSign() {
        ResourceManager resourceManager = this.plugin.getResourceManager();

        Location signLocation = resourceManager.getWeatherGadgetsLocations().get("barometer-sign");
        if (signLocation == null)
            return;
        Block block = signLocation.getBlock();
        if (!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST))
            return;
        Sign sign = (Sign) block.getState();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date measurementTime = null;
        try {
            measurementTime = formatter.parse(weather.getP_seaTime());
        } catch (ParseException e) {
            measurementTime = null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(measurementTime);
        calendar.setTimeZone(TimeZone.getTimeZone(resourceManager.getLocalTimeZone()));


        sign.setLine(0, resourceManager.getCaption("BAROMETER_LINE0", resourceManager.getDefaultLang()));
        sign.setLine(1, resourceManager.getCaption("BAROMETER_LINE1", resourceManager.getDefaultLang()).replace("{T}", weather.getP_sea()));
        sign.setLine(2, resourceManager.getCaption("BAROMETER_LINE2", resourceManager.getDefaultLang()).replace("{H}", Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)))
                .replace("{M}", (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + Integer.toString(calendar.get(Calendar.MINUTE))));
        sign.setLine(3, resourceManager.getCaption("BAROMETER_LINE3", resourceManager.getDefaultLang()));
        sign.update();

    }

    private void rainGauge() {

        ResourceManager resourceManager = this.plugin.getResourceManager();
        Block gaugeBlock = resourceManager.getWeatherGadgetsLocations().get("rain-gauge").getBlock();


        if (resourceManager.isWinter()) {
            if (gaugeBlock.getType() == Material.WATER || gaugeBlock.getType() == Material.STATIONARY_WATER)
                gaugeBlock.setType(Material.AIR);
        } else {


            double precipitation = Double.parseDouble(weather.getR_1h());
            double maxPrecipitation = resourceManager.getMaxPrecipitation();



            if (precipitation > 0.0 && precipitation < maxPrecipitation) {
                gaugeBlock.setType(Material.STATIONARY_WATER);
                gaugeBlock.setData((byte) (8.0 - Math.ceil(precipitation / maxPrecipitation * 6)));
            } else if (precipitation >= maxPrecipitation) {
                gaugeBlock.setType(Material.STATIONARY_WATER);
                gaugeBlock.setData((byte) 1);
                gaugeBlock.getState().update();
            } else if (precipitation <= 0.0)
                gaugeBlock.setType(Material.AIR);

        }


    }





    private void precipitationSign(){

        ResourceManager resourceManager = this.plugin.getResourceManager();

        Location signLocation = resourceManager.getWeatherGadgetsLocations().get("precipitation-sign");
        if(signLocation == null)
            return;
        Block block = signLocation.getBlock();
        if(!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST))
            return;
        Sign sign = (Sign) block.getState();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date measurementTime = null;
        try {
            measurementTime = formatter.parse(weather.getR_1hTime());
        } catch (ParseException e) {
            measurementTime = null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(measurementTime);
        calendar.setTimeZone(TimeZone.getTimeZone(resourceManager.getLocalTimeZone()));
        Calendar startTime = (Calendar) calendar.clone();
        startTime.add(Calendar.HOUR_OF_DAY, -1);


        sign.setLine(0, resourceManager.getCaption("PRECIPITATION_LINE0",resourceManager.getDefaultLang()));
        sign.setLine(1, resourceManager.getCaption("PRECIPITATION_LINE1", resourceManager.getDefaultLang()).replace("{T}", weather.getR_1h()));
        sign.setLine(2, resourceManager.getCaption("PRECIPITATION_LINE2", resourceManager.getDefaultLang()).replace("{H2}", Integer.toString(startTime.get(Calendar.HOUR_OF_DAY)))
                .replace("{M2}", (startTime.get(Calendar.MINUTE)<10?"0":"")+Integer.toString(startTime.get(Calendar.MINUTE)))
                .replace("{H}", Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))).replace("{M}", (calendar.get(Calendar.MINUTE)<10?"0":"")+Integer.toString(calendar.get(Calendar.MINUTE))));
        sign.setLine(3, resourceManager.getCaption("PRECIPITATION_LINE3", resourceManager.getDefaultLang()));
        sign.update();


    }

}
