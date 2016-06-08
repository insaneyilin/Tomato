package com.tomato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AboutAppFragment extends Fragment implements OnClickListener {

	private View view;
	private TextView tv_about_app;
	private ListView lv_about;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.about_app, container, false);
		tv_about_app = (TextView) view.findViewById(R.id.about_app);
		tv_about_app.setText("AboutApp");
		tv_about_app.setVisibility(View.GONE);

		lv_about = (ListView) view.findViewById(R.id.lv_about);
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), 
				AboutAppFragment.getListViewData(), R.layout.about_list, 
				new String[] {"title"}, new int[]{R.id.tv_about_list});

		lv_about.setAdapter(adapter);
		
		lv_about.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:  // ����ý��
					
					break;
					
				case 1:  // �������
					startFeedbackActivity();
					break;
					
				case 2:  // ��������
					showAboutDialog();
					break;

				default:
					break;
				}
			}
			
		});
		
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO �Զ����ɵķ������
		switch (v.getId()) {

		default:
			break;
		}
	}

	private static List<Map<String, Object>> getListViewData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "����ý��");
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "�������");
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "��������");
		list.add(map);
		
		return list;
	}
	
	private void showAboutDialog() {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage("Hello World");
		builder.setTitle("��������");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		builder.create().show();
	}
	
	private void startFeedbackActivity() {
		Intent i = new Intent(getActivity(), FeedBackActivity.class);
		startActivity(i);
	}
}
