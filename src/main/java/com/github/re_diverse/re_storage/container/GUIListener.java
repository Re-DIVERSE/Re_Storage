package com.github.re_diverse.re_storage.container;

import com.github.re_diverse.re_storage.item.DiverseItemActionType;
import com.github.re_diverse.re_storage.item.IDiverseItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

	@EventHandler
	public void onGUIClick(InventoryClickEvent event) {

		// インベントリを取得
		Inventory inv = event.getClickedInventory();
		if(inv == null) {

			// GUIの外でクリックした場合
			Inventory topInv = event.getWhoClicked().getOpenInventory().getTopInventory();
			if(topInv.getHolder() == null) return;
			if(!(topInv.getHolder() instanceof GUI)) return;

			// 独自クラスにキャスト
			GUI gui = (GUI)topInv.getHolder();

			// イベントをキャンセル
			event.setCancelled(true);

			// クリックごとの動作を実行
			switch (event.getClick()) {
				case LEFT:
				case SHIFT_LEFT:
					gui.runOutsideAction(GUIClickActionType.GUI_OUTSIDE_LEFT_CLICK);
					break;

				case RIGHT:
				case SHIFT_RIGHT:
					gui.runOutsideAction(GUIClickActionType.GUI_OUTSIDE_RIGHT_CLICK);
					break;

				case MIDDLE:
					gui.runOutsideAction(GUIClickActionType.GUI_OUTSIDE_MIDDLE_CLICK);
					break;
			}
			return;
		}

		// 独自クラスのインベントリが開かれている場合
		InventoryHolder holder = event.getWhoClicked().getOpenInventory().getTopInventory().getHolder();
		if(holder instanceof GUI) {
			GUI gui = (GUI)holder;

			// イベントをキャンセル
			if(!gui.canPick) {
				event.setCancelled(true);
			}
		}

		// 独自クラスのインベントリかどうかチェック
		if(inv.getHolder() == null) return;
		if(!(inv.getHolder() instanceof GUI)) return;

		// 独自クラスにキャスト
		GUI gui = (GUI)inv.getHolder();

		// アイテムと動作が存在する場合
		IDiverseItem<?> item = gui.getItem(event.getSlot());
		if(item == null) return;
		if(item.getAction(DiverseItemActionType.GUI_CLICK) == null) return;

		// 発火
		item.getAction(DiverseItemActionType.GUI_CLICK).run(item);
	}
}
