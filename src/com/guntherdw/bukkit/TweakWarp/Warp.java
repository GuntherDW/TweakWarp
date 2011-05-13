package com.guntherdw.bukkit.TweakWarp;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.jar.Attributes;

/**
 * @author GuntherDW
 */
@Entity()
@Table(name="warps")
public class Warp {
    
    @Id
    private int id;

    @Length(max=45)
    @NotNull
    private String Name;

    @NotNull
    private double X;

    @NotNull
    private double Y;

    @NotNull
    private double Z;

    @NotNull
    private float Pitch;

    @NotNull
    private float Yaw;

    @Length(max=45)
    @NotEmpty
    private String World;

    @Length(max=45)
    private String UserGroup;

    public String getUserGroup() {
        return UserGroup;
    }

    public void setUserGroup(String userGroup) {
        UserGroup = userGroup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getWorld() {
        return World;
    }

    public void setWorld(String world) {
        World = world;
    }

    public double getX() {
        return X;
    }

    public void setX(double x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double z) {
        Z = z;
    }

    public float getPitch() {
        return Pitch;
    }

    public void setPitch(float pitch) {
        Pitch = pitch;
    }

    public float getYaw() {
        return Yaw;
    }

    public void setYaw(float yaw) {
        Yaw = yaw;
    }

    /* public Warp(double x, double y, double z, float pitch, float yaw, String name, String world) {
        X = x;
        Y = y;
        Z = z;
        Pitch = pitch;
        Yaw = yaw;
        World = world;
        Name = name;
    }

    public Warp(double x, double y, double z, float pitch, float yaw, String name, String world, String group) {
        X = x;
        Y = y;
        Z = z;
        Pitch = pitch;
        Yaw = yaw;
        World = world;
        Name = name;
        Group = group;
    }

    public Warp() {
        
    } */

    public void construct(double x, double y, double z, float pitch, float yaw, String name, String world) {
        X = x;
        Y = y;
        Z = z;
        Pitch = pitch;
        Yaw = yaw;
        World = world;
        Name = name;
    }

    public void construct(double x, double y, double z, float pitch, float yaw, String name, String world, String group) {
        X = x;
        Y = y;
        Z = z;
        Pitch = pitch;
        Yaw = yaw;
        World = world;
        Name = name;
    }
}
