package com.github.re_diverse.re_storage.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * プラグイン有効化イベント
 */
public class PluginEnableEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@NotNull
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() { return handlers; }
}
