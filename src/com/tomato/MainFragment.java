package com.tomato;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnClickListener {

	private View view;
	private Button start;
	private TextView tv_gold;
	private TextView tv_xp;
	private TextView tv_level;
	private TextView tv_stage;
	private TextView tv_hp;
	private TextView tv_time;

	private TimeCount timeClock;

	private TextView tv_motto;

	// 标语格言
	private static final String[] mottos = {
		"保持专注", "时间就是金钱", "一寸光阴一寸金"	
	};
	private int motto_idx = 0;
	
	private boolean started_or_not;
	private long setTime;

	MainActivity mainAty = null;
	
	protected void init() {
		started_or_not = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mainAty = (MainActivity) getActivity();
		PetStatus ps = mainAty.getPetStatus();
		PetStatusDB psDB = mainAty.getPetStatusDB();

		setTime = 1000 * 60 * 30;

		view = inflater.inflate(R.layout.main_fragment, container, false);

		start = (Button) view.findViewById(R.id.start);
		start.setOnClickListener(this);

		tv_gold = (TextView) view.findViewById(R.id.tv_gold);
		tv_gold.setText(Integer.toString(ps.getGold()));

		tv_xp = (TextView) view.findViewById(R.id.tv_xp);
		tv_xp.setText(Integer.toString(ps.getXP()));

		tv_level = (TextView) view.findViewById(R.id.tv_level);
		tv_level.setText(Integer.toString(ps.getLevel()));

		tv_stage = (TextView) view.findViewById(R.id.tv_stage);
		tv_stage.setText(ps.getStage());

		tv_hp = (TextView) view.findViewById(R.id.tv_hp);
		tv_hp.setText(Integer.toString(ps.getHP()));

		tv_time = (TextView) view.findViewById(R.id.tv_time);
		tv_time.setText(Integer.toString(ps.getUseTime()));

		tv_motto = (TextView) view.findViewById(R.id.tv_motto);
		
		timeClock = (TimeCount) view.findViewById(R.id.time);
		timeClock.setOnClickListener(this);
		timeClock.setEndTime(setTime);
		timeClock.setClockListener(new TimeCount.ClockListener() {

			@Override
			public void timeEnd() {
				// TODO Auto-generated method stub
				init();
				timeClock.stop();
				timeClock.setEndTime(setTime);
				start.setText("开始");
				mainAty.setFocusMode(false);
			}

			@Override
			public void gainGold() {
				onGainGold();
				motto_idx = (motto_idx + 1) % MainFragment.mottos.length;
				tv_motto.setText(MainFragment.mottos[motto_idx]);
			}

			@Override
			public void gainXP(long time) {
				// TODO Auto-generated method stub
				onGainXP(time);
			}
		});

		return view;
	}

	@Override
	public void onClick(View v) {
		//MainActivity mainAty = (MainActivity) getActivity();
		
		switch (v.getId()) {
		case R.id.time:
			if (started_or_not == false) {
				CharSequence[] items = { "10分钟", "15分钟", "20分钟", "25分钟", "30分钟", "35分钟", "40分钟", "45分钟", "50分钟", "55分钟",
						"60分钟", "5秒钟" };
				Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle("设置时间")
						.setSingleChoiceItems(items, 4, new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int index) {
								// TODO 自动生成的方法存根
								if (index == 11) {
									setTime = 5000;
								} else {
									setTime = 1000 * 60 * (index + 2) * 5;
								}
							}
						}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								timeClock.setEndTime(setTime);
								timeClock.onAttachedToWindow();
							}
						}).create();
				dialog.show();
			}
			break;
		case R.id.start:
			if (started_or_not == false) {
				started_or_not = true;
				timeClock.setEndTime(setTime);
				timeClock.start();
				start.setText("放弃");
				mainAty.setFocusMode(true);
				
			} else {
				init();
				timeClock.stop();
				timeClock.setEndTime(setTime);
				start.setText("开始");
				mainAty.setFocusMode(false);
			}
			break;
		default:
			break;
		}
	}

	public void updateTextViews() {

		//MainActivity mainAty = (MainActivity) getActivity();
		PetStatus ps = mainAty.getPetStatus();
		PetStatusDB psDB = mainAty.getPetStatusDB();

		tv_gold.setText(Integer.toString(ps.getGold()));

		tv_xp.setText(Integer.toString(ps.getXP()));

		tv_level.setText(Integer.toString(ps.getLevel()));

		tv_stage.setText(ps.getStage());

		tv_hp.setText(Integer.toString(ps.getHP()));

		tv_time.setText(Integer.toString(ps.getUseTime()));
	}

	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateTextViews();
	}

	public void onGainGold() {
		MainActivity mainAty = (MainActivity) getActivity();
		PetStatus ps = mainAty.getPetStatus();
		PetStatusDB psDB = mainAty.getPetStatusDB();

		// TODO Auto-generated method stub
		System.out.println("gain Gold");
		ps.gainGold();
		tv_gold.setText(Integer.toString(ps.getGold()));

		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		Date now = ca.getTime();
		ps.updateDB(psDB, now);	
	}
	
	public void onGainXP(long time) {
		MainActivity mainAty = (MainActivity) getActivity();
		PetStatus ps = mainAty.getPetStatus();
		PetStatusDB psDB = mainAty.getPetStatusDB();

		int minutes = (int) time / 1000 / 60;

		System.out.println("gain XP of " + minutes + " minutes");

		if (minutes > 0) {
			ps.gainXP(minutes);
		} else {
			ps.setXP(ps.getXP() + 1);
		}

		if (time > 0) {
			ps.setUseTime(ps.getUseTime() + (int) time / 1000);
		}

		if (ps.levelUp()) {
			Toast.makeText(mainAty.getApplicationContext(), "等级提升了", Toast.LENGTH_SHORT).show();
		}

		if (ps.evovle()) {
			Toast.makeText(mainAty.getApplicationContext(), "成长到新的阶段", Toast.LENGTH_SHORT).show();
		}

		updateTextViews();

		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		Date now = ca.getTime();
		ps.updateDB(psDB, now);
	}

	// 测试用
	public void onGain15XP() {

		//MainActivity mainAty = (MainActivity) getActivity();
		PetStatus ps = mainAty.getPetStatus();
		PetStatusDB psDB = mainAty.getPetStatusDB();

		if (ps.levelUp()) {
			Toast.makeText(mainAty.getApplicationContext(), "等级提升了", Toast.LENGTH_SHORT).show();
		}

		if (ps.evovle()) {
			Toast.makeText(mainAty.getApplicationContext(), "成长到新的阶段", Toast.LENGTH_SHORT).show();
		}

		updateTextViews();

		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		Date now = ca.getTime();
		ps.updateDB(psDB, now);
	}
}
