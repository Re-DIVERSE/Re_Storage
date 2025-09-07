package com.github.re_diverse.re_storage.container.playerinventory;

import com.github.re_diverse.re_storage.Re_Storage;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlayerInventoryManager {

	// 保存するNBTのフィルター
	public static List<String> nbtFilter;

	// エンダーチェスト
	public static Map<InventoryKey, Map<Short, ItemStack>> inventories = new HashMap<>();

	/**
	 * インベントリの中身を取得
	 * @param key 対象インベントリ
	 */
	public static Map<Short, ItemStack> getInventory(InventoryKey key) {
		Map<Short, ItemStack> items;
		if(inventories.containsKey(key)) items = inventories.get(key);
		else items = new HashMap<>();
		return items;
	}
	public static Map<Short, ItemStack> getInventory(UUID uuid, InventorySaveType type) {
		InventoryKey key = new InventoryKey(uuid, type);
		return getInventory(key);
	}

	/**
	 * インベントリをセット
	 * @param key 対象インベントリ
	 * @param items インベントリの中身
	 */
	public static void setInventory(InventoryKey key, Map<Short, ItemStack> items) {
		inventories.put(key, items);
	}
	public static void setInventory(UUID uuid, InventorySaveType type, Map<Short, ItemStack> items) {
		InventoryKey key = new InventoryKey(uuid, type);
		setInventory(key, items);
	}

	/**
	 * インベントリをアンロード
	 * @param key 対象インベントリ
	 * @param save アンロード時に保存するか
	 */
	public static void unloadInventory(InventoryKey key, boolean save) {
		if(save) {
			Bukkit.getScheduler().runTaskAsynchronously(Re_Storage.getInstance(), () -> {
				saveInventory(key.uuid, key.type);
				inventories.remove(key);
			});
		}
		else {
			inventories.remove(key);
		}
	}
	public static void unloadInventory(UUID uuid, InventorySaveType type, boolean save) {
		InventoryKey key = new InventoryKey(uuid, type);
		unloadInventory(key, save);
	}

	/**
	 * すべてのインベントリを保存
	 */
	public static void saveAllInventories() {

		if(inventories.isEmpty()) return;

		// 全データを保存
		Set<InventoryKey> keySet = new HashSet<>(inventories.keySet());
		for(InventoryKey key : keySet) {
			if(!saveInventory(key.uuid, key.type)) return;

			// オフラインの場合はアンロード
			if(!Bukkit.getOfflinePlayer(key.uuid).isOnline()) {
				inventories.remove(key);
			}
		}
	}

	/**
	 * テーブル作成
	 */
	public static boolean createTable() {

		// オープン
		try(Connection con = Re_Storage.dataSource.getConnection()) {

			// SQL文作成
			StringBuilder sql = new StringBuilder();

			// SQL文作成
			sql.append(" CREATE TABLE IF NOT EXISTS PlayerInventory ( ");
			sql.append("     owner    varchar(40)  NOT NULL           ");
			sql.append("   , type     smallint     NOT NULL           ");
			sql.append("   , slot     smallint     NOT NULL           ");
			sql.append("   , material varchar(40)                     ");
			sql.append("   , amount   smallint                        ");
			sql.append("   , nbt      text                            ");
			sql.append("   , PRIMARY KEY(owner, type, slot)           ");
			sql.append(" )                                            ");
			PreparedStatement stmt = con.prepareStatement(sql.toString());

			// SQL文実行
			stmt.executeUpdate();
			stmt.close();

			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * インベントリの存在チェック
	 * @param uuid 対象プレイヤー
	 * @param type インベントリタイプ
	 */
	public static boolean checkExists(UUID uuid, InventorySaveType type) {

		// UUIDを文字列に変換
		String owner = uuid.toString();

		// オープン
		try(Connection con = Re_Storage.dataSource.getConnection()) {

			// SQL文作成
			StringBuilder sql = new StringBuilder();
			sql.append(" SELECT 1               ");
			sql.append(" FROM   PlayerInventory ");
			sql.append(" WHERE  owner = ?       ");
			sql.append(" AND    type  = ?       ");
			PreparedStatement stmt = con.prepareStatement(sql.toString());

			// 条件セット
			stmt.setString(1, owner);
			stmt.setShort(2, (short)type.ordinal());

			// SQL文実行
			ResultSet result = stmt.executeQuery();

			// データ読み込み
			boolean exists = false;
			if(result.next()) {
				exists = true;
			}

			// クローズ
			result.close();
			stmt.close();

			return exists;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * インベントリを読み込み
	 * @param uuid 所有者
	 * @param type インベントリタイプ
	 */
	public static void loadInventory(UUID uuid, InventorySaveType type) {

		// UUIDを文字列に変換
		String owner = uuid.toString();

		// オープン
		try(Connection con = Re_Storage.dataSource.getConnection()) {

			// SQL文作成
			StringBuilder sql = new StringBuilder();
			sql.append(" SELECT slot            ");
			sql.append("      , nbt             ");
			sql.append(" FROM   PlayerInventory ");
			sql.append(" WHERE  owner = ?       ");
			sql.append(" AND    type  = ?       ");
			sql.append(" ORDER BY slot          ");
			PreparedStatement stmt = con.prepareStatement(sql.toString());

			// 条件セット
			stmt.setString(1, owner);
			stmt.setShort(2, (short)type.ordinal());

			// SQL文実行
			ResultSet resultBody = stmt.executeQuery();

			Map<Short, ItemStack> items = new HashMap<>();

			// データ読み込み
			short slot;
			String nbt;
			while(resultBody.next()) {
				slot = resultBody.getShort("slot");
				nbt = resultBody.getString("nbt");

				// NBTからアイテムを作成
				ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT(nbt));

				// アイテムセット
				items.put(slot, item);
			}

			// クローズ
			resultBody.close();
			stmt.close();

			// アイテムをメモリに保存
			setInventory(uuid, type, items);

			// イベント呼び出し
//			Bukkit.getPluginManager().callEvent(new InventoryLoadEvent(uuid, type));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * アイテムを取得する
	 * @param player 対象プレイヤー
	 * @param type インベントリタイプ
	 */
	public static Map<Short, ItemStack> getItems(Player player, InventorySaveType type) {
		InventoryKey key = new InventoryKey(player.getUniqueId(), type);
		return inventories.get(key);
	}

	/**
	 * アイテムをセットする
	 * @param player 対象プレイヤー
	 * @param type インベントリタイプ
	 * @param slot スロット番号
	 * @param item アイテム
	 */
	public static void setItem(Player player, InventorySaveType type, short slot, ItemStack item) {
		Map<Short, ItemStack> items = getInventory(player.getUniqueId(), type);
		items.put(slot, item);
	}

	/**
	 * 指定したインベントリの全アイテムを保存する
	 * @param player 対象プレイヤー
	 * @param type インベントリタイプ
	 * @param inv インベントリ
	 */
	public static void fromInventory(Player player, InventorySaveType type, Inventory inv) {
		Map<Short, ItemStack> items = getInventory(player.getUniqueId(), type);
		items.clear();

		ItemStack[] contents = inv.getContents();
		for(short i = 0; i < contents.length; i++) {
			if(contents[i] == null) continue;
			items.put(i, contents[i]);
		}

		setInventory(player.getUniqueId(), type, items);
	}

	/**
	 * インベントリを保存
	 * @param uuid 所有者
	 * @param type インベントリタイプ
	 * @return 成否
	 */
	public static boolean saveInventory(UUID uuid, InventorySaveType type) {

		Connection con = null;
		try {
			// インベントリが存在しなければ保存しない
			InventoryKey key = new InventoryKey(uuid, type);
			if(!inventories.containsKey(key)) return false;

			// UUIDを文字列化
			String owner = uuid.toString();

			// オープン
			con = Re_Storage.dataSource.getConnection();

			// トランザクション開始
			con.setAutoCommit(false);

			Map<Short, ItemStack> items = inventories.get(key);
			if( items == null ) {
				con.rollback();
				return false;
			}

			// EC削除
			if( !deleteInventory(con, key) ) {
				con.rollback();
				return false;
			}

			// SQL文作成
			StringBuilder sqlBody = new StringBuilder();
			sqlBody.append(" INSERT INTO PlayerInventory ( ");
			sqlBody.append("         owner                 ");
			sqlBody.append("       , type                  ");
			sqlBody.append("       , slot                  ");
			sqlBody.append("       , material              ");
			sqlBody.append("       , amount                ");
			sqlBody.append("       , nbt                   ");
			sqlBody.append(" ) VALUES (                    ");
			sqlBody.append("        ?                      ");
			sqlBody.append("      , ?                      ");
			sqlBody.append("      , ?                      ");
			sqlBody.append("      , ?                      ");
			sqlBody.append("      , ?                      ");
			sqlBody.append("      , ?                      ");
			sqlBody.append(" )                             ");
			PreparedStatement stmt = con.prepareStatement(sqlBody.toString());

			// データ登録
			for(short slot : items.keySet()) {

				// アイテムを得る
				ItemStack item = items.get(slot);
				if(item == null || item.getType() == Material.AIR) continue;

				// 条件セット
				stmt.setString(1, owner);
				stmt.setShort(2, (short)type.ordinal());
				stmt.setShort(3, slot);
				stmt.setString(4, item.getType().name());
				stmt.setShort(5, (short)item.getAmount());
				stmt.setString(6, NBT.itemStackToNBT(item).toString());

				// SQL文実行
				stmt.addBatch();
			}

			// SQL文実行
			stmt.executeLargeBatch();

			// クローズ
			stmt.close();

			// 登録完了
			con.commit();

			// イベント呼び出し
//			Bukkit.getPluginManager().callEvent(new InventorySaveEvent(uuid, type));

			return true;
		}
		catch (SQLException e) {
			Re_Storage.getInstance().getLogger().log(Level.WARNING, "", e);

			try {
				// ロールバック
				if(con != null)	con.rollback();
			}
			catch (SQLException e2) {
				e2.printStackTrace();
			}
			return false;
		}
		finally {
			try {
				// トランザクション終了
				if (con != null) {
					con.setAutoCommit(true);
					con.close();
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * エンダーチェストを削除
	 * @param con DB接続
	 * @param key キー
	 */
	private static boolean deleteInventory(Connection con, InventoryKey key) {

		try {

			// SQL文作成
			StringBuilder sql = new StringBuilder();
			sql.append(" DELETE FROM PlayerInventory ");
			sql.append(" WHERE  owner = ?            ");
			sql.append(" AND    type  = ?            ");
			PreparedStatement stmt = con.prepareStatement(sql.toString());

			// 条件セット
			stmt.setString(1, key.uuid.toString());
			stmt.setShort(2, (short)key.type.ordinal());

			// SQL文実行
			int count = stmt.executeUpdate();

			// クローズ
			stmt.close();
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static class InventoryKey {
		UUID uuid;
		InventorySaveType type;

		public InventoryKey(UUID uuid, InventorySaveType key) {
			this.uuid = uuid;
			this.type = key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof InventoryKey)) return false;
			InventoryKey that = (InventoryKey) o;
			return Objects.equals(uuid, that.uuid) &&
					type == that.type;
		}

		@Override
		public int hashCode() {
			return Objects.hash(uuid, type);
		}
	}
}
