package com.mcsunnyside.ynlc;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.mcsunnyside.ynlc.Metrics;

import java.lang.reflect.Field;

public class Main extends JavaPlugin implements Listener {
	private final String name = Bukkit.getServer().getClass().getPackage().getName();
    private final String version = name.substring(name.lastIndexOf('.') + 1);
    private final DecimalFormat format = new DecimalFormat("##.##");
    private Object serverInstance;
    private Field tpsField;
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		reloadConfig();
		new Metrics(this); //bStats
		try {
            serverInstance = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
            tpsField = serverInstance.getClass().getField("recentTps");
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChatting(AsyncPlayerChatEvent event) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		if(event.isAsynchronous()&&event.getMessage().equals("1")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GOLD+""+getConfig().getString("servername") +ChatColor.GREEN+ " >> "+ChatColor.LIGHT_PURPLE+"您没有掉线喵  您的网络延迟:"+getPing(event.getPlayer())+"ms"+ChatColor.LIGHT_PURPLE+" 服务器tps:"+getTps()+ChatColor.LIGHT_PURPLE+"/"+ChatColor.GREEN+"20.0");
			event.getPlayer().sendMessage(ChatColor.GOLD+""+getConfig().getString("servername") +ChatColor.GREEN+ " >> "+ChatColor.LIGHT_PURPLE+"如果想要发送单纯的数字1，请发送"+ChatColor.GOLD+"\"!1\"");
		}else if(event.isAsynchronous()&&event.getMessage().equals("!1")) {
			event.setMessage(event.getMessage().replaceAll("!1", "1"));
		}
	}
	private String getPing(Player player) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException{
	    int ping = 0;
	    ChatColor color = ChatColor.AQUA;
	      Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." +Bukkit.getServer().getClass().getPackage().getName().substring(23) + ".entity.CraftPlayer");
	      Object handle = craftPlayerClass.getMethod("getHandle", new Class[0]).invoke(craftPlayerClass.cast(player), new Object[0]);
	      ping = handle.getClass().getDeclaredField("ping").getInt(handle);
	    if(ping <= 100) {
	    	color = ChatColor.GREEN;
	    }else if(ping <= 200) {
	    	color = ChatColor.YELLOW;
	    }else {
	    	color = ChatColor.RED;
	    }
	    return String.valueOf(color+""+ping);
	}
	private String getTps() {
		    double tps = Double.valueOf(this.getTPS(0));
		    if(tps>20) {tps = 20;}
		    ChatColor color = null;
		    if(tps >= 18.5) {
		    	color = ChatColor.GREEN;
		    }else if(tps >= 17.1) {
		    	color = ChatColor.YELLOW;
		    }else {
		    	color = ChatColor.RED;
		    }
		    return ""+color+tps;
	}
	private Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
	public String getTPS(int time) {
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return format.format(tps[time]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
