package shipWars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class game {

	// Is the game on = played at the moment
	public boolean isOn;

	// Each teams players
	public List<Player> redTeam = new ArrayList<Player>();
	public List<Player> blueTeam = new ArrayList<Player>();

	// Each teams spawn location
	public Location redLoation; // Found out about this spelling mistake , don't
								// feel like changing it , it stays that way
	public Location blueLocation;
	
	// Other locations
	public Location spawnLoc;
	public Location signLoc;

	// game sign variable
	public Sign sign;
	
	// is the timer running
	private boolean isRunning = false;
	
	//Block destroyed during game
	public ArrayList<Location> destBlockLoc;
	public ArrayList<Material> destBlockMat;
	public ArrayList<Byte> destBlockMeta;

	// the number of ticks for timer's starting position
	private int tickLeft;
	
	public ArrayList<Arrow> arrowShot;
	
	// game toString method
	@Override
	public String toString() {
		String ret = "";
		ret += redLoation.getWorld().getName() + "," + redLoation.getX() + "," + redLoation.getY() + ","
				+ redLoation.getZ() + ";";
		ret += blueLocation.getWorld().getName() + "," + blueLocation.getX() + "," + blueLocation.getY() + ","
				+ blueLocation.getZ() + ";";
		ret += signLoc.getWorld().getName() + "," + signLoc.getX() + "," + signLoc.getY() + "," + signLoc.getZ();
		return ret;
	}

	// Method that get world from world name
	public static World getWorld(String text) {
		return Bukkit.getWorld(text);
	}

	// Constructor
	public game(Location redLoc, Location blueLoc, Location signLocation) {
		this.redLoation = redLoc;
		this.blueLocation = blueLoc;
		this.signLoc = signLocation;
		this.sign = (Sign) signLocation.getWorld().getBlockAt(signLocation).getState();
		spawnLoc = new Location(this.signLoc.getWorld(), signLoc.getX(), signLoc.getY(), signLoc.getZ() - 1);
		this.sign.setLine(1, "No players");
		sign.setLine(2, "");
		sign.setLine(3, "");
		this.sign.update(true);
		isOn = false;
		Data.list.add(this);
		isRunning = false;
		tickLeft = Data.getSecondsOfCountdown() * 20;
		System.out.println("New game added");
		destBlockLoc = new ArrayList<Location>();
		destBlockMat = new ArrayList<Material>();
		destBlockMeta = new ArrayList<Byte>();
		arrowShot = new ArrayList<Arrow>();
	}
	
	
	// Constructor
		public game(Location redLoc, Location blueLoc, Location signLocation , Location spawnLocation) {
			this.redLoation = redLoc;
			this.blueLocation = blueLoc;
			this.signLoc = signLocation;
			this.sign = (Sign) signLocation.getWorld().getBlockAt(signLocation).getState();
			spawnLoc = spawnLocation;
			this.sign.setLine(1, "No players");
			sign.setLine(2, "");
			sign.setLine(3, "");
			this.sign.update(true);
			isOn = false;
			Data.list.add(this);
			isRunning = false;
			tickLeft = Data.getSecondsOfCountdown() * 20;
			System.out.println("New game added");
			destBlockLoc = new ArrayList<Location>();
			destBlockMat = new ArrayList<Material>();
			destBlockMeta = new ArrayList<Byte>();
			arrowShot = new ArrayList<Arrow>();
		}

	// Ads player to game and assigns to team
	public void addPlayer(Player player) {
		// If at least one team is not full
		if (!isOn && (blueTeam.size() < Data.getMaxPlayer() || redTeam.size() < Data.getMaxPlayer())) {

			// if blue team has less players
			if (blueTeam.size() < redTeam.size()) {
				blueTeam.add(player);
				player.sendMessage(ChatColor.BLUE + "You are in the Blue Team");
			}

			// If red team has less player
			else if (blueTeam.size() > redTeam.size()) {
				redTeam.add(player);
				player.sendMessage(ChatColor.RED + "You are in the Red Team");
			}

			// If both teams has the same amount of players add to red
			else {
				redTeam.add(player);
				player.sendMessage(ChatColor.RED + "You are in the Red Team");
			}

			checkTimer();

		}
	}

	// Checks if game is over , and acts
	public void gameOver() {
		if (isOn) {
			if (redTeam.size() == 0 && blueTeam.size() != 0) {

				try {
					for (Player temp : redTeam) {
						temp.teleport(spawnLoc);
						temp.getInventory().clear();
						temp.setGameMode(GameMode.SURVIVAL);
						redTeam.remove(temp);
					}
				} catch (Exception e) {

				}
				try {
					for (Player temp : blueTeam) {
						temp.teleport(spawnLoc);
						temp.setGameMode(GameMode.SURVIVAL);
						temp.getInventory().clear();
						blueTeam.remove(temp);
					}
				} catch (Exception e) {

				}

				Bukkit.broadcastMessage(ChatColor.BLUE + "BLUE TEAM WON !");
				restoreBlocks();
				redTeam.clear();
				blueTeam.clear();
				sign.setLine(0, "Waiting");
				sign.update(true);
				sign.setLine(1, "No players");
				sign.update(true);
				sign.setLine(2, "");
				sign.update(true);
				sign.setLine(3, "");
				sign.update(true);
				for(Arrow a : arrowShot){
					a.remove();
				}
				arrowShot.clear();
				isOn = false;
			}

			else if (blueTeam.size() == 0 && redTeam.size() != 0) {
				try {
					for (Player temp : redTeam) {
						temp.teleport(spawnLoc);
						temp.getInventory().clear();
						temp.setGameMode(GameMode.SURVIVAL);
						redTeam.remove(temp);
					}
				} catch (Exception e) {

				}
				try {
					for (Player temp : blueTeam) {
						temp.teleport(spawnLoc);
						temp.setGameMode(GameMode.SURVIVAL);
						temp.getInventory().clear();
						blueTeam.remove(temp);
					}
				} catch (Exception e) {

				}

				Bukkit.broadcastMessage(ChatColor.RED + "RED TEAM WON !");
				restoreBlocks();
				redTeam.clear();
				blueTeam.clear();
				sign.setLine(0, "Waiting");
				sign.update(true);
				sign.setLine(1, "No players");
				sign.update(true);
				sign.setLine(2, "");
				sign.update(true);
				sign.setLine(3, "");
				sign.update(true);
				for(Arrow a : arrowShot){
					a.remove();
				}
				arrowShot.clear();
				isOn = false;

			} else if (blueTeam.size() == 0 && redTeam.size() == 0) {
				for (Player temp : redTeam) {
					temp.teleport(spawnLoc);
					temp.getInventory().clear();
					temp.setGameMode(GameMode.SURVIVAL);
					redTeam.remove(temp);

				}

				for (Player temp : blueTeam) {
					temp.teleport(spawnLoc);
					temp.setGameMode(GameMode.SURVIVAL);
					temp.getInventory().clear();
					blueTeam.remove(temp);
				}
				
				Bukkit.broadcastMessage(ChatColor.GREEN + "TIE !");
				restoreBlocks();
				redTeam.clear();
				blueTeam.clear();
				sign.setLine(0, "Waiting");
				sign.update(true);
				sign.setLine(1, "No players");
				sign.update(true);
				sign.setLine(2, "");
				sign.update(true);
				sign.setLine(3, "");
				sign.update(true);
				isOn = false;
				for(Arrow a : arrowShot){
					a.remove();
				}
				arrowShot.clear();
				forceStop();
				Data.error(6);
			}
		}
	}

	// forces the game to stop on server close
	public void forceStop() {
		try {
			for (Player p : redTeam) {
				p.getInventory().clear();
				p.setGameMode(GameMode.SURVIVAL);
				redTeam.remove(p);
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}

		try {
			for (Player p : blueTeam) {
				p.getInventory().clear();
				p.setGameMode(GameMode.SURVIVAL);
				blueTeam.remove(p);
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}
		
		restoreBlocks();
		for(Arrow a : arrowShot){
			a.remove();
		}
		arrowShot.clear();

		sign.setLine(0, "Waiting");
		sign.setLine(1, "No players");
		sign.setLine(2, "");
		sign.setLine(3, "");
		sign.update(true);
		isOn = false;
	}
	
	
	public void restoreBlocks(){
		
		if(destBlockLoc.size() == destBlockMat.size() && destBlockLoc.size() == destBlockMeta.size()){
			
			for(int a = 0 ; a<destBlockLoc.size() ; a++){
				Block block = destBlockLoc.get(a).getWorld().getBlockAt(destBlockLoc.get(a));
				block.setType(destBlockMat.get(a));
				block.setData(destBlockMeta.get(a));
			}
			
			destBlockLoc.clear();
			destBlockMat.clear();
			destBlockMeta.clear();
			
		} else {
			Data.error(1);
		}
			
	}

	// Checks if can start or stop timer , and acts
	public void checkTimer() {
		if (redTeam.size() >= Data.getMinPlayer() && blueTeam.size() >= Data.getMinPlayer() && !isRunning) {
			startTimer();
		} else if (isRunning && !(redTeam.size() >= Data.getMinPlayer() && blueTeam.size() >= Data.getMinPlayer())) {
			stopTimer();
		}
	}

	// This method is called every one minecraft tick , tick is around 0.05
	// seconds , 20 ticks a second
	public void onTickEvent() {
		if (isRunning) {
			tickLeft = tickLeft - 1;
			if (tickLeft == 0) {
				isRunning = false;
				startGame();
			} else if (tickLeft % 20 == 0 && tickLeft > 0) {
				Bukkit.broadcastMessage(String.valueOf(tickLeft / 20) + " seconds left");
			}
			isRunning = redTeam.size() >= Data.getMinPlayer() && blueTeam.size() >= Data.getMinPlayer();
		}

		// Sets max health to everybody each tick
		try {
			for (Player p : redTeam) {
				p.setHealth(20);
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}

		try {
			for (Player p : blueTeam) {
				p.setHealth(20);
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}

	}

	public void startTimer() {
		isRunning = true;
		tickLeft = Data.getSecondsOfCountdown() * 20;
	}

	public void stopTimer() {
		isRunning = false;
	}

	// Checks if player is in the specified game
	public boolean isPlayerIn(Player player) {
		return redTeam.contains(player) || blueTeam.contains(player);
	}

	// Removes a player from game that has already started
	public void removePlayer(Player player) {
		try {
			if (redTeam.contains(player)) {
				for (Player red : redTeam) {
					if (red.equals(player)) {
						redTeam.remove(player);
						red.sendMessage("You Have Been Removed");
						player.setGameMode(GameMode.SURVIVAL);
						player.getInventory().clear();
						if (isOn) {
							player.teleport(spawnLoc);
							sign.setLine(0, "Currently Playing");
							sign.setLine(1, "Players : " + String.valueOf(this.redTeam.size() + this.blueTeam.size())
									+ "/" + String.valueOf(Data.getMaxPlayer() * 2));
							sign.setLine(2, "");
							sign.setLine(3, "");
							sign.update(true);
						} else {
							sign.setLine(0, "Waiting");
							sign.setLine(1, "Players : " + String.valueOf(this.redTeam.size() + this.blueTeam.size())
									+ "/" + String.valueOf(Data.getMaxPlayer() * 2));
							sign.setLine(2, "");
							sign.setLine(3, "");
							sign.update(true);
						}

						sign.update(true);
						
						gameOver();
					}
				}
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}

		try {
			if (blueTeam.contains(player)) {

				for (Player blue : blueTeam) {
					if (blue.equals(player)) {
						blueTeam.remove(player);
						blue.sendMessage("You Have Been Removed");
						player.setGameMode(GameMode.SURVIVAL);
						player.getInventory().clear();
						if(isOn){
							player.teleport(spawnLoc);
						}
						if (redTeam.size() > 0 || blueTeam.size() > 0) {
							if (isOn) {
								sign.setLine(0, "Currently Playing");
								sign.setLine(1,
										"Players : " + String.valueOf(this.redTeam.size() + this.blueTeam.size()) + "/"
												+ String.valueOf(Data.getMaxPlayer() * 2));
								sign.setLine(2, "");
								sign.setLine(3, "");
							} else {
								sign.setLine(0, "Waiting");
								sign.setLine(1,
										"Players : " + String.valueOf(this.redTeam.size() + this.blueTeam.size()) + "/"
												+ String.valueOf(Data.getMaxPlayer() * 2));
								sign.setLine(2, "");
								sign.setLine(3, "");
							}
							sign.update(true);
							
							gameOver();
						} else {
							if (isOn) {
								gameOver();
							} else {
								sign.setLine(0, "Waiting");
								sign.setLine(1,
										"Players : " + String.valueOf(this.redTeam.size() + this.blueTeam.size()) + "/"
												+ String.valueOf(Data.getMaxPlayer() * 2));
								sign.setLine(2, "");
								sign.setLine(3, "");
								sign.update(true);
							}
						}
					}

				}
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}

		checkTimer();
	}

	// Starts the game , after coundown is finished
	public void startGame() {
		for (int blue = 0; blue < blueTeam.size(); blue++) {
			// blueTeam.get(blue).setGameMode(GameMode.SURVIVAL);
			blueTeam.get(blue).sendMessage("Starting game !");
			blueTeam.get(blue).setHealth(20);
			blueTeam.get(blue).teleport(this.blueLocation);
			blueTeam.get(blue).getInventory().clear();
			ItemStack item = new ItemStack(Material.BOW);
			item.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			blueTeam.get(blue).getInventory().addItem(item);
			item = new ItemStack(Material.ARROW);
			blueTeam.get(blue).getInventory().addItem(item);
		}

		for (int red = 0; red < redTeam.size(); red++) {
			// redTeam.get(red).setGameMode(GameMode.SURVIVAL);
			redTeam.get(red).sendMessage("Starting game !");
			redTeam.get(red).setHealth(20);
			redTeam.get(red).teleport(this.redLoation);
			redTeam.get(red).getInventory().clear();
			ItemStack item = new ItemStack(Material.BOW);
			item.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			redTeam.get(red).getInventory().addItem(item);
			item = new ItemStack(Material.ARROW);
			redTeam.get(red).getInventory().addItem(item);
		}

		isOn = true;

		sign.setLine(0, "Currently Playing");
		sign.setLine(1, "Players : " + String.valueOf(redTeam.size() + blueTeam.size()) + "/"
				+ String.valueOf(Data.getMaxPlayer() * 2));
		sign.setLine(2, "");
		sign.setLine(3, "");
		sign.update(true);

	}

	// game equals method
	@Override
	public boolean equals(Object e) {
		game g = (game) e;
		if (signLoc.equals(g.signLoc)) {
			return true;
		} else {
			return false;
		}
	}
}
