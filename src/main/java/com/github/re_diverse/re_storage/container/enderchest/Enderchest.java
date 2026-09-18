package com.github.re_diverse.re_storage.container.enderchest;

import com.github.re_diverse.re_storage.container.GUI;
import com.github.re_diverse.re_storage.container.GUIItem;
import com.github.re_diverse.re_storage.item.DiverseItemActionType;
import com.github.re_diverse.re_storage.item.IDiverseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enderchest extends GUI {

	// ページ総覧
	public Map<Integer, EnderchestPage> pages = new HashMap<>();

	// モード
	public ClickMode clickMode = ClickMode.OPEN_PAGE;

	// 処理対象ページ
	int procPage = -1;

	// 開いているプレイヤー
	Player opener = null;

	/**
	 * コンストラクタ
	 */
	public Enderchest() {
		super(Integer.max(((EnderchestManager.maxPage+1) / 9), 54), LegacyComponentSerializer.legacyAmpersand().deserialize("&2エンダーチェスト"));
	}

	/**
	 * エンダーチェストメニューを開く
	 * @param player 対象者
	 * @param page ページ番号
	 */
	public void show(Player player, int page) {
		if(pages.size() == 0) return;

		// 開いているプレイヤーをセット
		opener = player;

		// 既にインベントリを開いている場合
		Enderchest target = this;
		InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
		if(holder instanceof Enderchest) {
			target = (Enderchest)holder;
		}
		target.clear();

		// ページ選択GUIを作成する
		int slot = 0;
		int pageNum;
		if(page == 0) {
			pageNum = EnderchestPage.REWARD_PAGE;
		}
		else {
			pageNum = (int)Math.floor((double)page / 45.0d) * 45 - 1;
		}
		for(int i = 0; i < Integer.min(45, pages.size()+1); i++) {
			if(!pages.containsKey(pageNum)) {
				pageNum++;
				break;
			}

			// ボタン作成
			EnderchestPage ecPage = pages.get(pageNum);
			Map<Integer, IDiverseItem<?>> mapItem = ecPage.getItems();
			int size = mapItem.size();
			GUIItem<Integer> item;
			if (pageNum == EnderchestPage.REWARD_PAGE) {
				item = new GUIItem<>(Material.VAULT, ecPage.title);
			}
			else if(size >= (ecPage.row * 9)) {
				item = new GUIItem<>(Material.RED_STAINED_GLASS_PANE, ecPage.title);
			}
			else if(size >= ((double)(ecPage.row * 9) * 0.75d)) {
				item = new GUIItem<>(Material.YELLOW_STAINED_GLASS_PANE, ecPage.title);
			}
			else {
				item = new GUIItem<>(Material.LIME_STAINED_GLASS_PANE, ecPage.title);
			}
			item.setData(pageNum);

			// ボタンに最初の5アイテムを表示
			ItemMeta meta = item.getItem().getItemMeta();
			List<Component> lore = new ArrayList<>();
			for(Integer index : mapItem.keySet()) {
				Component name = mapItem.get(index).getItem().displayName();
				lore.add(name);
				if(lore.size() >= 5) break;
			}
			meta.lore(lore);
			item.getItem().setItemMeta(meta);

			// 動作を定義
			item.setAction(DiverseItemActionType.GUI_CLICK, (clickedItem) -> {

				switch (clickMode) {
					// ページオープンモード
					case OPEN_PAGE:
						if(clickedItem.getData() instanceof Integer) {

							// エンダーチェストを開く
							Integer selPage = (Integer)clickedItem.getData();
							if(!pages.containsKey(selPage)) return;
							procPage = selPage;
							player.closeInventory();
							pages.get(selPage).open(player);
						}
						break;

					// 名前変更モード
					case RENAME_PAGE:
						if(clickedItem.getData() instanceof Integer) {

							if((Integer)clickedItem.getData() == EnderchestPage.REWARD_PAGE) {
								player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c報酬専用ページは名前変更できません。"));
								break;
							}

							// 名前変更モードON
							Integer selPage = (Integer)clickedItem.getData();
							if(!pages.containsKey(selPage)) return;
							procPage = selPage;
							EnderchestListener.renamePages.put(player, this);
							player.closeInventory();
							player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aチャット欄に新しい名前を15文字以内で入力してください。"));
						}
						break;

					// ページ入れ替えモード
					case MOVE_PAGE:
						if(clickedItem.getData() instanceof Integer) {

							// 移動する場合
							if(procPage >= 0) {
								if((Integer)clickedItem.getData() == EnderchestPage.REWARD_PAGE) {
									player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c報酬専用ページは移動できません。"));
									break;
								}

								Integer pageToNum = (Integer)clickedItem.getData();
								if(!pages.containsKey(procPage) || !pages.containsKey(pageToNum)) return;

								EnderchestPage pageFrom = pages.get(procPage);
								EnderchestPage pageTo = pages.get(pageToNum);

								// 入れ替えを実行
								pages.put(procPage, pageTo);
								pages.put(pageToNum, pageFrom);

								player.closeInventory();
								show(player, procPage);

								// 処理終了
								procPage = -1;
							}
							else {

								if((Integer)clickedItem.getData() == EnderchestPage.REWARD_PAGE) {
									player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c報酬専用ページは移動できません。"));
									break;
								}
								// 処理対象をセット
								procPage = (Integer)clickedItem.getData();
							}
						}
						break;
				}
			});

			// GUIにアイテムセット
			target.setItem(slot++, item);

			pageNum++;
		}

		// 次のページへ
		if(pages.size() > pageNum) {
			GUIItem item = new GUIItem(Material.ENDER_EYE, "&r&6次のページ");

			// ページ移動の動作を定義
			item.setAction(DiverseItemActionType.GUI_CLICK, (clickedItem) -> {
				show(player, page + 45);
			});

			target.setItem(53, item);
		}

		// 前のページ
		if(page >= 45) {
			GUIItem item = new GUIItem(Material.ENDER_PEARL, "&r&2前のページ");

			// ページ移動の動作を定義
			item.setAction(DiverseItemActionType.GUI_CLICK, (clickedItem) -> {
				show(player, page - 45);
			});

			target.setItem(45, item);
		}

		// 名前変更
		GUIItem<GUI> item;
		switch (clickMode) {
			case OPEN_PAGE:
			default:
				item = new GUIItem<>(Material.ENDER_CHEST, "&aエンダーチェストモード");
				break;

			case RENAME_PAGE:
				item = new GUIItem<>(Material.NAME_TAG, "&2名前変更モード");
				break;

			case MOVE_PAGE:
				item = new GUIItem<>(Material.CHORUS_FRUIT, "&2ページ入れ替えモード");
				break;
		}
		item.setData(target);

		// 名前変更の動作を定義
		item.setAction(DiverseItemActionType.GUI_CLICK, (clickedItem) -> {

			GUI gui = (GUI)clickedItem.getData();

			String name;
			switch (clickMode) {
				case OPEN_PAGE:
					clickMode = ClickMode.RENAME_PAGE;
					clickedItem.getItem().setType(Material.NAME_TAG);
					name = "&2名前変更モード";
					break;

				case RENAME_PAGE:
					clickMode = ClickMode.MOVE_PAGE;
					clickedItem.getItem().setType(Material.CHORUS_FRUIT);
					name = "&2ページ入れ替えモード";
					break;

				case MOVE_PAGE:
				default:
					clickMode = ClickMode.OPEN_PAGE;
					clickedItem.getItem().setType(Material.ENDER_CHEST);
					name = "&2エンダーチェストモード";
					break;
			}

			// アイテムセット
			ItemMeta meta = clickedItem.getItem().getItemMeta();
			meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
			clickedItem.getItem().setItemMeta(meta);

			gui.setItem(49, clickedItem);

			// 処理対象をリセット
			procPage = -1;
		});

		target.setItem(49, item);

		// GUIを開く
		target.show(player);
	}

	/**
	 * ページ移動
	 * @param diff 移動量
	 */
	public void move(int diff) {
		int next = procPage + diff;
		if(opener == null) return;
		if(!pages.containsKey(next)) return;

		// 移動先のページを得る
		EnderchestPage page = pages.get(next);

		// 移動先のページを開く
		page.show(opener);

		// 移動先のページを処理対象にセット
		procPage = next;
	}

	/**
	 * ページ追加
	 * @param page ページ番号
	 * @param row 行数
	 * @param title GUIタイトル
	 */
	public void addPage(int page, short row, Component title) {
		EnderchestPage ecPage = new EnderchestPage(this, row, title);
		pages.put(page, ecPage);
	}

	/**
	 * ページをセット
	 * @param ecPage ページクラス
	 */
	public void setPage(int page, EnderchestPage ecPage) {
		pages.put(page, ecPage);
	}

	/**
	 * ページを取得
	 * @param page ページ番号
	 * @return ページ
	 */
	public EnderchestPage getPage(int page) {
		return pages.get(page);
	}

	/**
	 * 指定ページが作成されているか？
	 * @param page ページ番号
	 * @return 作成状態
	 */
	public boolean hasPage(int page) {
		return pages.containsKey(page);
	}

	/**
	 * ページ数を返す
	 * @return ページ数
	 */
	public int getPageCount() {
		return pages.size();
	}

	/**
	 * クリック時のモード
	 */
	enum ClickMode {
		OPEN_PAGE,
		RENAME_PAGE,
		MOVE_PAGE,
	}
}
