package com.github.re_diverse.re_storage.util;

import com.github.re_diverse.re_storage.Re_Storage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Persistentを取得、セット、除去する
 * @author AburaAgeTarou
 */
public class ItemPersistentUtils {

	public static final String PERSISTENT_KEY_ID = "item.id";

	// 整数の値を返す
	public static @NotNull Long getLong(@Nonnull ItemStack itemStack, @Nonnull String keyName){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		Long value = itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.LONG);
		if(value == null) return 0L;
		return value;
	}

	// 整数の値をセット
	public static void setLong(@Nonnull ItemStack itemStack, @Nonnull String keyName, long value){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, value);
		itemStack.setItemMeta(meta);
	}

	// 文字列の値を返す
	public static @NotNull String getString(@Nonnull ItemStack itemStack, @Nonnull String keyName){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		String value = itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if(value == null) return "";
		return value;
	}

	// 文字列の値をセット
	public static void setString(@Nonnull ItemStack itemStack, @Nonnull String keyName, @Nonnull String value){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
		itemStack.setItemMeta(meta);
	}

	// 浮動小数点数の値を返す
	@Nullable
	public static Double getDouble(@Nonnull ItemStack itemStack, @Nonnull String keyName){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		return itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
	}

	// 浮動小数点数の値をセット
	public static void setDouble(@Nonnull ItemStack itemStack, @Nonnull String keyName, double value){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
		itemStack.setItemMeta(meta);
	}

	// 真偽値の値を返す
	public static Boolean getBoolean(@Nonnull ItemStack itemStack, @Nonnull String keyName){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		return (itemStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.SHORT) != null);
	}

	// 真偽値の値をセット
	public static void setBoolean(@Nonnull ItemStack itemStack, @Nonnull String keyName, boolean value){
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		itemStack.getItemMeta().getPersistentDataContainer().set(key, PersistentDataType.SHORT, (short)(value ? 1 : 0));
	}

	// 除去
	public static void remove(@Nonnull ItemStack itemStack, @Nonnull String keyName) {
		NamespacedKey key = new NamespacedKey(Re_Storage.getInstance(), keyName);
		itemStack.getItemMeta().getPersistentDataContainer().remove(key);
	}
}
