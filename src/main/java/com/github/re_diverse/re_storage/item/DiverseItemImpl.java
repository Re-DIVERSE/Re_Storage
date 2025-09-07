package com.github.re_diverse.re_storage.item;

import com.github.re_diverse.re_storage.Re_Storage;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * DIVERSEで使用するアイテム
 */
public abstract class DiverseItemImpl implements IDiverseItem<NBTCompound>{

	// アイテムの実体
	private ItemStack item;

	// 動作
	public Map<DiverseItemActionType, IDiverseItemAction> actionMap = new HashMap<>();

	// アイテムデータ
	public final ItemId itemId;
	private NBTCompound data;

	/**
	 * コンストラクタ
	 * @param itemId アイテムID
	 */
	public DiverseItemImpl(ItemId itemId) {
		this.itemId = itemId;
	}

	/**
	 * アイテムの実体を取得
	 * @return アイテムの実体
	 */
	@Override
	public ItemStack getItem() {

		// アイテムが存在しない場合、登録したデータから取得する
		if(item == null) {
			return Re_Storage.getItem(itemId);
		}
		return item;
	}

	/**
	 * 動作を得る
	 * @param type 動作タイプ
	 * @return 動作
	 */
	@Override
	public IDiverseItemAction getAction(DiverseItemActionType type) {
		return actionMap.get(type);
	}

	/**
	 * アイテムを変換
	 * @param item アイテム
	 */
	public void setItem(ItemStack item) {
		this.item = item;
	}

	/**
	 * カスタムモデルデータを設定する
	 * @param customModelData カスタムモデルデータ
	 */
	public void setCustomModelData(int customModelData) {
		// データセット
		if(item != null) {
			ItemMeta meta = item.getItemMeta();
			meta.setCustomModelData(customModelData);
			item.setItemMeta(meta);
		}
	}

	/**
	 * アイテムデータをマージ
	 * @param data データ
	 */
	public void mergeData(String data) {
		setData(new NBTContainer(data));
	}

	/**
	 * NBTデータを得る
	 * @return データ
	 */
	@Override
	public NBTCompound getData() {
		return data;
	}

	/**
	 * アイテムデータをセット
	 * @param data データ
	 */
	@Override
	public void setData(NBTCompound data) {
		this.data = data;
		NBTItem nbtItem = new NBTItem(item);
		nbtItem.mergeCompound(data);
		item = nbtItem.getItem();
	}
}
