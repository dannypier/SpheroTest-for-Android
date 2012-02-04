package com.frublin.spherosample;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.robot.base.RollResponse;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SpheroTest extends Activity {

	private final static int STARTUP_ACTIVITY = 0;

	protected static final int GLOW = 0;
	protected static final int ROTATE = 1;
	protected static final int HEADING_TEST = 2;
	protected static final int BACK_AND_FORTH = 3;
	protected static final int QUARTER_CLOCK = 4;
	protected static final int STOP_ROLL = 5;

	private Robot mRobot;

	public int hue = 5;
	private boolean glowup = true;
	private int color = 0;
	private int counter = 0;
	private int heading = 0;
	private boolean forward = true;

	private TextView speedValueField;
	private TextView headingValueField;

	private float speedSetting;
	private long driveTimeSetting;
	private int headingSetting;

	private Spinner speedSelectSpinner;
	private Spinner driveTimeSelectSpinner;
	private Spinner headingSelectSpinner;

	private Button goButton;

	@Override
	public void onStart() {
		super.onStart();
		Intent i = new Intent(this, StartupActivity.class);
		startActivityForResult(i, STARTUP_ACTIVITY);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		attachUIElements();
	}

	private void attachUIElements() {
		speedValueField = (TextView) findViewById(R.id.speedValue);
		headingValueField = (TextView) findViewById(R.id.headingValue);
		speedSelectSpinner = (Spinner) findViewById(R.id.speedSpinner);

		speedSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				speedSetting = Float.parseFloat(parent.getItemAtPosition(position).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		driveTimeSelectSpinner = (Spinner) findViewById(R.id.driveTimeSpinner);
		driveTimeSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				driveTimeSetting = Long.parseLong(parent.getItemAtPosition(position).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		headingSelectSpinner = (Spinner) findViewById(R.id.headingSpinner);
		headingSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				headingSetting = Integer.parseInt(parent.getItemAtPosition(position).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		headingSetting = Integer.parseInt(headingSelectSpinner.getSelectedItem().toString());

		goButton = (Button) findViewById(R.id.goButton);
		goButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RollCommand.sendCommand(mRobot, headingSetting, speedSetting);
				spheroHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						spheroHandler.sendMessage(spheroHandler.obtainMessage(STOP_ROLL));
					}
				}, driveTimeSetting);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		RobotProvider.getDefaultProvider().removeAllControls();
	}

	private void glow() {

		if(mRobot != null){

			if(glowup){
				glowup = hue + 10 > 255 ? false : true;
			}else{
				glowup = hue - 10 < 0 ? true : false;

				if(hue - 10 < 0)
					counter++;
			}

			hue = glowup ? hue + 10 : hue - 10;

			if(counter % 3 == 0){
				RGBLEDOutputCommand.sendCommand(mRobot, hue, 0, 0); // 2
			}else if(counter % 3 == 1){
				RGBLEDOutputCommand.sendCommand(mRobot, 0, hue, 0); // 2
			}else if(counter % 3 == 2){
				RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, hue); // 2
			}

			// Send delayed message on a handler to run blink again
			spheroHandler.postDelayed(new Runnable() {
				public void run() {
					spheroHandler.sendMessage(spheroHandler.obtainMessage(GLOW));
				}
			}, 50);
		}
	}

	private void rotate() {

		RollCommand.sendCommand(mRobot, heading++ % 360, speedSetting);

		// Send delayed message on a handler to run blink again
		spheroHandler.postDelayed(new Runnable() {
			public void run() {
				spheroHandler.sendMessage(spheroHandler.obtainMessage(ROTATE));
			}
		}, 10);
	}

	private void headingTest() {

		heading = heading + 10;

		RollCommand.sendCommand(mRobot, heading, speedSetting);

		Toast.makeText(SpheroTest.this, "Heading: " + heading, Toast.LENGTH_SHORT).show();

		// Send delayed message on a handler to run blink again
		final Handler handler = new Handler(); // 3
		spheroHandler.postDelayed(new Runnable() {
			public void run() {
				spheroHandler.sendMessage(spheroHandler.obtainMessage(HEADING_TEST));
			}
		}, 3000);
	}

	private void backAndForth() {

		RollCommand.sendCommand(mRobot, heading % 360, speedSetting);
		heading += 180;

		try{
			Thread.sleep(5000);
		}catch(InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RollCommand.sendStop(mRobot);

		Toast.makeText(SpheroTest.this, "Heading: " + heading % 360, Toast.LENGTH_SHORT).show();

		// Send delayed message on a handler to run blink again
		spheroHandler.postDelayed(new Runnable() {
			public void run() {
				spheroHandler.sendMessage(spheroHandler.obtainMessage(BACK_AND_FORTH));
			}
		}, 5000);
	}

	private void quarterClock() {

		RollCommand.sendCommand(mRobot, heading % 360, speedSetting);
		heading += 90;

		RollCommand.sendStop(mRobot);

		Toast.makeText(SpheroTest.this, "Heading: " + heading % 360, Toast.LENGTH_SHORT).show();

		// Send delayed message on a handler to run blink again
		spheroHandler.postDelayed(new Runnable() {
			public void run() {
				spheroHandler.sendMessage(spheroHandler.obtainMessage(QUARTER_CLOCK));
			}
		}, 5000);

		spheroHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				spheroHandler.sendMessage(spheroHandler.obtainMessage(STOP_ROLL));
			}
		}, driveTimeSetting);
	}

	private void stopRoll() {
		RollCommand.sendStop(mRobot);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == STARTUP_ACTIVITY && resultCode == RESULT_OK){
			// Get the connected Robot
			final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID); // 1
			if(robot_id != null && !robot_id.equals("")){
				mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id); // 2
			}
			// Start blinking
			// glow();
			// rotate();
			// headingTest();
			// backAndForth();
			// quarterClock();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			

			FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);
		}
	}

	private Handler spheroHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			Log.i("Sphero", "Message: " + msg.what);
			Log.i("Sphero", "Speed: " + speedSetting);
			Log.i("Sphero", "Drive time: " + driveTimeSetting);

			switch(msg.what){
				case GLOW:
					glow();
					break;
				case ROTATE:
					rotate();
					break;
				case HEADING_TEST:
					headingTest();
					break;
				case BACK_AND_FORTH:
					backAndForth();
					break;
				case QUARTER_CLOCK:
					quarterClock();
					break;
				case STOP_ROLL:
					stopRoll();
					break;
			}

		}

	};
}