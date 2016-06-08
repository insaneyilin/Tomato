package com.tomato;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PetStatusDB extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "pet_status";
	public static final String DATE = "_date";
	public static final String PET_NAME = "name";
	public static final String LEVEL = "level";
	public static final String HP = "hp";
	public static final String XP = "xp";
	public static final String GOLD = "gold";
	public static final String STAGE = "stage";
	public static final String ITEM_LV = "item_level";
	public static final String USE_TIME = "use_time";
	
	private static PetStatusDB mInstance;
	
	// 单例模式获取唯一数据库实例
	public static synchronized PetStatusDB getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PetStatusDB(context.getApplicationContext());
		}
		return mInstance;
	}
	
	private PetStatusDB(Context context) {
		super(context, "pet_status", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" 
			+ DATE + " TEXT PRIMARY KEY NOT NULL, "
			+ PET_NAME + " TEXT NOT NULL, "
			+ LEVEL + " INTEGER NOT NULL, "
			+ HP + " INTEGER NOT NULL, "
			+ XP + " INTEGER NOT NULL, "
			+ GOLD + " INTEGER NOT NULL, "
			+ STAGE + " TEXT NOT NULL, "
			+ ITEM_LV + " INTEGER NOT NULL, "
			+ USE_TIME + " INTEGER NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}

