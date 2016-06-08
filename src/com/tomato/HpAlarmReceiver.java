package com.tomato;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * 接受 AlarmManager 发送的 PendingIntent, 根据 Intent 中的字符串判断
 * 启动哪种服务（减少/增加 HP）
 */
public class HpAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		String type = intent.getStringExtra("type");
		if (type.equals("HP_DEC")) {
			System.out.println("生命值减少");
			Intent service1 = new Intent(context, HpAlarmService.class);
		    context.startService(service1);	
		} else if (type.equals("HP_INC")) {
			int hp_inc = intent.getIntExtra("hp_inc", 0);
			System.out.println("生命值增加 " + hp_inc);
			
			Intent service2 = new Intent(context, HpIncService.class);
			service2.putExtra("hp_inc", hp_inc);
			context.startService(service2);
			
		    System.out.println("HpAlarmReceiver: " + HpAlarmReceiver.getCount());
		    HpAlarmReceiver.setCount(HpAlarmReceiver.getCount() + 1);
		    if (HpAlarmReceiver.getCount() == 2) {
		    	HpAlarmReceiver.setCount(0);
		    	
				Intent i =  new Intent(context.getApplicationContext(), HpAlarmReceiver.class);
				i.putExtra("type", "HP_INC");
				i.putExtra("hp_inc", hp_inc);
				PendingIntent pending = PendingIntent.getBroadcast(context.getApplicationContext(), MainActivity.ALARM_HP_INC_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
		    	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    	am.cancel(pending);
		    }
		}
	}

	static private int getCount() {
		return count;
	}
	
	static private void setCount(int cnt) {
		HpAlarmReceiver.count = cnt;
	}
	
	static int count = 0;
}
