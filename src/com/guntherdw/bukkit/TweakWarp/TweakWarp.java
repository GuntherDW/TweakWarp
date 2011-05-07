package com.guntherdw.bukkit.TweakWarp;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * @author GuntherDW
 */
public class TweakWarp extends JavaPlugin {

    private final static Logger log = Logger.getLogger("Minecraft");
    private String dbhost, db, user, pass;
    // private static Connection conn;
    
    public static Permissions perm = null;
    public Map<String, WarpSet> warpGroups;

    public List<String> saveWarps;
    public TweakcraftUtils tweakcraftutils;
    
    public static final String DEFAULT_GROUP = "default";

    public WarpSet getSet(String group) {
    	WarpSet set = warpGroups.get(group.trim().toLowerCase());
    	if(set == null) {
    		set = new WarpSet(group);
    		warpGroups.put(set.getName(),set);
    	}
    	return set;
    }
    
    public WarpSet matchSet(String group) {
    	group = group.trim().toLowerCase();
    	WarpSet set = warpGroups.get(group);
    	if(set == null) {
    		int delta = Integer.MAX_VALUE;
    		for(WarpSet s : warpGroups.values()) {
    			if(s.getName().contains(group) && s.getName().length() - group.length() < delta) {
    				set = s;
    				delta = s.getName().length() - group.length();
    			}
    		}
    	}
    	if(set == null)
    		return getSet(DEFAULT_GROUP);
    	else
    		return set;
    }
    
    public Warp matchWarp(String group, String warpname)
    {
        WarpSet set = matchSet(group);
        if(set != null) return set.matchWarp(warpname);
        else return null;
    }

