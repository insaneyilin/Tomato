package com.tomato;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.accounts.NetworkErrorException;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.Window;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener {

	// private long deadTime;
	// private long remainderTime;
	// private Button pause;

	private TextView tv_money;
	private TextView tv_aboutApp;
	private TextView tv_petzone;

	private Fragment main_Fragment;
	private Fragment pet_zoneFragment;
	private Fragment about_appFragment;

	private PetStatus petStatus;   // ������Ϣ
	private PetStatusDB psDB;      // ������Ϣ�����ݿ�

	private PendingIntent pendingHp1; // ����ÿСʱ���� HP
	
	private AlarmManager alarmManager;  // ����ϵͳ���ӷ���ʵ��ÿСʱ����/���� HP ����

	boolean mIsReceiverRegistered = false;   // ����ע�� mReceiver
	HpAlarmBroadcastReceiver mReceiver = null;   // ��̬ע���  Receiver

	static final int ALARM_HP_DEC_ID = 0;  // AlarmManager ʹ�õ� PendingIntent �� requestCode
	static final int ALARM_HP_INC_ID = 1;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	private void initFragment(int index) {
		// ������������V4���µ�Fragment����������Ĺ�����Ҫ��getSupportFragmentManager��ȡ
		FragmentManager fragmentManager = getSupportFragmentManager();
		// ��������
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// ��������Fragment

		hideFragment(transaction);

		switch (index) {
		case 0:
			if (main_Fragment == null) {
				main_Fragment = new MainFragment();
				transaction.add(R.id.fl_content, main_Fragment);
			} else {
				transaction.show(main_Fragment);
				// ����� Fragment ʱ������ʾ�ؼ�
				((MainFragment) main_Fragment).updateTextViews();  
			}
			break;
		case 1:
			if (pet_zoneFragment == null) {
				pet_zoneFragment = new PetZoneFragment();
				transaction.add(R.id.fl_content, pet_zoneFragment);
			} else {
				transaction.show(pet_zoneFragment);
				// ����� Fragment ʱ������ʾ�ؼ�
				((PetZoneFragment) pet_zoneFragment).updateViews();
				((PetZoneFragment) pet_zoneFragment).init();
			}
			break;
		case 2:
			if (about_appFragment == null) {
				about_appFragment = new AboutAppFragment();
				transaction.add(R.id.fl_content, about_appFragment);
			} else {
				transaction.show(about_appFragment);
			}
			break;

		// �ύ����
		}
		transaction.commit();
	}

	private void hideFragment(FragmentTransaction transaction) {
		if (main_Fragment != null) {
			transaction.hide(main_Fragment);
		}
		if (pet_zoneFragment != null) {
			transaction.hide(pet_zoneFragment);
		}
		if (about_appFragment != null) {
			transaction.hide(about_appFragment);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);// �Զ��岼��

		initPetStatus();  // ��ʼ��������Ϣ�����ݿ�
		initFragment(0);

		tv_aboutApp = (TextView) findViewById(R.id.aboutApp);
		tv_aboutApp.setOnClickListener(this);

		tv_money = (TextView) findViewById(R.id.mymoney);
		tv_money.setOnClickListener(this);

		tv_petzone = (TextView) findViewById(R.id.pet_zone);
		tv_petzone.setOnClickListener(this);

		// --------------------------
		// ÿСʱ���� HP���ܣ�ֻ�ڵ�һ������Ӧ��ʱ����
		// isFirstStart() ͨ���ж� SharedPreferences ���Ƿ�����Ӧ���ַ���ʵ��
		if (isFirstStart()) {
			System.out.println("�״����������ö�ʱ���� HP ����");

			Intent i = new Intent(this, HpAlarmReceiver.class);
			i.putExtra("type", "HP_DEC");
			pendingHp1 = PendingIntent.getBroadcast(this, ALARM_HP_DEC_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
			long firsttime = SystemClock.elapsedRealtime();

			alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firsttime, 60 * 60 * 1000, pendingHp1);
		}
	}

	/*
	 * ����һ�����ӡ�ÿСʱ���� hp_inc �� HP ֵ
	 */
	public void addHpBuff(int hp_inc) {
		Intent i =  new Intent(getApplicationContext(), HpAlarmReceiver.class);
		i.putExtra("type", "HP_INC");
		i.putExtra("hp_inc", hp_inc);
		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), ALARM_HP_INC_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		
		long firsttime = SystemClock.elapsedRealtime();
		am.setRepeating(AlarmManager.RTC_WAKEUP, firsttime, 60 * 60 * 1000, pending);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		// ��̬ע�� Receiver 
		if (mIsReceiverRegistered) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
			mIsReceiverRegistered = false;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!mIsReceiverRegistered) {
			if (mReceiver == null)
				mReceiver = new HpAlarmBroadcastReceiver();
			registerReceiver(mReceiver, new IntentFilter("com.tomato.HP_ALARM_UI_UPDATE"));
			mIsReceiverRegistered = true;
		}

		initPetStatus();
		if (main_Fragment != null)
			((MainFragment) main_Fragment).updateTextViews();
	}

	@Override
	public void onClick(View v) {
		// TODO �Զ����ɵķ������
		switch (v.getId()) {
		case R.id.aboutApp:
			initFragment(2);
			break;
		case R.id.mymoney:
			initFragment(0);
			break;
		case R.id.pet_zone:
			initFragment(1);
			break;
		default:
			break;
		}
	}

	private void initPetStatus() {
		petStatus = new PetStatus();
		psDB = PetStatusDB.getInstance(getApplicationContext());

		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		Date now = ca.getTime();
		ca.add(Calendar.DATE, -1);

		Date lastDay = ca.getTime();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

		// �Ѿ��н���ļ�¼��ֱ�Ӷ�ȡ
		if (petStatus.readFromDB(psDB, now))
			return;

		// û�н���ļ�¼����������ģ��½�����ļ�¼
		if (petStatus.readFromDB(psDB, lastDay)) {
			petStatus.writeToDB(psDB, now);
			return;
		}

		// ���졢���춼û�м�¼
		petStatus.die();
		petStatus.writeToDB(psDB, now);
	}

	public PetStatus getPetStatus() {
		return petStatus;
	}

	public PetStatusDB getPetStatusDB() {
		return psDB;
	}

	private class HpAlarmBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			initPetStatus();
			if (main_Fragment != null)
				((MainFragment) main_Fragment).updateTextViews();
		}
	}

	// ������ݿ⣬������
	private void clearDB() {
		SQLiteDatabase dbWriter = psDB.getWritableDatabase();
		dbWriter.delete(PetStatusDB.TABLE_NAME, null, null);
	}

	// ϵͳ�˵���������
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);// main.xml������res/menu/Ŀ¼��
		return true;
	}

	/*
	 * ϵͳ�˵�����Ӧ��������
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		Date now = ca.getTime();

		switch (item.getItemId()) {
		case R.id.menu_clear_db:
			// do something
			clearDB();
			initPetStatus();
			initFragment(0);
			if (main_Fragment != null)
				((MainFragment) main_Fragment).updateTextViews();

			break; // �������Ժ�����Ҳ����return true;

		case R.id.menu_add_20_gold:
			petStatus.setGold(petStatus.getGold() + 20);
			petStatus.updateDB(psDB, now);
			if (main_Fragment != null)
				((MainFragment) main_Fragment).updateTextViews();
			break;

		case R.id.menu_add_15_xp:
			petStatus.setXP(petStatus.getXP() + 15);
			petStatus.updateDB(psDB, now);
			if (main_Fragment != null)
				((MainFragment) main_Fragment).onGain15XP();

			break;

		case R.id.menu_100_gold:
			petStatus.setGold(petStatus.getGold() + 100);
			petStatus.updateDB(psDB, now);
			if (main_Fragment != null)
				((MainFragment) main_Fragment).updateTextViews();
			break;

		case R.id.menu_reduce_2_hp:

			petStatus.setHP((petStatus.getHP() - 2 >= 0) ? petStatus.getHP() - 2 : 0);
			if (petStatus.getHP() == 0) {
				petStatus.die();
				Toast.makeText(getApplicationContext(), "��ĳ������", Toast.LENGTH_SHORT).show();
			}
			petStatus.updateDB(psDB, now);
			if (main_Fragment != null)
				((MainFragment) main_Fragment).updateTextViews();
			break;

		case R.id.menu_buy_food:

			petStatus.buyItem(1);
			petStatus.updateDB(psDB, now);
			if (main_Fragment != null)
				((MainFragment) main_Fragment).updateTextViews();
			break;

		case R.id.menu_gen_test_data:
			//generateTestData();
			break;
			
		case R.id.menu_add_hp_buff_5_hp:
			addHpBuff(5);
			break;
		}

		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	public void setFocusMode(boolean flag) {
		if (flag) {
			tv_aboutApp.setVisibility(View.INVISIBLE);
			tv_petzone.setVisibility(View.INVISIBLE);
		} else {
			tv_aboutApp.setVisibility(View.VISIBLE);
			tv_petzone.setVisibility(View.VISIBLE);
		}
	}

	// �ж��Ƿ��һ������Ӧ��
	private boolean isFirstStart() {
		SharedPreferences sp = getApplicationContext().getSharedPreferences(MainActivity.class.getName(),
				Context.MODE_PRIVATE);
		String content = sp.getString("HAS_STARTED_BEFORE", null);

		if (content == null) {
			Editor editor = getApplicationContext()
					.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE).edit();
			editor.putString("HAS_STARTED_BEFORE", "YES");
			editor.commit();
			return true;
		}

		return false;
	}

	// ��������ͼ����ʾ�Ĳ������� 
//	private void generateTestData() {
//		//clearDB();
//
//		int[] use_time_arr = { 720, 680, 675, 630, 580, 565, 520, 460, 385, 355, 350, 270, 245, 190, 175, 150, 145, 85,
//				60, 15 };
//
//		Calendar ca = Calendar.getInstance();
//		ca.setTime(new Date());
//
//		PetStatus ps_tmp = new PetStatus();
//		
//		int i = 19;
//		ps_tmp.setUseTime(use_time_arr[i]);
//		ca.add(Calendar.DATE, -i);
//		Date dt = ca.getTime();
//		if (ps_tmp.writeToDB(psDB, dt)) {
//			System.out.println(i);
//		}
//	}
}
