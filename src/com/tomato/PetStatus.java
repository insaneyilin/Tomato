package com.tomato;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PetStatus {

	// 成长阶段
	public static final String[] STAGE_NAMES = {
		"EGG_1", "EGG_2", "CHILD", "YOUTH", "ADULT"
	};
	
	// 每个阶段的最低等级
	private static final int[] STAGE_LEVELS = {
		0, 1, 3, 6, 10
	};
	
	// 每个阶段的最大生命值
	public static final int[] STAGE_MAX_HPS = {
		2, 12, 48, 120, 240	
	};
	
	// 每个阶段饥饿时扣除的 HP 值
	public static final int[] STAGE_HUNGER_VALUES = {
		0, 1, 4, 10, 20
	};
	
	// 到达下一级所需经验值, 1 至 16 级；1 至 ·15 级有道具
	public static final int[] XP_TO_NEXT_LEVEL = {
		20, 30, 50, 50, 70, 70, 90, 110, 130, 150, 150, 170, 190, 210, 230, 250	
	};
	
	// 购买每一个道具所需的金币数
	public static final int[] GOLD_COST_OF_ITEMS = {
		80, 120, 200, 200, 280, 360, 360, 440, 520, 600, 600, 680, 760, 840, 920
	};
	
	// 完成不同专注时间(分钟)所能获取的 XP
	private static final HashMap<Integer, Integer> XP_FROM_TIME = new HashMap<Integer, Integer>();
	
	private String name;
	private int level;
	private int hp;
	private int xp;
	private int gold;
	private int use_time;
	private int stage_idx = 0;
	private int item_lv = 0;
	
	// ----------------------------------------------------
	
	public PetStatus() {
		this.name = "Tom";
		this.level = 0;
		this.hp = 0;
		this.xp = 0;
		this.gold = 0;
		this.use_time = 0;
		this.item_lv = 0;
		this.stage_idx = 0;
		
		initXP_FROM_TIME();
	}
	
	public PetStatus(String name, int lv, int hp, int xp, int gold, int time, String stage) {
		this.name = name;
		this.level = lv;
		this.hp = hp;
		this.xp = xp;
		this.gold = gold;
		this.use_time = time;
		this.stage_idx = 0;
		for (int i = 0; i < STAGE_NAMES.length; ++i) {
			if (STAGE_NAMES[i].equals(stage)) {
				stage_idx = i;
				break;
			}
		}
		
		initXP_FROM_TIME();
	}
	
	private static void initXP_FROM_TIME() {
		
		int[] time_minutes = {10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
		int[] xps = {5, 12, 18, 25, 30, 37, 43, 50, 57, 63, 70};
		
		for (int i = 0; i < time_minutes.length; ++i) {
			XP_FROM_TIME.put(time_minutes[i], xps[i]);
		}
	}
	
	// 从数据库中读取信息
	public boolean readFromDB(PetStatusDB psDB, Date curDate) {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String str_date = format.format(curDate);
		
		SQLiteDatabase dbReader = psDB.getReadableDatabase();
		
		Cursor cursor = dbReader.query(PetStatusDB.TABLE_NAME, 
				null, PetStatusDB.DATE + "=?", new String[] {str_date}, null, null, null);
		
		if (cursor != null && cursor.moveToFirst()) {
			
			this.name = cursor.getString(cursor.getColumnIndex(PetStatusDB.PET_NAME));
			this.level = cursor.getInt(cursor.getColumnIndex(PetStatusDB.LEVEL));
			this.hp = cursor.getInt(cursor.getColumnIndex(PetStatusDB.HP));
			this.xp = cursor.getInt(cursor.getColumnIndex(PetStatusDB.XP));
			this.gold = cursor.getInt(cursor.getColumnIndex(PetStatusDB.GOLD));
			this.item_lv = cursor.getInt(cursor.getColumnIndex(PetStatusDB.ITEM_LV));
			this.use_time = cursor.getInt(cursor.getColumnIndex(PetStatusDB.USE_TIME));
			
			String str_stage = cursor.getString(cursor.getColumnIndex(PetStatusDB.STAGE));
			this.stage_idx = 0;
			for (int i = 0; i < STAGE_NAMES.length; ++i) {
				if (STAGE_NAMES[i].equals(str_stage)) {
					this.stage_idx = i;
					break;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	// 向数据库中写入信息
	public boolean writeToDB(PetStatusDB psDB, Date curDate) {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String str_date = format.format(curDate);
		
		SQLiteDatabase dbWriter = psDB.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		
		cv.put(PetStatusDB.DATE, str_date);
		cv.put(PetStatusDB.PET_NAME, this.name);
		cv.put(PetStatusDB.LEVEL, this.level);
		cv.put(PetStatusDB.HP, this.hp);
		cv.put(PetStatusDB.XP, this.xp);
		cv.put(PetStatusDB.GOLD, this.gold);
		cv.put(PetStatusDB.ITEM_LV, this.item_lv);
		cv.put(PetStatusDB.USE_TIME, this.use_time);
		cv.put(PetStatusDB.STAGE, STAGE_NAMES[this.stage_idx]);
		
		if (dbWriter.insert(PetStatusDB.TABLE_NAME, null, cv) != -1) {
			return true;
		}
		
		return false;
	}
	
	// 更新数据
	public boolean updateDB(PetStatusDB psDB, Date curDate) {
	
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String str_date = format.format(curDate);
		
		SQLiteDatabase dbWriter = psDB.getReadableDatabase();
		
		ContentValues cv = new ContentValues();
		
		cv.put(PetStatusDB.DATE, str_date);
		cv.put(PetStatusDB.PET_NAME, this.name);
		cv.put(PetStatusDB.LEVEL, this.level);
		cv.put(PetStatusDB.HP, this.hp);
		cv.put(PetStatusDB.XP, this.xp);
		cv.put(PetStatusDB.GOLD, this.gold);
		cv.put(PetStatusDB.ITEM_LV, this.item_lv);
		cv.put(PetStatusDB.USE_TIME, this.use_time);
		cv.put(PetStatusDB.STAGE, STAGE_NAMES[this.stage_idx]);
		
		String[] args = {str_date};
		if (dbWriter.update(PetStatusDB.TABLE_NAME, cv, PetStatusDB.DATE + "=?", args) != -1) {
			return true;
		}
		
		return false;
	}
	
	// ----------------------------------------------------
	// 宠物行为
	
	// 根据专注时间（分钟）获取经验
	public void gainXP(int time) {
		// time 为分钟数
		if (XP_FROM_TIME.containsKey(time)) {
			this.xp += XP_FROM_TIME.get(time);
		}
	}
	
	public boolean levelUp() {
		if (xp >= XP_TO_NEXT_LEVEL[level]) {
			if (item_lv == level + 1) {
				xp -= XP_TO_NEXT_LEVEL[level];
				++level;
				hp = STAGE_MAX_HPS[stage_idx];
				return true;
			} else {
				xp = XP_TO_NEXT_LEVEL[level];
			}
		}
		return false;
	}
	
	public boolean evovle() {
		for (int i = 0; i < STAGE_LEVELS.length; ++i) {
			if (level == STAGE_LEVELS[i] && stage_idx == i-1) {
				stage_idx = i;
				hp = STAGE_MAX_HPS[stage_idx];
				return true;
			}
		}
		return false;
	}
	
	public void gainGold() {
		gold += 1;
	}
	
	public void hunger() {
		if (stage_idx > 0) {
			hp -= STAGE_HUNGER_VALUES[stage_idx];
			if (hp <= 0) {
				die();
			}
		}
	}
	
	public void die() {
		this.level = 0;
		this.hp = 0;
		this.xp = 0;
		this.gold = 0;
		this.use_time = 0;
		this.item_lv = 0;
		this.stage_idx = 0;
	}
	
	public boolean buyItem(int item_idx) {
		if (item_idx < 0 || item_idx >= GOLD_COST_OF_ITEMS.length)
			return false;
		
		if (level >= item_idx) {
			
			if (gold < GOLD_COST_OF_ITEMS[item_idx])
				return false;
			
			gold -= GOLD_COST_OF_ITEMS[item_idx];  // 扣除相应金币
			if (item_lv <= item_idx)  // 道具等级增加
				item_lv = item_idx + 1;
			
			if (item_idx == 0) {
				hp += 1;
				if (hp > STAGE_MAX_HPS[stage_idx])
					hp = STAGE_MAX_HPS[stage_idx];
			} else if (item_idx == 1) {
				hp += 12;
				if (hp > STAGE_MAX_HPS[stage_idx])
					hp = STAGE_MAX_HPS[stage_idx];
			} else if (item_idx == 3) {
				hp += 48;
				if (hp > STAGE_MAX_HPS[stage_idx])
					hp = STAGE_MAX_HPS[stage_idx];
			} else if (item_idx == 6) {
				hp += 120;
				if (hp > STAGE_MAX_HPS[stage_idx])
					hp = STAGE_MAX_HPS[stage_idx];
			} else if (item_idx == 10) {
				hp += 240;
				if (hp > STAGE_MAX_HPS[stage_idx])
					hp = STAGE_MAX_HPS[stage_idx];
			}
			
			return true;
		}
		
		return false;
	}
	
	// ----------------------------------------------------
	// Getters and Setters
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int lv) {
		this.level = lv;
	}
	
	public int getHP()
	{
		return hp;
	}
	
	public void setHP(int hp)
	{
		this.hp = hp;
		if (this.hp > STAGE_MAX_HPS[stage_idx]) {
			this.hp = STAGE_MAX_HPS[stage_idx];
		}
	}
	
	public int getXP()
	{
		return xp;
	}
	
	public void setXP(int xp)
	{
		this.xp = xp;
	}
	
	public int getGold()
	{
		return gold;
	}
	
	public void setGold(int gold)
	{
		this.gold = gold;
	}

	public int getItemLV()
	{
		return item_lv;
	}
	
	public void setItemLV(int item_lv)
	{
		this.item_lv = item_lv;
	}
	
	public int getUseTime()
	{
		return use_time;
	}
	
	public void setUseTime(int t)
	{
		this.use_time = t;
	}
	
	public String getStage()
	{
		return STAGE_NAMES[stage_idx];
	}
	
	public void setStage(String stage)
	{
		for (int i = 0; i < STAGE_NAMES.length; ++i) {
			if (STAGE_NAMES[i].equals(stage)) {
				this.stage_idx = i;
				break;
			}
		}
	}
	
	public int getStageIdx() {
		return stage_idx;
	}
}

