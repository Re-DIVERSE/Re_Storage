package com.github.re_diverse.re_storage.container.playerinventory;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InventorySaveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@NotNull
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() { return handlers; }

	public UUID uuid;
	public InventorySaveType type;

	/**
	 * コンストラクタ
	 * @param uuid 対象プレイヤー
	 * @param type インベントリタイプ
	 */
	public InventorySaveEvent(UUID uuid, InventorySaveType type) {
		this.uuid = uuid;
		this.type = type;
	}

}
