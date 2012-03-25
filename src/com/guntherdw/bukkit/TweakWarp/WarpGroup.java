/*
 * Copyright (c) 2012 GuntherDW
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.guntherdw.bukkit.TweakWarp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WarpGroup {

    private String name;
    private Map<String, Warp> warps;

    public WarpGroup(String name) {
        this.name = name;
        warps = new HashMap<String, Warp>();
    }

    public Warp matchWarp(String warpname) {
        Warp rt = getWarp(warpname);
        if(rt == null) {
            int delta = Integer.MAX_VALUE;
            for(Warp w : warps.values()) {
                if(w.getName().toLowerCase().contains(warpname) && Math.abs(w.getName().length() - warpname.length()) < delta) {
                    rt = w;
                    delta = Math.abs(w.getName().length() - warpname.length());
                    if(delta == 0) break;
                }
            }
        }

        return rt;
    }

    public Warp getWarp(String warpname) {
        return warps.get(warpname);
    }

    public int getWarpCount(){
        return warps.values().size();
    }

    public boolean registerWarp(Warp warp) {
        if(!warp.getWarpgroup().equals(getName())) {
            TweakWarp.log.warning("[TweakWarp] trying to add warp to invalid group, warp: " + warp.getName() + "[" + warp.getWarpgroup() + "] to group " + getName() + ".");
            return false;
        }
        warps.put(warp.getName().toLowerCase(), warp);
        return true;
    }

    public boolean forgetWarp(Warp warp) {
        if(!warp.getWarpgroup().equals(getName())) {
            TweakWarp.log.warning("[TweakWarp] trying to remove warp from invalid group, warp: " + warp.getName() + "[" + warp.getWarpgroup() + "] from group " + getName() + ".");
            return false;
        }
        warps.remove(warp.getName().toLowerCase());
        return true;
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    public String getName() {
        return name;
    }
}
