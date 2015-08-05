package com.vdemo.acy;

import com.vdemo.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Intent;

public class MainAcy extends Activity implements OnClickListener {
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acy_main);
		init();
	}

	private void init() {
		findViewById(R.id.btn_in).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_in:
			intent = new Intent(this, PlayAcy.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
}
