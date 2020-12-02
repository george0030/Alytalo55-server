package me.hihii55.alytalo55.talo55;

import me.hihii55.alytalo55.AlytaloMain;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


public class Talo55 {


    private static final byte PROTOCOL_HEADER = 0b00110111;
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final int REQUEST = 1;
    private static final int NORMAL_DATA = 0;
    private static final int PROVIDER = 1;
    private static final int CONSUMER = 0;

    private AlytaloMain plugin;
    private String providerAddress;
    private InetAddress providerIpAddress;
    private int port;
    private Thread receiverThread;

    private Talo55Receiver receiver;
    private Talo55Sender sender;


    private DatagramSocket outboundSocket;
    private DatagramSocket inboundSocket;

    private DatagramPacket outboundPacket;
    private DatagramPacket inboundPacket;

    private byte[] outbound;
    private volatile byte[] inboundCopy;
    private byte[] inbound;
    private volatile int firstEditField;
    private volatile int dataSize;
    private volatile int editableFields;
    private volatile int minorVersion;

    private volatile boolean protocolOk;
    private volatile boolean keepRunning;
    private volatile boolean shutDown;

    private volatile int senderTaskId;



    private volatile List<Byte> editList;

    private long lastDoorbell;


    public Talo55(AlytaloMain plugin, String providerAddress, int port){

        this.plugin = plugin;
        this.providerAddress = providerAddress;
        this.port = port;
        this.shutDown = true;


    }

    public boolean start(){


        shutDown = false;
        keepRunning = true;
        minorVersion = MINOR_VERSION;
        protocolOk = false;
        outboundSocket = null;
        inboundSocket = null;
        outboundPacket = null;
        inboundPacket = null;
        outbound = null;
        inbound = null;
        editList = null;
        firstEditField = 0;
        dataSize = 0;
        lastDoorbell = 0;


        try{
            providerIpAddress = InetAddress.getByName(providerAddress);
        }
        catch (UnknownHostException e){
            plugin.getLogger().log(Level.WARNING, "[Talo55-Sender] Unknown host name "+providerAddress+" . Talo55 service shutting down.");
            shutDown = true;
            return false;
        }
        receiver = new Talo55Receiver();
        if(!receiver.initialize()){
            shutDown = true;
            return false;
        }
        sender = new Talo55Sender(0, 1);
        if(!sender.initialize()){
            shutDown = true;
            return false;
        }
        receiverThread = new Thread(receiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
        senderTaskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, sender, 0, 0);

        return true;

    }

