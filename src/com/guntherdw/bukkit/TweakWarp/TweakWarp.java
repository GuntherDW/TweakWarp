package com.guntherdw.bukkit.TweakWarp;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import com.sun.xml.internal.ws.api.server.InstanceResolver;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import sun.reflect.generics.tree.ReturnType;

import javax.jnlp.ExtendedService;
import javax.persistence.PersistenceException;


/**
 * @author GuntherDW
 */
public class TweakWarp extends JavaPlugin {

    private final static Logger log = Logger.getLogger("Minecraft");
    // private static Connection conn;

    public static Permissions perm = null;
    public Map<String, Warp> warps = new HashMap<String, Warp>();

    public List<String> saveWarps;
    public TweakcraftUtils tweakcraftutils;

    public Warp searchWarp(String warpname)
    {
        Warp warp = null;
        for(String search : warps.keySet())
        {
            if(search.equalsIgnoreCase(warpname))
            {
                warp = warps.get(search);
                return warp;
            }
        }
        return warp;
    }



    public String getWarpList()
    {
        String msg = "";
        // String[] lijstje = (String[]) warps.keySet().toArray();
        List<String> lijst = new ArrayList<String>();
        for(String m : warps.keySet())
        {
            lijst.add(m);
        }
        Collections.sort(lijst, String.CASE_INSENSITIVE_ORDER);
        for(String m : lijst)
        {
            msg += m+", ";
        }
        if(msg.length()!=0) msg = msg.substring(0, msg.length()-2);
        return msg;
    }

    public boolean removeWarp(String warpname) {
        Warp w = getDatabase().find(Warp.class).where().ieq("name", warpname).findUnique();
        if(w != null) {
            getDatabase().delete(w);
            return true;
        } else {
            return false;
        }
    }

    public boolean addWarp(String warpname, Warp warp)
    {
        if(warps.containsKey(warpname)) {
            this.getDatabase().delete(warps.get(warpname));
        }
        this.getDatabase().save(warp);
        warps.put(warpname, warp);
        return true;
    }

    public void reloadWarpTable() {
        List<Warp> warpies =  this.getDatabase().find(Warp.class).findList();
        warps.clear();
        for(Warp w : warpies) {
            warps.put(w.getName(), w);
        }
    }

    public void onDisable() {
        log.info("[TweakWarp] Shutting down!");
    }

    private void setupDatabase() {
        try {
            getDatabase().find(Warp.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Warp.class);
        return list;
    }

    public void onEnable() {


        setupPermissions();
        setupTCUtils();

        saveWarps = new ArrayList<String>();
        // reloadWarpTable(false);
        setupDatabase();
        reloadWarpTable();
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
            String msg = this.getWarpList();
            commandSender.sendMessage(msg);
            return true;
        } else if(command.getName().equalsIgnoreCase("removewarp")) {
            // if(commandSender instanceof Player)
            // {
            // Player player = (Player) commandSender;
            if(commandSender instanceof Player)
                if(!(check((Player)commandSender, "tweakwarp.removewarp"))) {
                    commandSender.sendMessage(ChatColor.RED+"You do not have the correct permission!");
                    return true;
                }

            String warpname = strings[0];
            if(warps.containsKey(warpname)) {
                if(removeWarp(warpname))
                {
                    commandSender.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' removed!");
                } else {
                    commandSender.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
                }
            } else {
                commandSender.sendMessage(ChatColor.AQUA + "Warp with name '"+warpname+"' not found!");
            }

            this.reloadWarpTable();
            return true;
            /* } else {
                commandSender.sendMessage("You need to be a player to remove a warp!");
            } */
        } else if(command.getName().equalsIgnoreCase("setwarp")) {
            if(commandSender instanceof Player)
            {
                Player player = (Player) commandSender;
                if(check(player, "tweakwarp.setwarp"))
                {
                    String warpname = strings[0];
                    Warp tempwarp = new Warp();
                    tempwarp.construct(player.getLocation().getX(),
                            player.getLocation().getY(),
                            player.getLocation().getZ(),
                            player.getLocation().getYaw(),
                            player.getLocation().getPitch(),
                            warpname,
                            player.getLocation().getWorld().getName());
                    if(addWarp(warpname, tempwarp))
                    {
                        log.info("[TweakWarp] Warp '"+warpname+"' created by "+player.getName()+"!");
                        player.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' created!");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
                    }
                    this.reloadWarpTable();
                } else {
                    player.sendMessage("You do not have the correct permissions");
                }
            } else {
                commandSender.sendMessage("You need to be a player to set a warp!");
            }
            return true;
        } else if(command.getName().equalsIgnoreCase("warp")) {
            if(commandSender instanceof Player)
            {
                Player player = (Player) commandSender;
                if(check(player, "tweakwarp.warp"))
                {
                    if(strings.length==1)
                    {
                        String warpname = strings[0];
                        Warp w = searchWarp(warpname);

                        if(w != null)
                        {
                            player.sendMessage(ChatColor.AQUA + "Found warp with name "+w.getName());
                            Location loc = new Location(this.getServer().getWorld(w.getWorld()),
                                    w.getX(), w.getY() + 1, w.getZ(), w.getPitch(), w.getYaw());
                            if(tweakcraftutils!=null) {
                                if(!saveWarps.contains(player.getName())) {
                                    tweakcraftutils.getTelehistory().addHistory(player.getName(), player.getLocation());
                                }
                            }
                            player.teleport(loc);
                            player.sendMessage(ChatColor.AQUA + "WHOOOSH!");
                            log.info("[TweakWarp] "+player.getName()+" warped to "+w.getName()+"!");
                        } else {
                            log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                            player.sendMessage(ChatColor.AQUA + "Warp not found!");
                        }
                    } else {
                        player.sendMessage(ChatColor.AQUA + command.getUsage());
                    }
                } else {
                    player.sendMessage("You don't have permission to warp!");
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
            this.reloadWarpTable();

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
