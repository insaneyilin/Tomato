package com.tomato;

import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * 用于增加 HP 的服务（道具的 buff 功能实现）
 */
public class HpIncService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		PetStatusDB db = PetStatusDB.getInstance(getApplicationContext());
		
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		Date now = ca.getTime();
		ca.add(Calendar.DATE, -1);
		
		Date lastDay = ca.getTime(); 
		
		PetStatus ps = new PetStatus();

		if (ps.readFromDB(db, now)) {
			// 已经有今天的记录，直接读取
			System.out.println("Service: 读取数据库成功");
		} else if (ps.readFromDB(db, lastDay)) {
		    // 没有今天的记录，但有昨天的，新建今天的记录
			ps.writeToDB(db, now);
		} else {
			ps.die();
			ps.writeToDB(db, now);
		}
		ps.readFromDB(db, now);
		
		ps.setHP(ps.getHP() + intent.getIntExtra("hp_inc", 0));
		ps.updateDB(db, now);
		
		// ------------------
		Intent i = new Intent("com.tomato.HP_ALARM_UI_UPDATE");
		getApplication().sendBroadcast(i);
		
		stopSelf();  // 读写完数据库后关闭该服务
		
		return super.onStartCommand(intent, flags, startId);
	}
}