    public void stop(){
        plugin.getLogger().log(Level.INFO, "Shutting down Talo55 service");
        keepRunning = false;
        protocolOk = false;
        outboundSocket.close();
        inboundSocket.close();
        plugin.getServer().getScheduler().cancelTask(senderTaskId);
        try {
            receiverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shutDown = true;

    }


    public boolean isShutDown(){
        return this.shutDown;
    }






    private class Talo55Sender implements Runnable {

        byte[] inboundCopyCopy;
        int currentDataSize;
        int currentFirstEditField;
        List<Byte> currentEditList;
        int consecutiveFails;
        int printExceptionEvery;
        boolean packetSetUp;

        DatagramPacket requestPacket;

        int sendRepeats;
        long repeatPeriod;

        public Talo55Sender(int sendRepeats, long repeatPeriod) {
            this.sendRepeats = sendRepeats;
            this.repeatPeriod = repeatPeriod;
            packetSetUp = false;
        }


        public boolean initialize() {
            try {
                outboundSocket = new DatagramSocket();

            } catch (SocketException e) {
                plugin.getLogger().log(Level.WARNING, "[Talo55-Sender] Could not open listening socket. Talo55 service shutting down.");
                e.printStackTrace();
                return false;

            }
            requestPacket = new DatagramPacket(new byte[]{PROTOCOL_HEADER, MAJOR_VERSION < 4 ?
                    (byte) ((MAJOR_VERSION << 5) | (minorVersion << 2) | (REQUEST << 1) | CONSUMER) : (byte) -(((MAJOR_VERSION << 5) | (minorVersion << 2) | (REQUEST << 1) | CONSUMER) & 0b011111111)}, 2, providerIpAddress, port);

            return true;
        }

        @Override
        public void run() {


            if (!protocolOk) {
                packetSetUp = false;
                try {
                    outboundSocket.send(requestPacket);
                    consecutiveFails = 0;
                } catch (IOException e) {
                    consecutiveFails++;
                    if (consecutiveFails % printExceptionEvery == 0) {
                        plugin.getLogger().log(Level.WARNING, "[Talo55-Sender] Failed to send protocol request packet");
                        e.printStackTrace();
                    }
                }


            } else {

                if(!packetSetUp){

                    outbound = new byte[2 + dataSize];
                    outbound[0] = PROTOCOL_HEADER;
                    outbound[1] = MAJOR_VERSION < 4 ?
                            (byte) ((MAJOR_VERSION << 5) | (minorVersion << 2) | (NORMAL_DATA << 1) | CONSUMER) : (byte) -(((MAJOR_VERSION << 5) | (minorVersion << 2) | (NORMAL_DATA << 1) | CONSUMER) & 0b011111111);
                    outboundPacket = new DatagramPacket(outbound, outbound.length, providerIpAddress, port);
                    packetSetUp = true;


                }

                currentDataSize = dataSize;
                currentFirstEditField = firstEditField;
                inboundCopyCopy = inboundCopy;


                for (int i = 0; i < currentFirstEditField; i++) {
                    outbound[i + 2] = inboundCopyCopy[i + 2];
                }
                currentEditList = editList;
                synchronized (currentEditList) {
                    for (int i = currentFirstEditField; i < currentDataSize; i++) {
                        outbound[i + 2] = currentEditList.get(i-currentFirstEditField);
                    }


                }

                try {
                    outboundSocket.send(outboundPacket);
                    consecutiveFails = 0;
                } catch (IOException e) {
                    consecutiveFails++;

                    if (consecutiveFails % printExceptionEvery == 0) {
                        plugin.getLogger().log(Level.WARNING, "[Talo55-Sender] Failed to send protocol request packet");
                        e.printStackTrace();
                    }
                }

            }



        }

    }
    private class Talo55Receiver implements Runnable{

        int consecutiveFails;
        int printExceptionEvery = 50;
        int currentDataSize;
        int taskId = 0;

        byte[] inboundTemp;
        byte[] lastCycle;


        public boolean initialize(){

            try {
                inboundSocket = new DatagramSocket(port);
            } catch (SocketException e) {
                plugin.getLogger().log(Level.WARNING, "[Talo55-Receiver] Could not open UDP port number "+port+". Talo55 service shutting down.");
                e.printStackTrace();
                return false;
            }
            inbound = new byte[512];
            inboundPacket = new DatagramPacket(inbound, 512);
            return true;

        }


        @Override
        public void run() {



            while(keepRunning){


                try {
                    inboundSocket.receive(inboundPacket);
                    consecutiveFails = 0;
                } catch (IOException e) {
                    consecutiveFails++;
                    if(consecutiveFails % printExceptionEvery == 0){
                        plugin.getLogger().log(Level.WARNING, "[Talo55-Receiver] Error while listening for incoming packets at port "+port+".");
                        e.printStackTrace();}
                    continue;}
                inbound = inboundPacket.getData();

                if(!inboundPacket.getAddress().equals(providerIpAddress))
                    continue;
                if(inbound[0] != PROTOCOL_HEADER)
                    continue;
                if(((inbound [1] & 0b11100000) >> 5) != MAJOR_VERSION)
                    continue;
                if((inbound[1] & 0b00000001) == CONSUMER)
                    continue;
                if(((inbound[1] & 0b00000010) >> 1) == REQUEST){



                    firstEditField = ((inbound[2] & 0xff) << 8) | (inbound[3] & 0xff);
                    dataSize = ((inbound[4] & 0xff) << 8) | (inbound[5] & 0xff);
                    editableFields = dataSize - firstEditField;

                    minorVersion = (inbound[1] & 0b00011100) >> 2;
                    ArrayList<Byte> tempEditList = new ArrayList<Byte>(editableFields);
                    for(int i = 0; i < editableFields; i++){
                        tempEditList.add((byte)0);
                    }
                    editList = Collections.synchronizedList(tempEditList);
                    inboundCopy = new byte[dataSize+2];
                    protocolOk = true;


                }
                else if (protocolOk){



                    if(((inbound[1] & 0b00011100) >> 2) != minorVersion){
                        protocolOk = false;
                    }
                    else{
                        currentDataSize = dataSize;
                        inboundTemp = new byte[currentDataSize+2];
                        if(lastCycle != null){
                            for(int i = 0; i < (currentDataSize + 2); i++){
                                //if (i > 1 && lastCycle[i] != inbound[i]){
                                 //   new Talo55Update(i-2).runTask(plugin);
                                //}
                                inboundTemp[i] = inbound[i];

                            }
                        }
                        else{
                            for(int i = 0; i < (currentDataSize + 2); i++){
                                //if (i > 1){
                                //    new Talo55Update(i-2).runTask(plugin);
                               // }
                                inboundTemp[i] = inbound[i];

                            }

                        }
                        lastCycle = inboundTemp;
                        inboundCopy = inboundTemp;
                        if(taskId == 0 || (!plugin.getServer().getScheduler().isQueued(taskId))){

                        taskId = new Talo55Update().runTask(plugin).getTaskId();}

                    }


                }



            }

        }
    }


    private class Talo55Update extends BukkitRunnable {

        int inboundSize;


        @Override
        public void run() {
            byte[] inboundUpdateCopy = inboundCopy;
            inboundSize = inboundUpdateCopy.length;
            if(inboundSize >= 4) {
                if (inboundUpdateCopy[2] != -1 || inboundUpdateCopy[3] != -1) {
                    Location signLocation = plugin.getResourceManager().getTalo55GadgetsLocations().get("potentiometer-sign");
                    if (signLocation != null) {
                        Block signBlock = signLocation.getBlock();
                        if ((signBlock.getType() == Material.WALL_SIGN || signBlock.getType() == Material.SIGN_POST)) {
                            byte first = inboundUpdateCopy[2];
                            byte second = inboundUpdateCopy[3];
                            int combined = (first << 5) | (second & 31);
                            Sign state = (Sign) signBlock.getState();
                            state.setLine(0, plugin.getResourceManager().getCaption("POTENTIOMETER_LINE0", plugin.getResourceManager().getDefaultLang()));
                            state.setLine(1, plugin.getResourceManager().getCaption("POTENTIOMETER_LINE1",plugin.getResourceManager().getDefaultLang()).replace("{T}", Integer.toString(combined)));
                            state.setLine(2, plugin.getResourceManager().getCaption("POTENTIOMETER_LINE2", plugin.getResourceManager().getDefaultLang()));
                            state.setLine(3, plugin.getResourceManager().getCaption("POTENTIOMETER_LINE3", plugin.getResourceManager().getDefaultLang()));
                            state.update();
                        }
                    }
                }
            }

            //if(inboundSize <= 6){
                //if(inboundUpdateCopy[4] != -1 && inboundUpdateCopy[5] != -1){
                    //TODO: lisää lämpötila-anturi
                //}
            //}

            if(inboundSize >= 7){
                Location lampLocation = plugin.getResourceManager().getTalo55GadgetsLocations().get("ceiling-light");
                if(lampLocation != null){
                    Block lampBlock = lampLocation.getBlock();
                    Material currentType = lampBlock.getType();
                    if(inboundUpdateCopy[6] == -1) {
                        if (currentType != Material.WOOL)
                            lampBlock.setType(Material.WOOL);
                    }
                    else if(inboundUpdateCopy[6] == 0){
                        if(currentType != Material.STONE)
                            lampBlock.setType(Material.STONE);

                    }
                    else if(inboundUpdateCopy[6] == 1){
                        if(currentType != Material.GLOWSTONE){
                            lampBlock.setType(Material.GLOWSTONE);
                        }
                    }
                }
            }
            if(inboundSize >= 8){
                Location doorbellLocation = plugin.getResourceManager().getTalo55GadgetsLocations().get("doorbell");
                if(doorbellLocation != null){
                    Block doorbellBlock = doorbellLocation.getBlock();
                    if(doorbellBlock.getType() == Material.WOOL){
                        if(inboundUpdateCopy[7] == -1){
                            doorbellBlock.setData(DyeColor.GRAY.getWoolData());
                        }
                        else if(inboundUpdateCopy[7] == 0){
                            doorbellBlock.setData(DyeColor.WHITE.getWoolData());
                        }
                        else if(inboundUpdateCopy[7] == 1){
                            long now = System.currentTimeMillis();
                            if(now - lastDoorbell > plugin.getResourceManager().getDoorbellCooldown()){
                                doorbellLocation.getWorld().playSound(doorbellLocation, Sound.BLOCK_NOTE_CHIME, 10, 1);
                                lastDoorbell = now;
                            }
                            doorbellBlock.setData(DyeColor.RED.getWoolData());
                        }

                    }
                }
            }
            if(inboundSize <= 9){
                Location tableLampLocation = plugin.getResourceManager().getTalo55GadgetsLocations().get("table-lamp");
                if(tableLampLocation != null){
                    Block tableLampBlock = tableLampLocation.getBlock();
                    if(tableLampBlock.getType() == Material.TORCH || tableLampBlock.getType() == Material.AIR){
                        if(inboundUpdateCopy[8] == 0 || inboundUpdateCopy[8] == -1){
                            tableLampBlock.setType(Material.AIR);
                        }
                        else if(inboundUpdateCopy[8] == 1){
                            tableLampBlock.setType(Material.TORCH);
                        }
                    }
                }

            }

        }

    }


    public boolean setOutboundField(int field, byte value){

        if(!protocolOk)
            return false;
        if(field < firstEditField)
            return false;

        synchronized(editList){
            editList.set(field - firstEditField, value);
        }
        return true;
    }




}
