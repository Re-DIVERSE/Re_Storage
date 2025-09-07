package com.github.re_diverse.re_storage;

import com.github.re_diverse.re_storage.container.GUIListener;
import com.github.re_diverse.re_storage.container.enderchest.EnderchestCommand;
import com.github.re_diverse.re_storage.container.enderchest.EnderchestListener;
import com.github.re_diverse.re_storage.container.enderchest.EnderchestManager;
import com.github.re_diverse.re_storage.container.playerinventory.PlayerInventoryManager;
import com.github.re_diverse.re_storage.item.ItemId;
import com.github.re_diverse.re_storage.util.PluginEnableEvent;
import com.github.re_diverse.re_storage.util.Utilities;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Re_Storage extends JavaPlugin {

	// プラグイン インスタンス
	private static Re_Storage instance;

	// データソース
	public static HikariDataSource dataSource;

	// アイテム
	private static Map<ItemId, ItemStack> items;

	private static ItemMode itemMode;

	private static boolean enderChest = false;

	@Override
	public void onEnable() {

		// プラグイン インスタンスをセット
		instance = this;

		// 存在しない場合、デフォルトの設定ファイルを作成
		saveDefaultConfig();

		// コンフィグを再読み込み
		reloadConfig();

		// 設定を読み込み
		loadConfig();

		// イベント登録
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
		if(enderChest) Bukkit.getPluginManager().registerEvents(new EnderchestListener(), this);

		// コマンド登録
		getCommand("re_storage").setExecutor(new EnderchestCommand());

		// アイテムデータを読み込み
		//loadAllItem();

		// 接続
		connect();

		// 10分おきにDBに保存を行う
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, EnderchestManager::saveEnderchestAll, 30000L, 30000L);

		// エンダーチェスト読み込み
		if(enderChest) {
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
				for(Player player : Bukkit.getOnlinePlayers()) {
					EnderchestManager.loadEnderchest(player.getUniqueId());
				}
			});
		}

		// プラグイン有効化
		Bukkit.getPluginManager().callEvent(new PluginEnableEvent());
	}

	@Override
	public void onDisable() {

		// 保存
		EnderchestManager.saveEnderchestAll();
		PlayerInventoryManager.saveAllInventories();

		// 解放
		dataSource.close();
	}

	/**
	 * プラグイン インスタンスを取得
	 * @return インスタンス
	 */
	public static Re_Storage getInstance() {
		return instance;
	}

	/**
	 * 設定を読み込み
	 */
	public void loadConfig() {

		// 初期化
		if(EnderchestManager.nbtFilter == null) {
			EnderchestManager.nbtFilter = new ArrayList<>();
		}
		else {
			EnderchestManager.nbtFilter.clear();
		}

		// フィルターをセット
		EnderchestManager.nbtFilter.addAll(getConfig().getStringList("enderchest.nbt_filter"));

		// 最大ページ数をセット
		EnderchestManager.maxPage = getConfig().getInt("enderchest.max_page", 54);

		// 行数をセット
		EnderchestManager.defaultRow = (short)getConfig().getInt("enderchest.default_page", 6);

		// タイトル初期値をセット
		EnderchestManager.defaultTitle = Component.text(getConfig().getString("enderchest.default_title", "Enderchest"));

		// アイテムモードをセット
		itemMode = ItemMode.valueOf(getConfig().getString("item.item_mode", ItemMode.DIM.toString()));

		enderChest = getConfig().getBoolean("enderchest.enable", true);
	}

	/**
	 * コンフィグを読み込んでDBに接続
	 */
	public void connect() {

		// 設定内容を取得
		String address  = getConfig().getString("server.address");
		int    port     = getConfig().getInt("server.port");
		String user     = getConfig().getString("server.user");
		String pass     = getConfig().getString("server.password");
		String schema   = getConfig().getString("server.schema");
		String driver   = getConfig().getString("database.class_name");
		int    lifespan = getConfig().getInt("database.lifespan");

		// Nullチェック
		if(!Utilities.strNullCheck(address, user, pass, schema, driver)){
			throw new IllegalStateException("いずれかの設定が正しくありません。");
		}

		// ポートチェック
		if(!Utilities.portCheck(port)){
			throw new IllegalStateException("ポート番号の設定が正しくありません。");
		}

		// ライフスパンチェック
		if(lifespan == 0){
			throw new IllegalStateException("接続維持時間の設定が正しくありません。");
		}

		// データソースに接続
		dataSource = new HikariDataSource();

		// JDBCのURLを設定
		dataSource.setDriverClassName(driver);
		String url = String.format(
				"jdbc:postgresql://%s:%d/%s?user=%s&password=%s&useSSL=false",
				address,
				port,
				schema,
				user,
				pass
		);
		dataSource.setJdbcUrl(url);

		// ライフスパン
		if(lifespan > 0) {
			dataSource.setMaxLifetime(TimeUnit.MINUTES.toMillis(lifespan));
		}

		// テーブル作成
		if( !EnderchestManager.createTable() ) {
			throw new IllegalStateException("テーブルの作成に失敗しました。");
		}
		if( !PlayerInventoryManager.createTable() ) {
			throw new IllegalStateException("テーブルの作成に失敗しました。");
		}
	}

	/**
	 * アイテムを読み込む
	 */
	public void loadAllItem() {

		// コンフィグファイルを生成する
		File configFile = new File(getDataFolder(), "items.yml");
		if(!configFile.exists()) {
			try {
				if( !configFile.createNewFile() ) {
					throw new IllegalStateException("アイテム設定ファイルが作成できませんでした。");
				}
				return;
			}
			catch (IOException e) {
				getLogger().warning("アイテム設定ファイルが作成できませんでした。");
				e.printStackTrace();
			}
		}

		// YAMLコンフィグクラスを得る
		YamlConfiguration itemConfig = YamlConfiguration.loadConfiguration(configFile);

		// 設定がない場合は処理しない
		if(itemConfig.getConfigurationSection("items") == null) return;

		// アイテム設定を読み込む
		items = new HashMap<>();
		for(String boxKey : itemConfig.getConfigurationSection("items").getKeys(false)) {
			int box = Integer.parseInt(boxKey);
			for(String idKey : itemConfig.getConfigurationSection("items." + boxKey).getKeys(false)) {
				int id = Integer.parseInt(idKey);
				ItemId itemId = new ItemId(box, id);

				// アイテム設定
				String key = "items." + boxKey + "." + idKey;
				Material mat = Material.valueOf(itemConfig.getString(key + "." + "Material"));
				ItemStack item = new ItemStack(mat);

				items.put(itemId, item);
			}
		}
	}

	/**
	 * アイテムを得る
	 * @param key アイテムID
	 * @return アイテム
	 */
	public static ItemStack getItem(ItemId key) {
		return items.get(key);
	}

	/**
	 * アイテムモードを得る
	 * @return アイテムモード
	 */
	public static ItemMode getItemMode() {
		return itemMode;
	}
}
