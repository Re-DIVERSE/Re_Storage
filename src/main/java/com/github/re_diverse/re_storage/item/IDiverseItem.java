package com.github.re_diverse.re_storage.item;

import org.bukkit.inventory.ItemStack;

public interface IDiverseItem<T> {

	/**
	 * Bukkitアイテムを取得
	 * @return アイテム
	 */
	ItemStack getItem();

	/**
	 * アイテムの動作を取得
	 * @param type 動作タイプ
	 * @return 動作
	 */
	IDiverseItemAction getAction(DiverseItemActionType type);

	/**
	 * データをセットする
	 * @param data データ
	 */
	void setData(T data);

	/**
	 * データを返す
	 * @return データ
	 */
	T getData();
}

