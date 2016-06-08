package com.tomato;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

//public class FeedBackActivity extends Activity {
//
//	private EditText et_content;
//	private EditText et_email;
//	private Button btn_send;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.feedback);
//		
//		et_content = (EditText) findViewById(R.id.et_feedback_content);
//		et_email = (EditText) findViewById(R.id.et_feedback_email);
//		btn_send = (Button) findViewById(R.id.btn_send_feedback);
//		
//		btn_send.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				String fb_content = et_content.getText().toString();
//				String fb_email = et_email.getText().toString();
//				
//				finish();
//			}
//		});
//	}
//}

/*
 * 基于 WebView 实现意见反馈
 */
public class FeedBackActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_web);

		wv_feedback = (WebView) findViewById(R.id.wv_feedback);
		wv_feedback.getSettings().setJavaScriptEnabled(true);
		wv_feedback.loadUrl("http://www.diaochapai.com/survey1735844");
		wv_feedback.setWebViewClient(new MyWebViewClient());
	}

	public class MyWebViewClient extends WebViewClient {
		public boolean shouldOverviewUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	private WebView wv_feedback;
}