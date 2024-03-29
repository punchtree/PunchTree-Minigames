package net.punchtree.minigames.lobby;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.punchtree.minigames.MinigamesPlugin;

public class LobbyControls implements Listener {
	
	private static final ItemStack READY_BLOCK;
	private static final ItemStack NOT_READY_BLOCK;
	private static final ItemStack LEAVE_LOBBY;
	static {
		//Initializing the item meta for READY_BLOCK
		READY_BLOCK = new ItemStack(Material.GREEN_CONCRETE);
		ItemMeta im = READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "" + ChatColor.ITALIC + "READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click if you're",
				                                  "no longer ready!"));
		READY_BLOCK.setItemMeta(im);
		
		//Initializing the item meta for NOT_READY_BLOCK
		NOT_READY_BLOCK = new ItemStack(Material.RED_CONCRETE);
		im = NOT_READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "NOT READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click when you're",
                                                  "ready to play!"));
		NOT_READY_BLOCK.setItemMeta(im);
		
		LEAVE_LOBBY = new ItemStack(Material.BARRIER);
		im = LEAVE_LOBBY.getItemMeta();
		im.setDisplayName(ChatColor.RED + "Leave the lobby");
		LEAVE_LOBBY.setItemMeta(im);
	}
	
	private final PerMapLegacyLobby lobby;
	
	public LobbyControls(PerMapLegacyLobby lobby) {
		this.lobby = lobby;
		// TODO This leaks memory (when reloading) - abstract out singleton listener
		Bukkit.getServer().getPluginManager().registerEvents(this, MinigamesPlugin.getInstance());
	}
	
	public void giveLobbyControls(Player player) {
		player.getInventory().clear();
		player.getInventory().setItem(0, NOT_READY_BLOCK);
		player.getInventory().setItem(1, NOT_READY_BLOCK);
		player.getInventory().setItem(2, NOT_READY_BLOCK);
		player.getInventory().setItem(3, NOT_READY_BLOCK);
		player.getInventory().setItem(4, NOT_READY_BLOCK);
		player.getInventory().setItem(5, NOT_READY_BLOCK);
		player.getInventory().setItem(6, NOT_READY_BLOCK);
		player.getInventory().setItem(7, NOT_READY_BLOCK);
		player.getInventory().setItem(8, LEAVE_LOBBY);
		player.getInventory().setHeldItemSlot(0);
	}
	
	@EventHandler
	public void onToggleReadiness(PlayerInteractEvent e){
		
		Player player = e.getPlayer();
		//TODO Fix being able to move ready block in inventory?
		if(! lobby.hasPlayer(player)) return;
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			if (LEAVE_LOBBY.equals(e.getItem())) {
				e.setCancelled(true);
				new BukkitRunnable() {
					public void run() {
						lobby.removeAndRestorePlayer(e.getPlayer());
					}
				}.runTaskLater(MinigamesPlugin.getInstance(), 1);
			} else if (READY_BLOCK.equals(e.getItem())){
				lobby.setPlayerReadiness(player, false);
				player.getInventory().setItem(0, NOT_READY_BLOCK);
				player.getInventory().setItem(1, NOT_READY_BLOCK);
				player.getInventory().setItem(2, NOT_READY_BLOCK);
				player.getInventory().setItem(3, NOT_READY_BLOCK);
				player.getInventory().setItem(4, NOT_READY_BLOCK);
				player.getInventory().setItem(5, NOT_READY_BLOCK);
				player.getInventory().setItem(6, NOT_READY_BLOCK);
				player.getInventory().setItem(7, NOT_READY_BLOCK);
				e.setCancelled(true);
			} else if (NOT_READY_BLOCK.equals(e.getItem())) {
				lobby.setPlayerReadiness(player, true);
				player.getInventory().setItem(0, READY_BLOCK);
				player.getInventory().setItem(1, READY_BLOCK);
				player.getInventory().setItem(2, READY_BLOCK);
				player.getInventory().setItem(3, READY_BLOCK);
				player.getInventory().setItem(4, READY_BLOCK);
				player.getInventory().setItem(5, READY_BLOCK);
				player.getInventory().setItem(6, READY_BLOCK);
				player.getInventory().setItem(7, READY_BLOCK);
				e.setCancelled(true);
				
				lobby.startCountdownIfStartConditionsMet();
				 
			}
			
		}
	}
	
	//Cancels dropping the readiness indicator
	@EventHandler
	public void onDropReadyBlock(PlayerDropItemEvent e){
		if (READY_BLOCK.equals(e.getItemDrop().getItemStack()) || NOT_READY_BLOCK.equals(e.getItemDrop().getItemStack())){
			e.setCancelled(true);
		}
	}
	
	//Cancel pulling blocks out of the inventory
	@EventHandler
	private void onInventoryDrag(InventoryDragEvent e){
		Player whoClicked = (Player) e.getWhoClicked();
		// TODO change this for production
		if(lobby.hasPlayer(whoClicked) && whoClicked.getGameMode() != GameMode.CREATIVE){
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e){
		Player whoClicked = (Player) e.getWhoClicked();
		// TODO change this for production
		if(lobby.hasPlayer(whoClicked) && whoClicked.getGameMode() != GameMode.CREATIVE){
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onPlayerLeaveServer(PlayerQuitEvent e) {
		lobby.removeAndRestorePlayer(e.getPlayer());
	}

	@EventHandler
	private void onLeaveCommand(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().toLowerCase().startsWith("/leave") && lobby.hasPlayer(e.getPlayer())) {
			lobby.removeAndRestorePlayer(e.getPlayer());
			e.setCancelled(true);
		}
	}
	
}
