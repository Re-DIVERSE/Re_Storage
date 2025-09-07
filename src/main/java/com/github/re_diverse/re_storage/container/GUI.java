package com.github.re_diverse.re_storage.container;

import com.github.re_diverse.re_storage.item.IDiverseItem;
import com.github.re_diverse.re_storage.item.ItemUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class GUI implements IDiverseItemContainer, InventoryHolder {

	// GUIのアイテム
	public Map<Integer, IDiverseItem<?>> items = new HashMap<>();

	// GUI外部クリック時の処理
	public Map<GUIClickActionType, IGUIAction> outsideActions = new HashMap<>();

	// インベントリ
	private final Inventory inventory;

	// アイテムを取り出せるか
	public boolean canPick = false;

	/**
	 * コンストラクタ
	 * @param row 行数
	 * @param title タイトル
	 */
	public GUI(int row, Component title) {
		inventory = Bukkit.createInventory(this, row, title);
	}

	/**
	 * GUI外部クリック時の動作を定義
	 * @param type 動作タイプ
	 * @param action 動作
	 */
	public void setOutsideAction(GUIClickActionType type, IGUIAction action) {
		outsideActions.put(type, action);
	}

	/**
	 * GUI外部クリック時の動作を発火
	 * @param type
	 */
	public void runOutsideAction(GUIClickActionType type) {
		if(!outsideActions.containsKey(type)) return;
		outsideActions.get(type).run();
	}

	/**
	 * インベントリを開く
	 * @param player 対象者
	 */
	public void show(Player player) {
		player.openInventory(inventory);
	}

	/**
	 * クリア
	 */
	public void clear() {
		inventory.clear();
		items.clear();
	}

	/**
	 * アイテムをセット
	 * @param index スロット番号
	 * @param item アイテム
	 */
	@Override
	public void setItem(int index, IDiverseItem item) {
		items.put(index, item);
		inventory.setItem(index, item.getItem());
	}

	/**
	 * アイテムを取得
	 * @param index スロット番号
	 * @return アイテム
	 */
	@Override
	public IDiverseItem<?> getItem(int index) {
		return items.get(index);
	}

	/**
	 * アイテムを取得
	 * @param index スロット番号
	 * @return アイテム
	 */
	public IDiverseItem<?> getInventoryItem(int index) {
		return ItemUtil.getItem(inventory.getItem(index));
	}

	/**
	 * インベントリを返すオーバーライド
	 * @return インベントリ
	 */
	@NotNull
	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