    private void loadDriver() {
        final String driverName = "com.mysql.jdbc.Driver";
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //return null;
        }
    }

    private Connection getConnection()
    {
        try {
            String url = "jdbc:mysql://"+dbhost+":3306/" + db;
            return DriverManager.getConnection(url + "?autoReconnect=true&user=" + user + "&password=" + pass);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    public String getWarpList(String group)
    {
        String msg = "";
        // String[] lijstje = (String[]) warps.keySet().toArray();
        List<String> lijst = new ArrayList<String>();
        WarpSet set = getSet(group);
        if(set != null){
        	for(Warp warp : set.getWarps())
        		lijst.add(warp.getName());        	
        }
        
        Collections.sort(lijst, String.CASE_INSENSITIVE_ORDER);
        for(String m : lijst)
        {
            msg += m+", ";
        }
        if(msg.length()!=0) msg = msg.substring(0, msg.length()-2);
        return msg;
    }

    public boolean removeWarp(Warp warp)
    {
    	Connection conn = null;
    	PreparedStatement st = null;
        try{
            conn = getConnection();
            st = conn.prepareStatement("DELETE FROM warps WHERE name = ? AND group = ?");
            st.setString(1, warp.getName());
            st.setString(2, warp.getGroup());
            st.executeUpdate();
        } catch(SQLException e){
            log.warning("[TweakWarp] removeWarp error occurred : " + e.getStackTrace());
            return false;
        } finally {
        	if(conn != null)conn.close();
        	if(st != null)st.close();
        }
        getSet(warp.getGroup()).removeWarp(warp);
        
        return true;
    }

    public boolean addWarp(Warp warp)
    {
    	Connection conn = null;
    	PreparedStatement st = null;    	
        try{
            conn = getConnection();
            st = conn.prepareStatement("REPLACE INTO warps (name,x,y,z,rotX,rotY,world,group) VALUES (?,?,?,?,?,?,?,?)");
            st.setString(1, warp.getName());
            st.setDouble(2, warp.getX());
            st.setDouble(3, warp.getY());
            st.setDouble(4, warp.getZ());
            st.setFloat(5, warp.getPitch());
            st.setFloat(6, warp.getYaw());
            st.setString(7, warp.getWorld());
            st.setString(8, warp.getGroup());
            st.executeUpdate();
            return true;
        } catch(SQLException e){
            log.warning("[TweakWarp] addWarp error occurred : " + e.getStackTrace());
            return false;
        } finally {
        	if(conn != null)conn.close();
        	if(st != null)st.close();
        }
        
        return true;
    }

    public void reloadWarpTable(boolean sql) {
    	Connection conn = null;
    	PreparedStatement st = null;
    	ResultSet rs = null;
        
    	try {
            if(sql){
                setupConnection();
            }
            conn = getConnection();
            int count = 0;
            this.warpGroups = new HashMap<String, WarpSet>();
            st = conn.prepareStatement("SELECT name, x,y,z,rotX,rotY,world,group FROM warps");
            rs = st.executeQuery();

            while (rs.next()) {
            	Warp warp = new Warp(rs.getDouble(2), rs.getDouble(3),rs.getDouble(4), rs.getFloat(5), rs.getFloat(6),rs.getString(1), rs.getString(7), rs.getString(8));
            	getSet(warp.getGroup()).addWarp(warp);
                count++;
            }
            log.info("[TweakWarp] Loaded " + count + " warps!");

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
        	if(conn != null)conn.close();
        	if(st != null)st.close();
        	if(rs != null)rs.close();
        }
    }

    public void onDisable() {
        log.info("[TweakWarp] Shutting down!");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setupConnection() {
        this.dbhost = getConfiguration().getString("dbhost");
        this.db =  getConfiguration().getString("database");
        this.user = getConfiguration().getString("username");
        this.pass = getConfiguration().getString("password");
    }

    public void initConfig()
    {
        try{
            getConfiguration().setProperty("database", "databasename");
            getConfiguration().setProperty("username", "database-username");
            getConfiguration().setProperty("password", "database-password");
        } catch (Throwable e)
        {
            log.severe("[TweakWarp] There was an exception while we were saving the config, be sure to doublecheck!");
        }
    }

    public void onEnable() {
        if(getConfiguration() == null)
        {
            log.severe("[TweakWarp] You have to configure me now, reboot the server after you're done!");
            getDataFolder().mkdirs();
            initConfig();
            this.setEnabled(false);
        }
        loadDriver();
        setupConnection();
        setupPermissions();
        setupTCUtils();
        saveWarps = new ArrayList<String>();
        reloadWarpTable(false);
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[TweakWarp] TweakWarp v"+pdfFile.getVersion()+" enabled!");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setupTCUtils() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("TweakcraftUtils");

        if (tweakcraftutils == null) {
            if (plugin != null) {
                tweakcraftutils = (TweakcraftUtils) plugin;
            }
        }
    }

    public void setupPermissions() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");

        if (perm == null) {
            if (plugin != null) {
                perm = (Permissions) plugin;
            }
        }
    }

    public boolean check(Player player, String permNode) {
        if (perm == null) {
            return true;
        } else {
            return player.isOp() ||
                   perm.Security.permission(player, permNode);
        }
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {

        if(command.getName().equalsIgnoreCase("listwarps")) {
            commandSender.sendMessage(ChatColor.GREEN + "Current warps:");
            String group = DEFAULT_GROUP;
            if(strings.length > 0) {
            	if(strings[0] != null && !strings[0].trim().equals(""))
            		group = strings[0];
            }
            String msg = this.getWarpList(group);
            commandSender.sendMessage(msg);
            return true;
        } else if(command.getName().equalsIgnoreCase("removewarp")) {
            if(commandSender instanceof Player)
            {
                Player player = (Player) commandSender;
                if(!check(player, "tweakwarp.removewarp")){
                	player.sendMessage(ChatColor.RED + "You do not have the correct permissions!");
                	return true;
                }
            	if(strings.length < 1) {
            		player.sendMessage(ChatColor.YELLOW + "Usage: /removewarp [warp name] <group name>");
            		return true;
            	}
        		String group = DEFAULT_GROUP;
        		if(strings.length > 1) {
        			if(strings[1] != null && !strings[1].trim().equals(""))
        				group = strings[1];
        		}
        		Warp warp = matchWarp(group,strings[0]);
        		if(warp == null) {
        			player.sendMessage(ChatColor.RED + "No warp '" + strings[0] + "' " + (group.equals(DEFAULT_GROUP) ? "" : "in group '" + group + "' ") + "found!");
        		} else {
        			if(removeWarp(warp)) {
        				player.sendMessage(ChatColor.GREEN + "Succesfully removed warp '" + warp.getName() + "' " + (warp.getGroup().equals(DEFAULT_GROUP) ? "" : "in group '" + warp.getGroup() + "'") + "!");
        			} else {
        				player.sendMessage(ChatColor.RED + "Error removing warp, contact an server administrator.");
        			}
        		}
                return true;
            } else {
                commandSender.sendMessage("You need to be a player to remove a warp!");
            }
            } else if(command.getName().equalsIgnoreCase("setwarp")) {
                if(commandSender instanceof Player)
                {
                    Player player = (Player) commandSender;
                    if(!check(player, "tweakwarp.setwarp")){
                    	player.sendMessage("You do not have the correct permissions");
                    	return true;
                    }
                	if(strings.length < 1) {
                		player.sendMessage(ChatColor.YELLOW + "Usage: /addwarp [warp name] <group name>");
                		return true;
                	} 
                	String group = DEFAULT_GROUP;
            		if(strings.length > 1) {
            			if(strings[1] != null && !strings[1].trim().equals(""))
            				group = strings[1];
            		}
                    String warpname = strings[0];
                    Warp tempwarp = new Warp(player.getLocation().getX(),
                                             player.getLocation().getY(),
                                             player.getLocation().getZ(),
                                             player.getLocation().getYaw(),
                                             player.getLocation().getPitch(),
                                             warpname,
                                             player.getLocation().getWorld().getName(),
                                             group);
                    if(addWarp(tempwarp)){
                        log.info("[TweakWarp] Warp '"+warpname+"' created by "+player.getName()+"!");
                        player.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' created!");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
                    }

                } else {
                    commandSender.sendMessage("You need to be a player to set a warp!");
                }
            return true;
        } else if(command.getName().equalsIgnoreCase("warp")) {
            if(commandSender instanceof Player)
            {
                Player player = (Player) commandSender;
                if(!check(player, "tweakwarp.warp")){
                	player.sendMessage("You don't have permission to warp!");
                	return true;
                }
                if(strings.length < 1){ 
                	player.sendMessage(ChatColor.AQUA + command.getUsage());
                	return true;
                }
                String warpname = strings[0];
                
                String group = DEFAULT_GROUP;
        		if(strings.length > 1) {
        			if(strings[1] != null && !strings[1].trim().equals(""))
        				group = strings[1];
        		}
        		
        		Warp warp = matchWarp(group, warpname);
                if(warp != null)
                {
                    player.sendMessage(ChatColor.AQUA + "Found warp with name "+w.getName());
                    Location loc = new Location(this.getServer().getWorld(warp.getWorld()),
                    		warp.getX(), warp.getY() + 1, warp.getZ(), warp.getPitch(), warp.getYaw());
                    if(tweakcraftutils!=null) {
                        if(!saveWarps.contains(player.getName())) {
                            tweakcraftutils.getTelehistory().addHistory(player.getName(), player.getLocation());
                        }
                    }
                    player.teleport(loc);
                    player.sendMessage(ChatColor.AQUA + "WHOOOSH!");
                    log.info("[TweakWarp] "+player.getName()+" warped to "+warp.getName()+"!");
                } else {
                    log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                    player.sendMessage(ChatColor.AQUA + "Warp not found!");
                }
            } else {
                commandSender.sendMessage("You need to be a player to warp!");
            }
            return true;
        } else if(command.getName().equalsIgnoreCase("reloadwarps")) {
            if(commandSender instanceof Player)
            {
                Player player = (Player) commandSender;
                if(!check(player, "tweakwarp.reloadwarps"))
                    return true;
                log.info("[TweakWarp] "+player.getName()+" issued /reloadwarps!");
            } else {
                log.info("[TweakWarp] console issued /reloadwarps!");
            }

            commandSender.sendMessage(ChatColor.GREEN + "Reloading warps table");
            this.reloadWarpTable(true);

            return true;
        } else if(command.getName().equals("warpback")) {
            if(commandSender instanceof Player) {
                Player p = (Player) commandSender;
                if(!check(p, "tweakcraftutils.tpback")) {
                    p.sendMessage("You don't have permission to tpback, so this would be useless!");
                    return true;
                }

                if(saveWarps.contains(p.getName())) {
                	p.sendMessage(ChatColor.GOLD+"Warping will no longer save a TPBack instance!");
                    saveWarps.add(p.getName());
                } else {
                	p.sendMessage(ChatColor.GOLD+"Warping will save a TPBack instance!");
                    saveWarps.remove(p.getName());
                }
            } else {
                commandSender.sendMessage("Consoles need a tp history nowadays?");
            }
            return true;
        }
        return false;
    }
}
