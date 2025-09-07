package com.github.re_diverse.re_storage.item;

import com.github.re_diverse.re_storage.Re_Storage;
import com.github.re_diverse.re_storage.ItemMode;
import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

	/**
	 * モード別のアイテムを取得する
	 * @param item 基のアイテム
	 * @return アイテム
	 */
	public static IDiverseItem<NBTCompound> getItem(ItemStack item) {
		if(Re_Storage.getItemMode() == ItemMode.DIVERSE_SKRIPT) {
			return SkriptItem.get(item);
		}
		return null;
	}
}
