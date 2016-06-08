package com.tomato;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class PetZoneFragment extends Fragment implements OnClickListener {

	private View view;
	private TextView tv_pet_zone;

	private ImageView iv_avatar;
	private ProgressBar pbar_hp;
	private ProgressBar pbar_xp;
	private Button btn_shop;
	private Button btn_stats;
	
	private ColumnChartView columChart;  // hellocharts 中的柱状图 View
    private ColumnChartData columData;   // hellocharts 中的柱状图 Data
    private Button btn_lastweek;
    private Button btn_nextweek;
    private TextView tv_weekchart;

    public final static String[] weekdays = new String[]{"Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun",};
    
    private ListView lv_items;   // 道具列表
    private List<Map<String, Object>> item_data;  // 道具信息数据
    private ItemAdapter itemAdapter;   // 道具列表的  adapter
    
    private Calendar columnChartCal;   // 表示当前柱状图日期
    private Date earliestDate;   // 数据库中最早记录的日期
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.pet_zone, container, false);
		tv_pet_zone = (TextView) view.findViewById(R.id.layout_pet_zone);
		tv_pet_zone.setText("宠物空间");

		iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		pbar_hp = (ProgressBar) view.findViewById(R.id.pbar_hp);
		pbar_xp = (ProgressBar) view.findViewById(R.id.pbar_xp);
		btn_shop = (Button) view.findViewById(R.id.btn_shop);
		btn_stats = (Button) view.findViewById(R.id.btn_stats);
		
		btn_shop.setOnClickListener(this);
		btn_stats.setOnClickListener(this);

		columChart = (ColumnChartView) view.findViewById(R.id.columchart);
		columChart.setZoomEnabled(false);
		columChart.setVisibility(View.GONE);
		
		btn_lastweek = (Button) view.findViewById(R.id.btn_lastweek_chart);
		btn_nextweek = (Button) view.findViewById(R.id.btn_nextweek_chart);
		tv_weekchart = (TextView) view.findViewById(R.id.tv_chart_week);
		btn_lastweek.setOnClickListener(this);
		btn_nextweek.setOnClickListener(this);
		btn_lastweek.setVisibility(View.GONE);
		btn_nextweek.setVisibility(View.GONE);
		tv_weekchart.setVisibility(View.GONE);
		
		lv_items = (ListView) view.findViewById(R.id.lv_items);
		lv_items.setVisibility(View.GONE);
		
		item_data = getItemData();
		itemAdapter = new ItemAdapter(getActivity());
		lv_items.setAdapter(itemAdapter);
		
		MainActivity mainAty = (MainActivity) getActivity();
		PetStatusDB ps_db = PetStatusDB.getInstance(mainAty.getApplicationContext());
		SQLiteDatabase dbReader = ps_db.getReadableDatabase();
		
		// 查询数据库中最早日期
		Cursor c = dbReader.query(PetStatusDB.TABLE_NAME, new String[] { "min(" + PetStatusDB.DATE + ")" }, null, null,
                null, null, null);  
		c.moveToFirst();
		
		String earliestDateStr = c.getString(0);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			earliestDate = format.parse(earliestDateStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_shop:
			if (lv_items.getVisibility() == View.VISIBLE) {
				lv_items.setVisibility(View.GONE);
				tv_pet_zone.setVisibility(View.VISIBLE);
			} else {
				tv_pet_zone.setVisibility(View.GONE);
				columChart.setVisibility(View.GONE);
				btn_lastweek.setVisibility(View.GONE);
				btn_nextweek.setVisibility(View.GONE);
				tv_weekchart.setVisibility(View.GONE);
				columnChartCal = null;
				
				lv_items.setVisibility(View.VISIBLE);
			}
			break;
			
		case R.id.btn_stats:
			if (columChart.getVisibility() == View.VISIBLE) {
				columChart.setVisibility(View.GONE);
				btn_lastweek.setVisibility(View.GONE);
				btn_nextweek.setVisibility(View.GONE);
				tv_weekchart.setVisibility(View.GONE);
				columnChartCal = null;
				
				tv_pet_zone.setVisibility(View.VISIBLE);
			} else {
				columnChartCal = Calendar.getInstance();
				columnChartCal.setFirstDayOfWeek(Calendar.MONDAY);
				
				tv_pet_zone.setVisibility(View.GONE);
				lv_items.setVisibility(View.GONE);
				
				columChart.setVisibility(View.VISIBLE);
				btn_lastweek.setVisibility(View.VISIBLE);
				btn_nextweek.setVisibility(View.VISIBLE);
				tv_weekchart.setVisibility(View.VISIBLE);
				
				btn_lastweek.setEnabled(true);
				btn_nextweek.setEnabled(true);
				
				generateData(columnChartCal);
			}
			
			break;
			
		case R.id.btn_lastweek_chart:
			columnChartCal.add(Calendar.DATE, -7);
			if (getWeekSunday(columnChartCal.getTime()).before(earliestDate)) {
				columnChartCal.add(Calendar.DATE, 7);	
				break;
			}
			
			if (datesInSameWeek(earliestDate, columnChartCal.getTime())) {
				btn_lastweek.setEnabled(false);
			}
			
			{
				Date monday = getWeekMonday(columnChartCal.getTime());
				Date sunday = getWeekSunday(columnChartCal.getTime());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				tv_weekchart.setText(format.format(monday) + " -- " + format.format(sunday));
			}
			generateData(columnChartCal);
			btn_nextweek.setEnabled(true);
			
			break;
			
		case R.id.btn_nextweek_chart:
			columnChartCal.add(Calendar.DATE, 7);
			if (getWeekMonday(columnChartCal.getTime()).after(Calendar.getInstance().getTime())) {
				columnChartCal.add(Calendar.DATE, -7);	
				break;				
			}
			//System.out.println(columnChartCal.getTime().toString());
			
			{
				Date monday = getWeekMonday(columnChartCal.getTime());
				Date sunday = getWeekSunday(columnChartCal.getTime());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				
				if (datesInSameWeek(monday, Calendar.getInstance().getTime())) {
					tv_weekchart.setText("本周");
					btn_nextweek.setEnabled(false);
				} else {
					tv_weekchart.setText(format.format(monday) + " -- " + format.format(sunday));	
				}
			}
			generateData(columnChartCal);
			btn_lastweek.setEnabled(true);
			
			break;
			
		default:
			break;
		}
	}
	
	public void init() {
		tv_pet_zone.setVisibility(View.VISIBLE);
		columChart.setVisibility(View.GONE);
		btn_lastweek.setVisibility(View.GONE);
		btn_nextweek.setVisibility(View.GONE);
		tv_weekchart.setVisibility(View.GONE);
		lv_items.setVisibility(View.GONE);
		itemAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateViews();
	}

	public void updateViews() {
		System.out.println("PetZone:onResume");
		
		MainActivity mainAty = (MainActivity) getActivity();
		PetStatus ps = mainAty.getPetStatus();
		
		int hp_progress = 0;
		int xp_progress = 0;
		
		double cur_hp = ps.getHP() * 1.0;
		double max_hp = PetStatus.STAGE_MAX_HPS[ps.getStageIdx()] * 1.0; 
		hp_progress = (int)(cur_hp / max_hp * 100);
		
		pbar_hp.setProgress(hp_progress);
		
		double cur_xp = ps.getXP() * 1.0;
		double max_xp = PetStatus.XP_TO_NEXT_LEVEL[ps.getLevel()] * 1.0; 
		xp_progress = (int)(cur_xp / max_xp * 100);
		
		pbar_xp.setProgress(xp_progress);		
	}
	
	// 获取一周的数据
	private int[] getWeekData(Calendar c) {
		int[] res = {0, 0, 0, 0, 0, 0, 0};
		
		MainActivity mainAty = (MainActivity) getActivity();
		PetStatusDB ps_db = PetStatusDB.getInstance(mainAty.getApplicationContext());
		
		Date now = c.getTime();
		Date monday = getWeekMonday(now);
		Date sunday = getWeekSunday(now);
		
		PetStatus ps_tmp = new PetStatus();
		
		Calendar cal_tmp = Calendar.getInstance();
		cal_tmp.setFirstDayOfWeek(Calendar.MONDAY);
		cal_tmp.setTime(monday);
		cal_tmp.add(Calendar.DATE, -1);
		int last_use_time = 0;
		if (ps_tmp.readFromDB(ps_db, cal_tmp.getTime())) {
			last_use_time = ps_tmp.getUseTime();
		}
		cal_tmp.add(Calendar.DATE, 1);
		
		int i = 0;
		while (!cal_tmp.getTime().after(sunday)) {
			monday = cal_tmp.getTime();
			if (ps_tmp.readFromDB(ps_db, monday)) {
				res[i] = ps_tmp.getUseTime() - last_use_time;
				last_use_time = ps_tmp.getUseTime();
			}
			cal_tmp.add(Calendar.DATE, 1);
			++i;
		}
		
		return res;
	}
	
	// 根据日期生成一周的数据并通过柱状图显示
	private void generateData(Calendar c) {
		int numSubcolumns = 1;
        int numColumns = 7;
        
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        int[] weekdata = getWeekData(c);
        
        for (int i = 0; i < numColumns; ++i) {
        	values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; ++j) {
                //values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
            	values.add(new SubcolumnValue((float) weekdata[i], ChartUtils.pickColor()));
            }

            Column column = new Column(values);
            column.setHasLabels(true);
            column.setHasLabelsOnlyForSelected(false);
            columns.add(column);
        }
        
        columData = new ColumnChartData(columns);
        
        List<AxisValue> axisXValues = new ArrayList<AxisValue>();
        for (int i = 0; i < numColumns; ++i) {
        	axisXValues.add(new AxisValue(i).setLabel(weekdays[i]));
        }
        Axis axisX = new Axis(axisXValues);
        Axis axisY = new Axis().setHasLines(true);
        
        axisY.setName("每日专注时间");
        axisX.setTextColor(Color.parseColor("#000000"));
        axisY.setTextColor(Color.parseColor("#000000"));
        columData.setAxisXBottom(axisX);
        columData.setAxisYLeft(axisY);
        
        columChart.setColumnChartData(columData);
	}
	
	public final class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView info;
		public Button btn_buy;
	}
	
	public class ItemAdapter extends BaseAdapter {

		public ItemAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return item_data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				
				convertView = inflater.inflate(R.layout.item_list, null);
				holder.img = (ImageView) convertView.findViewById(R.id.iv_item);
				holder.title = (TextView) convertView.findViewById(R.id.tv_item_title);
				holder.info = (TextView) convertView.findViewById(R.id.tv_item_info);
				holder.btn_buy = (Button) convertView.findViewById(R.id.btn_buy_item);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.img.setImageResource((Integer) item_data.get(position).get("img"));
			holder.title.setText((String) item_data.get(position).get("title"));
			holder.info.setText((String) item_data.get(position).get("info"));

			final MainActivity mainAty = (MainActivity) getActivity();
			final PetStatus ps_tmp = mainAty.getPetStatus();
			final PetStatusDB ps_db = PetStatusDB.getInstance(mainAty.getApplicationContext());

			if (ps_tmp.getItemLV() < position || ps_tmp.getGold() < PetStatus.GOLD_COST_OF_ITEMS[position]) {
				holder.btn_buy.setEnabled(false);
			} else {
				holder.btn_buy.setEnabled(true);
			}
			final int itemIdx = position;
			
			final Button btn_buy_tmp = holder.btn_buy; 
			holder.btn_buy.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					System.out.println(ps_tmp.getLevel());
					System.out.println(itemIdx);
					if (ps_tmp.buyItem(itemIdx)) {
						ps_tmp.updateDB(ps_db, Calendar.getInstance().getTime());
						itemAdapter.notifyDataSetChanged();
						
						if (itemIdx == 6) {
							mainAty.addHpBuff(5);
						}
						if (itemIdx == 10) {
							mainAty.addHpBuff(10);
						}
					}
				}
			});
			
			return convertView;
		}
		
		private LayoutInflater inflater;
	}
	
	private List<Map<String, Object>> getItemData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "牛奶");
		map.put("info", "80金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "猫粮");
		map.put("info", "120金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "疫苗");
		map.put("info", "200金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "猫咪罐头");
		map.put("info", "200金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "普通玩具球");
		map.put("info", "280金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "普通猫厕所");
		map.put("info", "360金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "小鱼干");
		map.put("info", "360金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "玩具毛线球");
		map.put("info", "440金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "猫坐垫");
		map.put("info", "520金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "猫爬架");
		map.put("info", "600金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "黄金小鱼干");
		map.put("info", "600金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "玩具老鼠");
		map.put("info", "680金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "豪华猫厕所");
		map.put("info", "760金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "温馨猫坐垫");
		map.put("info", "840金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "豪华猫爬架");
		map.put("info", "920金币");
		map.put("img", R.drawable.ic_launcher);
		list.add(map);
		
		return list;
	}
	
	// 获取 date 所在周的周一日期
	public static Date getWeekMonday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		
		return cal.getTime();
	}
	
	// 获取 date 所在周的周日日期	
	public static Date getWeekSunday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		
		return cal.getTime();
	}
	
	// 判断两个日期是否在同一周
	public boolean datesInSameWeek(Date dt1, Date dt2) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(getWeekMonday(dt1)).equals(format.format(getWeekMonday(dt2))); 
	}
}
