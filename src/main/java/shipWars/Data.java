package shipWars;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Data {

	public static ArrayList<game> list = new ArrayList<game>();
	private static int minPlayer = 1;
	private static int secondsOfCountdown = 15;
	private static int maxPlayer = 16;
	private static boolean arrowDamage = true;

	// GETTERS AND SETTERS
	public static void setMinPlayer(int min) {
		minPlayer = min;
	}

	public static int getMinPlayer() {
		return minPlayer;
	}
	
	public static void setSecondsOfCountdown(int temp) {
		secondsOfCountdown = temp;
	}

	public static int getSecondsOfCountdown() {
		return secondsOfCountdown;
	}

	public static void setMaxPlayer(int temp) {
		maxPlayer = temp;
	}

	public static int getMaxPlayer() {
		return maxPlayer;
	}

	public static void setArrowDamage(boolean temp) {
		arrowDamage = temp;
	}

	public static boolean getArrowDamage() {
		return arrowDamage;
	}

	// Saves the game ArrayList to an external file
	public static void saveToFile() {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("shipWars");
		FileConfiguration config = plugin.getConfig();
		ArrayList<String> tempList = new ArrayList<String>();

		config.set("shipWars.game", null);

		for (game g : list) {
			tempList.add(g.toString());
		}

		config.set("shipWars.game.length", tempList.size());

		int counter = 0;
		for (String str : tempList) {
			config.set("shipWars.game.index" + String.valueOf(counter), str);
			counter++;
		}

		config.set("shipWars.minPlayer", minPlayer);
		config.set("shipWars.secondsOfCountdown", secondsOfCountdown);
		config.set("shipWars.maxPlayer", maxPlayer);
		config.set("shipWars.isArrowDamage", arrowDamage);

		plugin.saveConfig();

	}

	// Retrieves the game ArrayList from the file
	public static void getGameList() {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("shipWars");
		FileConfiguration config = plugin.getConfig();
		int counter = config.getInt("shipWars.game.length");
		list.clear();

		for (int i = 0; i < counter; i++) {
			String temp = config.getString("shipWars.game.index" + String.valueOf(i));
			String[] tempArray = temp.split(";");
			if (tempArray.length == 3) {
				Location tempRed = getLocation(tempArray[0]);
				Location tempBlue = getLocation(tempArray[1]);
				Location tempSign = getLocation(tempArray[2]);
				System.out.println("Adding new game");
				new game(tempRed, tempBlue, tempSign);
			} else {
				error(0);
			}
		}

		try {
			if(config.contains("shipWars.minPlayer")){
					minPlayer = config.getInt("shipWars.minPlayer");
					System.out.println("Variable loaded");
			} else {
				System.out.println("Variable not found");
				minPlayer = 1;
			}
			
		} catch (Exception e) {
			System.out.println("Variable not found");
			minPlayer = 1;
		}

		try {
			if(config.contains("shipWars.secondsOfCountdown")){
				secondsOfCountdown = config.getInt("shipWars.secondsOfCountdown");
				System.out.println("Variable loaded");
			} else {
				System.out.println("Variable not found");
				secondsOfCountdown = 15;
			}
		} catch (Exception e) {
			System.out.println("Variable not found");
			secondsOfCountdown = 15;
		}

		try {
			maxPlayer = config.getInt("shipWars.maxPlayer");
			if(config.contains("shipWars.maxPlayer")){
				maxPlayer = config.getInt("shipWars.maxPlayer");
				System.out.println("Variable loaded");
			} else {
				System.out.println("Variable not found");
				maxPlayer = 16;
			}
			
		} catch (Exception e) {
			System.out.println("Variable not found");
			maxPlayer = 16;
		}

		try {
			if(config.contains("shipWars.isArrowDamage")){
				arrowDamage = config.getBoolean("shipWars.isArrowDamage");
				System.out.println("Variable loaded");
			} else {
				System.out.println("Variable not found");
				arrowDamage = false;
			}
		} catch (Exception e) {
			System.out.println("Variable not found");
			arrowDamage = false;
		}
		
		
		
		
	}

	public static Location getLocation(String locText) {
		Location ret = null;

		String[] array = locText.split(",");
		if (array.length == 4) {
			World world = game.getWorld(array[0]);
			Double x = Double.valueOf(array[1]);
			Double y = Double.valueOf(array[2]);
			Double z = Double.valueOf(array[3]);

			ret = new Location(world, x, y, z);
		}

		return ret;
	}

	@SuppressWarnings("deprecation")
	public static void error(int errorCode) {
		// Bukkit.broadcastMessage("ERROR #" + String.valueOf(errorCode) + " !
		// Call a programmer");
		System.out.println(
				"ERROR #" + String.valueOf(errorCode) + " ! Call a programmer ---------------------------------");
		if (Bukkit.getPlayer("DirtyAxe").isOnline()) {
			Bukkit.getPlayer("DirtyAxe").sendMessage("shipWars error , check log");
		} else if (Bukkit.getPlayer("yotam_salmon").isOnline()) {
			Bukkit.getPlayer("yotam_salmon").sendMessage("shipWars error , let DirtyAxe check the server log");
		}
	}
}
