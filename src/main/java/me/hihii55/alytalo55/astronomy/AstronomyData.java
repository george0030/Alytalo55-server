package me.hihii55.alytalo55.astronomy;

public class AstronomyData {

    private long sunrise;
    private long sunset;
    private String date;

    public AstronomyData(long sunrise, long sunset, String date){
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.date = date;
    }

    public long getSunrise(){
        return this.sunrise;
    }

    public long getSunset(){
        return this.sunset;
    }
    public String getDate(){
        return this.date;
    }


}
