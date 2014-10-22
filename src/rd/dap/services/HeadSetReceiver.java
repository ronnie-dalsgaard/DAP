package rd.dap.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class HeadSetReceiver extends BroadcastReceiver {
	private static final String TAG = "HeadSetReceiver";
	
	public HeadSetReceiver(){
		super();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
//		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
//            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
//                Toast.makeText(context, "HEAD_SET", Toast.LENGTH_LONG).show();
//                Log.d(TAG, "Play pressed");
//            }
//        }
	}

}
