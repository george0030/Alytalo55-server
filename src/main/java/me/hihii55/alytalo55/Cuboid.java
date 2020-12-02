package me.hihii55.alytalo55;

import org.bukkit.Location;

import static java.lang.Math.abs;

// Tämä luokka esittää suorakulmaista särmiötä pelin 3d-maailmassa. Sitä hyödynnetään estettäessä pelaajien liikkumisen toivotun alueen ulkopuolelle.
public class Cuboid {



    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;

    private int xOffset; //Mitä pitää lisätä x1:seen, että päästään x2:seen
    private int yOffset;
    private int zOffset;

    private boolean xIncreases;
    private boolean yIncreases;
    private boolean zIncreases;


    public Cuboid(){}

    public Cuboid(int x1, int y1, int z1, int x2, int y2, int z2){
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.xOffset = x2-x1;
        this.yOffset = y2-y1;
        this.zOffset = z2-z1;
        this.xIncreases = this.xOffset >= 0;
        this.yIncreases = this.yOffset >= 0;
        this.zIncreases = this.zOffset >= 0;


    }

    public Cuboid(Location pos1, Location pos2){
        this.x1=pos1.getBlockX();
        this.y1=pos1.getBlockY();
        this.z1=pos1.getBlockZ();
        this.x2=pos2.getBlockX();
        this.y2=pos2.getBlockY();
        this.z2=pos2.getBlockZ();
        this.xOffset = pos2.getBlockX()-pos1.getBlockX();
        this.yOffset = pos2.getBlockY()-pos1.getBlockY();
        this.zOffset = pos2.getBlockZ()-pos1.getBlockZ();
        this.xIncreases = this.xOffset >= 0;
        this.yIncreases = this.yOffset >= 0;
        this.zIncreases = this.zOffset >= 0;



    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.xOffset = this.x2-x1;
        this.xIncreases = this.xOffset >= 0;
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.xOffset = this.y2-y1;
        this.yIncreases = this.yOffset >= 0;
        this.y1 = y1;
    }

    public int getZ1() {
        return z1;
    }

    public void setZ1(int z1) {
        this.zOffset = this.z2-z1;
        this.zIncreases = this.zOffset >= 0;
        this.z1 = z1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.xOffset=x2-this.x1;
        this.xIncreases = this.xOffset >= 0;
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.yOffset=y2-this.y1;
        this.yIncreases = this.yOffset >= 0;
        this.y2 = y2;
    }

    public int getZ2() {
        return z2;
    }

    public void setZ2(int z2) {
        this.zOffset = z2-this.z1;
        this.zIncreases = this.zOffset >= 0;
        this.z2 = z2;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getzZOffset() {
        return zOffset;
    }

    public boolean isInside(Location location){

        int xDif = location.getBlockX() - this.x1;
        int yDif = location.getBlockY() - this.y1;
        int zDif = location.getBlockZ() - this.z1;

        if(!(this.xIncreases ? this.xOffset > xDif && 0 < xDif : this.xOffset < xDif && 0 > xDif)){
            return false;}
        if(!(this.yIncreases ? this.yOffset > yDif && 0 < yDif : this.yOffset < yDif && 0 > yDif)){
            return false;}
        if(!(this.zIncreases ? this.zOffset > zDif && 0 < zDif : this.zOffset < zDif && 0 > zDif)){
            return false;}

        return true;



    }

    public int volume(){

        return abs(x2-x1)*abs(y2-y1)*abs(z2-z1);
    }




}
