package me.hihii55.alytalo55.weather;

import me.hihii55.alytalo55.AlytaloMain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AlytaloWeather {


    private AlytaloMain plugin;
    private String url;
    private String townName;
    private long interval;
    private int weatherArchiveSize;
    LinkedBlockingDeque<WeatherData> weatherArchive;

    boolean raining;
    boolean thunder;

    ScheduledExecutorService executorService;


    public AlytaloWeather(AlytaloMain plugin, String url, String townName, long interval, int weatherArchiveSize){

        this.plugin = plugin;
        this.url = url;
        this.townName = townName;
        this.interval = interval;
        this.weatherArchiveSize = weatherArchiveSize;
        this.weatherArchive = new LinkedBlockingDeque<>(weatherArchiveSize);

        this.raining = false;
        this.thunder = false;

        executorService = Executors.newSingleThreadScheduledExecutor();

    }

    public void start() {


        int failedInitial = 0;
        boolean failInitial;
        WeatherData startData = null;

        do {
            failInitial = false;
            try {
                startData = fetchLatestWeather();
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                failedInitial++;
                failInitial = true;
            }

        } while (failInitial ? failedInitial < 10 : false);

        if (failInitial) {
            plugin.getLogger().warning("Fetching weather data failed " + failedInitial + " times. Trying again in " + interval / 1000 + " seconds");
        }

        new UpdateWeather(plugin, startData).runTask(plugin);









        executorService.scheduleAtFixedRate(new Runnable() {


            @Override
            public void run() {


                int failed = 0;
                boolean fail;
                WeatherData currentData = null;

                do {
                    fail = false;
                    try {
                        currentData = fetchLatestWeather();
                    } catch (IOException | ParserConfigurationException | SAXException e) {
                        e.printStackTrace();
                        failed++;
                        fail = true;
                    }

                } while (fail ? failed < 10 : false);

                if (fail)
                    plugin.getLogger().warning("Fetching weather data failed " + failed + " times. Trying again in " + interval / 1000 + "seconds");

                new UpdateWeather(plugin, currentData).runTask(plugin);


            }
        }, interval, interval, TimeUnit.MILLISECONDS);
    }

    public void stop(){
        executorService.shutdown();
    }



    private WeatherData fetchLatestWeather() throws IOException, ParserConfigurationException, SAXException
    {


            URL fullUrl = new URL( this.url+ this.townName);

            this.plugin.getLogger().log(Level.INFO, "Attempting to fetch weather information from URL:\n"+fullUrl.toString());

            InputStream is = fullUrl.openStream();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList nl = doc.getDocumentElement().getElementsByTagName("wfs:member");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            HashMap<Long, Element> t2m = new HashMap<>();
            HashMap<Long, Element> ws_10min = new HashMap<>();
            HashMap<Long, Element> wg_10min = new HashMap<>();
            HashMap<Long, Element> wd_10min = new HashMap<>();
            HashMap<Long, Element> rh = new HashMap<>();
            HashMap<Long, Element> td = new HashMap<>();
            HashMap<Long, Element> r_1h = new HashMap<>();
            HashMap<Long, Element> ri_10min = new HashMap<>();
            HashMap<Long, Element> snow_aws = new HashMap<>();
            HashMap<Long, Element> p_sea = new HashMap<>();
            HashMap<Long, Element> vis = new HashMap<>();

            for (int i = 0; i < nl.getLength(); i++) {
                Node iNode = nl.item(i);
                if (iNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element iElement = (Element) iNode;
                    Long timeStamp = null;
                    try{
                    timeStamp = formatter.parse(iElement.getElementsByTagName("BsWfs:Time").item(0).getTextContent()).getTime();}
                    catch(ParseException e){
                        break;
                    }
                    String type = iElement.getElementsByTagName("BsWfs:ParameterName").item(0).getTextContent();

                    switch (type) {

                        case "t2m":
                            t2m.put(timeStamp, iElement);
                            break;
                        case "ws_10min":
                            ws_10min.put(timeStamp, iElement);
                            break;
                        case "wg_10min":
                            wg_10min.put(timeStamp, iElement);
                            break;
                        case "wd_10min":
                            wd_10min.put(timeStamp, iElement);
                            break;
                        case "rh":
                            rh.put(timeStamp, iElement);
                            break;
                        case "td":
                            td.put(timeStamp, iElement);
                            break;
                        case "r_1h":
                            r_1h.put(timeStamp, iElement);
                            break;
                        case "ri_10min":
                            ri_10min.put(timeStamp, iElement);
                            break;
                        case "snow_aws":
                            snow_aws.put(timeStamp, iElement);
                            break;
                        case "p_sea":
                            p_sea.put(timeStamp, iElement);
                            break;
                        case "vis":
                            vis.put(timeStamp, iElement);
                            break;
                    }

                }
            }

        long[] t2mKeys = t2m.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] ws_10minKeys = ws_10min.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] wg_10minKeys = wg_10min.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] wd_10minKeys = wd_10min.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] rhKeys = rh.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] tdKeys = td.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] r_1hKeys = r_1h.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] ri_10minKeys = ri_10min.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] snow_awsKeys = snow_aws.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] p_seaKeys = p_sea.keySet().stream().mapToLong(Long::longValue).toArray();
        long[] visKeys = vis.keySet().stream().mapToLong(Long::longValue).toArray();

        Arrays.sort(t2mKeys);
        Arrays.sort(ws_10minKeys);
        Arrays.sort(wg_10minKeys);
        Arrays.sort(wd_10minKeys);
        Arrays.sort(rhKeys);
        Arrays.sort(tdKeys);
        Arrays.sort(r_1hKeys);
        Arrays.sort(ri_10minKeys);
        Arrays.sort(snow_awsKeys);
        Arrays.sort(p_seaKeys);
        Arrays.sort(visKeys);

        String r_1hValue = "0";
        Element currentElement;
        String currentValue;
        String r_1hTime = null;

        for(int i = r_1hKeys.length - 1; i >= 0; i--){
            currentElement = r_1h.get(r_1hKeys[i]);
            currentValue = currentElement.getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent();
            if(!currentValue.equals("NaN")){
                r_1hValue = currentValue;
                r_1hTime = currentElement.getElementsByTagName("BsWfs:Time").item(0).getTextContent();
                break;
            }

        }



        return new WeatherData(
                t2m.get(t2mKeys[t2mKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                ws_10min.get(ws_10minKeys[ws_10minKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                wg_10min.get(wg_10minKeys[wg_10minKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                wd_10min.get(wd_10minKeys[wd_10minKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                rh.get(rhKeys[rhKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                td.get(tdKeys[tdKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                r_1hValue,
                ri_10min.get(ri_10minKeys[ri_10minKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                snow_aws.get(snow_awsKeys[snow_awsKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                p_sea.get(p_seaKeys[p_seaKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),
                vis.get(visKeys[visKeys.length-1]).getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent(),

                t2m.get(t2mKeys[t2mKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                ws_10min.get(ws_10minKeys[ws_10minKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                wg_10min.get(wg_10minKeys[wg_10minKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                wd_10min.get(wd_10minKeys[wd_10minKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                rh.get(rhKeys[rhKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                td.get(tdKeys[tdKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                r_1hTime,
                ri_10min.get(ri_10minKeys[ri_10minKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                snow_aws.get(snow_awsKeys[snow_awsKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                p_sea.get(p_seaKeys[p_seaKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent(),
                vis.get(visKeys[visKeys.length-1]).getElementsByTagName("BsWfs:Time").item(0).getTextContent()


                );
    }

    public int getWeatherArchiveSize(){ return this.weatherArchiveSize; }

    public boolean isRaining(){ return this.raining;}

    public boolean isThunder(){ return this.thunder;}







}