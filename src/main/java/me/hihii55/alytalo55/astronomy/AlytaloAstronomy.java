package me.hihii55.alytalo55.astronomy;

import me.hihii55.alytalo55.AlytaloMain;
import org.bukkit.World;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AlytaloAstronomy implements Runnable{

    private AlytaloMain plugin;
    private String astronomyQueryUrl;
    private String latitude;
    private String longitude;
    private AstronomyData today;
    private AstronomyData tomorrow;
    private AstronomyData yesterday;
    private long tomorrowMidnight;
    private long todayNoon;
    private boolean noonAdjusted;
    private World world;
    private long nightLength;
    private long dayLength;
    private long currentMoonPhaseOffset;

    private int taskId;

    private static final long SUNRISE = 22917;
    private static final long SUNSET = 13050;
    private static final long DAY_LENGTH = 14133;
    private static final long NIGHT_LENGTH = 9867;

    private ScheduledExecutorService service;

    public AlytaloAstronomy(AlytaloMain plugin, String astronomyQueryUrl, String latitude, String longitude){

        this.plugin = plugin;
        this.astronomyQueryUrl = astronomyQueryUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.noonAdjusted = false;

    }

    public void start(){

        service = Executors.newSingleThreadScheduledExecutor();
        world = plugin.getServer().getWorlds().get(0);
        world.setGameRuleValue("doDaylightCycle", "false");
        plugin.getLogger().log(Level.INFO, "Normal minecraft day-night-cycle has been disabled for the sake of AlytaloAstronomy. Enabling it might cause weird interactions.");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar todayCalendar = Calendar.getInstance();
        long todayTime = todayCalendar.getTimeInMillis();
        Calendar tomorrowCalendar = (Calendar) todayCalendar.clone();
        Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
        tomorrowCalendar.add(Calendar.DATE, 1);
        yesterdayCalendar.add(Calendar.DATE, -1);
        int failedInitial = 0;
        boolean failInitial;
        do {
            failInitial = false;
            try {
                today = fetchAstronomyData(formatter.format(todayCalendar.getTime()));
                tomorrow = fetchAstronomyData(formatter.format(tomorrowCalendar.getTime()));
                yesterday = fetchAstronomyData(formatter.format(yesterdayCalendar.getTime()));
            } catch (IOException | ParseException| java.text.ParseException e) {
                e.printStackTrace();
                failedInitial++;
                failInitial = true;
            }

        } while (failInitial ? failedInitial < 10 : false);

        if (failInitial) {
            plugin.getLogger().warning("Fetching astronomy data failed " + failedInitial + " times. Shutting down service.");
            return;
        }

        tomorrowCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tomorrowCalendar.set(Calendar.MINUTE, 0);
        tomorrowCalendar.set(Calendar.SECOND, 0);
        tomorrowCalendar.set(Calendar.MILLISECOND, 0);
        tomorrowMidnight = tomorrowCalendar.getTimeInMillis();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 12);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        todayNoon = todayCalendar.getTimeInMillis();

        if(todayTime < todayNoon)
            currentMoonPhaseOffset = moonPhaseOffset(tomorrowMidnight - 86400000);

        if(todayTime < today.getSunrise()){
            nightLength = today.getSunrise() - yesterday.getSunset();
            dayLength = today.getSunset() - today.getSunrise();
        }
        else if(todayTime >= today.getSunset()){
            nightLength = tomorrow.getSunrise() - today.getSunset();
            dayLength = tomorrow.getSunset() - tomorrow.getSunrise();
        }
        else{
            nightLength = tomorrow.getSunrise() - today.getSunset();
            dayLength = today.getSunset() -today.getSunrise();
        }


        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 200);




    }

    public void stop(){
        service.shutdown();
        plugin.getServer().getScheduler().cancelTask(taskId);
        world.setGameRuleValue("doDaylightCycle", "true");
        plugin.getServer().getLogger().log(Level.INFO, "Disabling astronomy service. Regular minecraft day-night cycle is now active.");
    }

    private AstronomyData fetchAstronomyData(String date) throws IOException, ParseException, java.text.ParseException {

        URL fullUrl = new URL(astronomyQueryUrl+"lat="+latitude+"&lng="+longitude+"&date="+date+"&formatted=0");
        this.plugin.getLogger().log(Level.INFO, "Attempting to fetch astronomy information from URL:\n"+fullUrl.toString());
        InputStream is = fullUrl.openStream();
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = (JSONObject)jsonParser.parse(new InputStreamReader(is, "UTF-8"));
        JSONObject subObject = (JSONObject)obj.get("results");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date sunrise = formatter.parse((String)subObject.get("sunrise"));
        Date sunset = formatter.parse((String)subObject.get("sunset"));

        return new AstronomyData(sunrise.getTime(), sunset.getTime(), date);


    }

    private long moonPhaseOffset(long time) {

        int phasesToSubstract = (int) ((time - plugin.getResourceManager().getFirstFullMoon()) / plugin.getResourceManager().getLunarMonthDuration());
        double moonPhaseRatio = ((double)((time - plugin.getResourceManager().getFirstFullMoon()) - (phasesToSubstract * plugin.getResourceManager().getLunarMonthDuration()))) /
                ((double) plugin.getResourceManager().getLunarMonthDuration());

        if(moonPhaseRatio < 0.3)
            return 0;
        else if(moonPhaseRatio >= 0.3 && moonPhaseRatio < 0.22)
            return 24000;
        else if(moonPhaseRatio >= 0.22 && moonPhaseRatio < 0.28)
            return 48000;
        else if(moonPhaseRatio >= 0.28 && moonPhaseRatio < 0.47)
            return 72000;
        else if(moonPhaseRatio >= 0.47 && moonPhaseRatio < 0.53)
            return 96000;
        else if(moonPhaseRatio >= 0.53 && moonPhaseRatio < 0.72)
            return 120000;
        else if(moonPhaseRatio >= 0.72 && moonPhaseRatio < 0.78)
            return 144000;
        else if(moonPhaseRatio >= 0.78 && moonPhaseRatio < 0.97)
            return 168000;
        else if(moonPhaseRatio >= 0.97)
            return 0;
        else
            return 0;


    }


    @Override
    public void run(){

        long now = System.currentTimeMillis();

        if(now >= tomorrowMidnight){
            tomorrowMidnight =+ 86400000;
            todayNoon = todayNoon =+ 86400000;
            noonAdjusted = false;
            yesterday = today;
            today = tomorrow;
            tomorrow = null;
            dayLength = today.getSunset() - today.getSunrise();
            service.scheduleAtFixedRate(new Runnable(){

                @Override
                public void run(){

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                    int failedInitial = 0;
                    boolean failInitial;
                    do {
                        failInitial = false;
                        try {
                            tomorrow = fetchAstronomyData(formatter.format(new Date(now)));
                        } catch (IOException | ParseException| java.text.ParseException e) {
                            e.printStackTrace();
                            failedInitial++;
                            failInitial = true;
                        }

                    } while (failInitial ? failedInitial < 10 : false);

                    if (failInitial) {
                        plugin.getLogger().warning("Fetching astronomy data for tomorrow failed " + failedInitial + " times. Trying again in "+plugin.getResourceManager().getAstronomyRetryInterval()+" minutes.");
                        return;
                    }
                    else
                        service.shutdown();


                }

            }, 0, plugin.getResourceManager().getAstronomyRetryInterval(), TimeUnit.MINUTES );
        }

        else if((!noonAdjusted) && now >= todayNoon){
            if(tomorrow != null)
                nightLength = tomorrow.getSunrise() - today.getSunset();
            else{
                service.shutdown();
                plugin.getLogger().log(Level.SEVERE, "Length of upcoming night cannot be determined after multiple retries. Astronomy service shutting down.");
                plugin.getServer().getScheduler().cancelTask(taskId);
                return;
            }

            currentMoonPhaseOffset = moonPhaseOffset(tomorrowMidnight);
            noonAdjusted = true;
        }




        if(now >= today.getSunset())
            world.setFullTime((((now - today.getSunset())*NIGHT_LENGTH)/nightLength) + SUNSET + currentMoonPhaseOffset);
        else if(now < today.getSunrise())
            world.setFullTime((((now - yesterday.getSunset())*NIGHT_LENGTH)/nightLength) + SUNSET + currentMoonPhaseOffset);
        else{
            long base = (((now - today.getSunrise())*DAY_LENGTH)/dayLength) + SUNRISE + currentMoonPhaseOffset;
            world.setFullTime(base >= 24000?base-24000:base);
        }









    }


}
