package com.tomato;

import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * �������� HP �ķ��񣨵��ߵ� buff ����ʵ�֣�
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
			// �Ѿ��н���ļ�¼��ֱ�Ӷ�ȡ
			System.out.println("Service: ��ȡ���ݿ�ɹ�");
		} else if (ps.readFromDB(db, lastDay)) {
		    // û�н���ļ�¼����������ģ��½�����ļ�¼
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
		
		stopSelf();  // ��д�����ݿ��رո÷���
		
		return super.onStartCommand(intent, flags, startId);
	}
}
