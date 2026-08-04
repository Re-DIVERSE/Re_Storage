package com.github.re_diverse.re_storage.container.enderchest;

import com.github.re_diverse.re_storage.container.GUI;
import com.github.re_diverse.re_storage.container.GUIClickActionType;
import com.github.re_diverse.re_storage.container.IDiverseItemContainer;
import com.github.re_diverse.re_storage.item.IDiverseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class EnderchestPage extends GUI implements IDiverseItemContainer {

	public short row;
	public Component title;

	// エンダーチェストのクラス
	Enderchest chest;

	// ロックモード
	private boolean lock = false;

	// 報酬受け取り専用ページ
	static final int REWARD_PAGE = -1;

	/**
	 * エンダーチェストのページを開いているかどうかを返す
	 * @param player プレイヤー
	 * @return エンダーチェストのページを開いているか
	 */
	public static boolean isOpen(Player player) {
		if(player == null) return false;
		InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
		return holder instanceof EnderchestPage;
	}

	/**
	 * ページに新しい名前をつけてコピーする
	 * @param oldPage コピー元
	 * @param title 名前
	 * @return 新しいページ
	 */
	public static EnderchestPage pageClone(Enderchest chest, EnderchestPage oldPage, Component title) {
		EnderchestPage newPage = new EnderchestPage(chest, oldPage.row, title);
		Map<Integer, IDiverseItem<?>> items = oldPage.getItems();
		for(Integer slot : items.keySet()) {
			newPage.setItem(slot, items.get(slot));
		}
		return newPage;
	}

	/**
	 * コンストラクタ
	 * @param row 行数
	 */
	public EnderchestPage(Enderchest chest, short row, Component title) {
		super(row * 9, title);
		this.row = row;
		this.title = title;
		this.chest = chest;
		this.canPick = true;

		// GUI外左クリック時、前のページに移動する
		setOutsideAction(GUIClickActionType.GUI_OUTSIDE_LEFT_CLICK, () -> chest.move(-1));

		// GUI外右クリック時、次のページに移動する
		setOutsideAction(GUIClickActionType.GUI_OUTSIDE_RIGHT_CLICK, () -> chest.move(1));

		// GUI外右クリック時、次のページに移動する
		setOutsideAction(GUIClickActionType.GUI_OUTSIDE_MIDDLE_CLICK, () -> chest.show(chest.opener, chest.procPage));
	}

	/**
	 * インベントリのアイテムを返す
	 * @return スロット：アイテムのマップ
	 */
	public Map<Integer, IDiverseItem<?>> getItems() {

		// インベントリをすべて読み込み
		Map<Integer, IDiverseItem<?>> items = new HashMap<>();
		for(int i = 0; i < (row * 9); i++) {
			IDiverseItem<?> item = getInventoryItem(i);
			if(item != null) items.put(i, item);
		}
		return items;
	}

	/**
	 * ページを開く
	 * @param player 対象者
	 */
	public void open(Player player) {

		// ロック中は開かせない
		if(lock) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c現在そのページを開くことはできません。"));
			return;
		}

		// インベントリを開く
		show(player);

		// ロック
		lock = true;
	}

	/**
	 * ロック解除
	 */
	public void unlock() {
		lock = false;
	}
}
