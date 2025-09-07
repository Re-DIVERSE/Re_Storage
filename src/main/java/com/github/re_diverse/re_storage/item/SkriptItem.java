package com.github.re_diverse.re_storage.item;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Skriptで生成されたアイテム
 */
public class SkriptItem extends DiverseItemImpl{

	/**
	 * アイテムを作成
	 * @param item 基となるアイテム
	 * @return アイテム
	 */
	public static SkriptItem get(ItemStack item) {

		// チェック
		if(item == null || item.getType() == Material.AIR) return null;

		ItemId itemId;
		try {

			// NBTから変換
			NBTItem nbt = new NBTItem(item);
			NBTCompound comp = nbt.getCompound("PublicBukkitValues");
			int box = comp.getInteger("skript:item.box");
			int id = comp.getInteger("skript:item.id");

			itemId = new ItemId(box, id);
		}
		catch (Exception e) {
			itemId = new ItemId(0, 0);
		}

		// アイテムを作成
		SkriptItem divItem = new SkriptItem(itemId);
		divItem.setItem(item);

		// アイテムを生成して返す
		return divItem;
	}

	/**
	 * コンストラクタ
	 */
	public SkriptItem(ItemId itemId) {
		super(itemId);
	}
}
