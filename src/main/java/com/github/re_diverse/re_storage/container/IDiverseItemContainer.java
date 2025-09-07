package com.github.re_diverse.re_storage.container;

import com.github.re_diverse.re_storage.item.IDiverseItem;

/**
 * アイテム保管インターフェース
 */
public interface IDiverseItemContainer {

	/**
	 * アイテム設定
	 * @param index スロット番号
	 * @param item アイテム
	 */
	void setItem(int index, IDiverseItem item);

	/**
	 * アイテム取得
	 * @param index スロット番号
	 * @return アイテム
	 */
	IDiverseItem getItem(int index);
}
