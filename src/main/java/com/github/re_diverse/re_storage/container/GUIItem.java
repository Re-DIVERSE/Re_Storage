package com.github.re_diverse.re_storage.container;

import com.github.re_diverse.re_storage.item.DiverseItemActionType;
import com.github.re_diverse.re_storage.item.IDiverseItem;
import com.github.re_diverse.re_storage.item.IDiverseItemAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class GUIItem<T> implements IDiverseItem<T> {

	ItemStack item;
	Map<DiverseItemActionType, IDiverseItemAction> actionMap = new HashMap<>();
	T data;

	/**
	 * コンストラクタ
	 * @param mat アイテムの{@link Material}
	 */
	public GUIItem(Material mat, String name) {
		this.item = new ItemStack(mat);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
		item.setItemMeta(itemMeta);
	}

	/**
	 * コンストラクタ
	 * @param mat アイテムの{@link Material}
	 */
	public GUIItem(Material mat, Component name) {
		this.item = new ItemStack(mat);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.displayName(name);
		item.setItemMeta(itemMeta);
	}

	/**
	 * コンストラクタ
	 * @param mat アイテムの{@link Material}
	 * @param customModelData カスタムモデルデータ
	 */
	public GUIItem(Material mat, int customModelData, Component name) {
		item = new ItemStack(mat);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setCustomModelData(customModelData);
		itemMeta.displayName(name);
		item.setItemMeta(itemMeta);
	}

	/**
	 * アイテムを取得する
	 * @return アイテム
	 */
	@Override
	public ItemStack getItem() {
		return item;
	}

	/**
	 * 動作を定義する
	 * @param type 動作タイプ
	 * @param action 動作
	 */
	public void setAction(DiverseItemActionType type, IDiverseItemAction action) {
		this.actionMap.put(type, action);
	}

	/**
	 * 動作を取得する
	 * @param type 動作タイプ
	 * @return 動作
	 */
	@Override
	public IDiverseItemAction getAction(DiverseItemActionType type) {
		return actionMap.get(type);
	}

	/**
	 * データをセットする
	 * @param data データ
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * データを取得する
	 * @return データ
	 */
	@Override
	public T getData() {
		return data;
	}
}
