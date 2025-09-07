package com.github.re_diverse.re_storage.util;

public class Utilities {

	/**
	 * 指定した文字列が空白であるかチェックする
	 * @param values チェックする文字列
	 * @return 値が存在する(Nullか空白)場合true, 存在しない場合false
	 */
	public static boolean strNullCheck(String... values){
		for(String str : values){

			// Nullチェック
			if(str == null) return false;

			// 空白チェック
			if(str.trim().equals("")) return false;
		}
		return true;
	}

	/**
	 * ポートが使用できるかチェックする
	 * @param port ポート番号
	 * @return ポートが正しい範囲内か
	 */
	public static boolean portCheck(int port) {

		// 範囲チェック
		if(port < 1) return false;
		if(port > 65535) return false;

		return true;
	}
}
