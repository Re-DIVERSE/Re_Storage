package com.github.re_diverse.re_storage.container.enderchest;

import com.github.re_diverse.re_storage.Re_Storage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderchestListener implements Listener {

	public static Map<Player, Enderchest> renamePages = new HashMap<>();

	/**
	 * エンダーチェストデータ読み込み
	 * @param event イベント
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		UUID uuid = event.getPlayer().getUniqueId();
		EnderchestManager.enderchest.remove(uuid);

		// データ読み込み
		Bukkit.getScheduler().runTaskTimerAsynchronously(Re_Storage.getInstance(), (task) -> {
			if(!EnderchestManager.checkSync(uuid)) {
				Bukkit.getScheduler().runTaskAsynchronously(Re_Storage.getInstance(),
						() -> EnderchestManager.loadEnderchest(uuid)
				);
				task.cancel();
			}
		}, 60L, 20L);
	}

	/**
	 * エンダーチェストデータ保存
	 * @param event イベント
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		UUID uuid = event.getPlayer().getUniqueId();

		// データ保存
		Bukkit.getScheduler().runTaskAsynchronously(Re_Storage.getInstance(),
				() -> {
					EnderchestManager.saveEnderChest(uuid);
					EnderchestManager.enderchest.remove(uuid);
				}
		);
	}

	/**
	 * エンダーチェストクリック時処理
	 * @param event イベント
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onEnderchestOpen(PlayerInteractEvent event) {

		// ブロッククリックの場合
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {

			// クリック対象のブロックが存在しない場合
			if(event.getClickedBlock() == null) return;

			// クリック対象がエンダーチェストの場合
			if(event.getClickedBlock().getType() == Material.ENDER_CHEST) {
				EnderchestManager.open(event.getPlayer());
				event.setCancelled(true);
			}
		}
	}

	/**
	 * ページのロック解除
	 * @param event イベント
	 */
	@EventHandler
	public void onEnderchestPageClose(InventoryCloseEvent event) {

		// ページのインベントリかどうかチェック
		Inventory inv = event.getInventory();
		if(!(inv.getHolder() instanceof EnderchestPage)) return;

		// 独自クラスにキャスト
		EnderchestPage ecPage = (EnderchestPage)inv.getHolder();

		// ロック解除
		ecPage.unlock();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEnderchestRename(AsyncPlayerChatEvent event) {

		// 対象外の場合は処理しない
		if(!renamePages.containsKey(event.getPlayer())) return;

		if(event.getMessage().length() > 15) {
			event.getPlayer().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cページ名が長すぎます。(15文字以内)"));
			event.setCancelled(true);
			return;
		}

		// ページを得る
		Enderchest ec = renamePages.get(event.getPlayer());
		int targetPage = ec.procPage;
		EnderchestPage ecPage = ec.getPage(targetPage);

		// コピーする
		EnderchestPage newPage = EnderchestPage.pageClone(ec, ecPage, LegacyComponentSerializer.legacyAmpersand().deserialize(event.getMessage()));
		ec.setPage(targetPage, newPage);

		// 開き直す
		Bukkit.getScheduler().runTaskLater(Re_Storage.getInstance(), () -> {
			int page = 0;
			if(targetPage > 0) {
				page = (int)Math.floor((double)targetPage / 45.0d) * 45;
			}
			ec.show(event.getPlayer(), page);
		}, 1L);

		// イベントをキャンセル
		event.setCancelled(true);

		// モード解除
		renamePages.remove(event.getPlayer());
	}
}
