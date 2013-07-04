package com.shephertz.app42.photo.sharing;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
/*
 * Splash Screen of application
 */
public class SplashActivty extends Activity{
	private Timer timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);	
		startTimer();
	}
	
	/*
	 * This function allows user to schedule time for Splash
	 */
	private void startTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				gotoFriendList();
			}
		}, Constants.SplashTime);
	}
	
	/*
	 * This function allows user to navigate user Facebook friendlist
	 */
	private void gotoFriendList(){
		Intent intent=new Intent(this,FriendList.class);
		startActivity(intent);
		finish();
	
	}
	
	  /**
     * Processes splash screen touch events
     */
    @Override
    public boolean onTouchEvent(MotionEvent evt)
    {
        if(evt.getAction() == MotionEvent.ACTION_DOWN)
        {
        	if (timer != null) {
    			timer.cancel();
    			timer = null;
    			gotoFriendList();
    		}
        }
        return true;
    }  
	
	public void onStart() {
		super.onStart();
	
	}

	/*
	 * * This method is called when a Activty is stop disable all the events if
	 * occuring (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	public void onStop() {
		super.onStop();
	

	}

	/*
	 * This method is called when a Activty is finished or user press the back
	 * button (non-Javadoc)
	 * 
	 * @override method of superclass
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * called when this activity is restart again
	 * 
	 * @override method of superclass
	 */
	public void onReStart() {
		super.onRestart();
	}

	/*
	 * called when activity is paused
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
	}

	/*
	 * called when activity is resume
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();
	}
}

    

