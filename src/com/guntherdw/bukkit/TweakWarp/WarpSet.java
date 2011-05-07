package com.guntherdw.bukkit.TweakWarp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WarpSet {

	private final String name;	
	private final Map<String, Warp> warps;
	public WarpSet(String name) {
		this.name = name;
		warps = new HashMap<String, Warp>();
	}
	
	
	public String getName() { return name; }
	public int getSize() { return warps.size(); }
	
	public Warp getWarp(String name) {
		return warps.get(name.trim().toLowerCase());
	}
	
	public Warp matchWarp(String name) {
		if(name == null || name.trim().equals("")) return null;
		name = name.trim().toLowerCase();
		Warp warp = warps.get(name);
		if(warp != null ) return warp;
		
		int delta = Integer.MAX_VALUE;
		for(Warp w : warps.values()) {
			if(w.getName().contains(name)) {
				if(w.getName().length() - name.length() < delta) {
					warp = w;
					delta = w.getName().length() - name.length();
				}
			}
		}
		
		return warp;
	}
	
	public Collection<Warp> getWarps() {
		return warps.values();
	}
	
	public void addWarp(Warp warp) {
		warps.put(warp.getName(), warp);
	}
	
	public void removeWarp(Warp warp) {
		if(warp.getGroup().equalsIgnoreCase(getName())) {
			warps.remove(warp.getName());
		}
	}
}
