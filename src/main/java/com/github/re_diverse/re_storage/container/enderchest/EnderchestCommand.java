package com.github.re_diverse.re_storage.container.enderchest;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnderchestCommand implements CommandExecutor {

	/**
	 * コマンド受け取り時
	 * @param sender 実行者
	 * @param command コマンド
	 * @param label ラベル
	 * @param args 引数
	 * @return 実行可否
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!(sender instanceof Player)) return false;

		// プレイヤーを得る
		Player player = (Player)sender;
		if(args.length <= 0) {
			if(!player.hasPermission("diverseitemmanager.enderchest.help")) {
				player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cコマンドを使用する権限がありません。"));
				return false;
			}
			sendHelp(player);
		}
		else {
			switch (args[0].toLowerCase()) {
				case "open":
					if(!player.hasPermission("diverseitemmanager.enderchest.open")) {
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cコマンドを使用する権限がありません。"));
						return false;
					}
					if(args.length != 2) {
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c引数が不正です。(/dec open <player>)"));
						return false;
					}

					// 対象者を得る
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

					// 他人のエンダーチェストを開く
					EnderchestManager.open(target, player);

					return true;

				default:
					sendHelp(player);
					break;
			}
		}

		return false;
	}

	private void sendHelp(Player player) {
		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a/dec open <player>"));
	}
}
