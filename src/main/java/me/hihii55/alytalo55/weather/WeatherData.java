package me.hihii55.alytalo55.weather;

public class WeatherData {

    private String t2m;
    private String ws_10min;
    private String wg_10min;
    private String wd_10min;
    private String rh;
    private String td;
    private String r_1h;
    private String ri_10min;
    private String snow_aws;
    private String p_sea;
    private String vis;

    private String t2mTime;
    private String ws_10minTime;
    private String wg_10minTime;
    private String wd_10minTime;
    private String rhTime;
    private String tdTime;
    private String r_1hTime;
    private String ri_10minTime;
    private String snow_awsTime;
    private String p_seaTime;
    private String visTime;


    public WeatherData(String t2m, String ws_10min, String wg_10min, String wd_10min, String rh, String td, String r_1h, String ri_10min, String snow_aws, String p_sea, String vis,
                       String t2mTime, String ws_10minTime, String wg_10minTime, String wd_10minTime, String rhTime, String tdTime, String r_1hTime, String ri_10minTime, String snow_awsTime, String p_seaTime, String visTime) {
        this.t2m = t2m;
        this.ws_10min = ws_10min;
        this.wg_10min = wg_10min;
        this.wd_10min = wd_10min;
        this.rh = rh;
        this.td = td;
        this.r_1h = r_1h;
        this.ri_10min = ri_10min;
        this.snow_aws = snow_aws;
        this.p_sea = p_sea;
        this.vis = vis;

        this.t2mTime = t2mTime;
        this.ws_10minTime = ws_10minTime;
        this.wg_10minTime = wg_10minTime;
        this.wd_10minTime = wd_10minTime;
        this.rhTime = rhTime;
        this.tdTime = tdTime;
        this.r_1hTime = r_1hTime;
        this.ri_10minTime = ri_10minTime;
        this.snow_awsTime = snow_awsTime;
        this.p_seaTime = p_seaTime;
        this.visTime = visTime;

    }


    public String getT2m() {
        return t2m;
    }

    public String getWs_10min() {
        return ws_10min;
    }

    public String getWg_10min() {
        return wg_10min;
    }

    public String getWd_10min() {
        return wd_10min;
    }

    public String getRh() {
        return rh;
    }

    public String getTd() {
        return td;
    }

    public String getR_1h() {
        return r_1h;
    }

    public String getRi_10min() {
        return ri_10min;
    }

    public String getSnow_aws() {
        return snow_aws;
    }

    public String getP_sea() {
        return p_sea;
    }

    public String getVis() {
        return vis;
    }

    public String getT2mTime() {
        return t2mTime;
    }

    public String getWs_10minTime() {
        return ws_10minTime;
    }

    public String getWg_10minTime() {
        return wg_10minTime;
    }

    public String getWd_10minTime() {
        return wd_10minTime;
    }

    public String getRhTime() {
        return rhTime;
    }

    public String getTdTime() {
        return tdTime;
    }

    public String getR_1hTime() {
        return r_1hTime;
    }

    public String getRi_10minTime() {
        return ri_10minTime;
    }

    public String getSnow_awsTime() {
        return snow_awsTime;
    }

    public String getP_seaTime() {
        return p_seaTime;
    }

    public String getVisTime() {
        return visTime;
    }
}
