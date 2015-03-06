package rd.dap.activities;

import rd.dap.model.Audiobook;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AudiobooksActivity extends Activity implements AudiobooksFragment.OnAudiobookSelectedListener{
	private static final String TAG = "AudiobookActivity";
	public static final int REQUEST_AUDIOBOOK = 8801;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		if(savedInstanceState == null)
            getFragmentManager().beginTransaction().add(android.R.id.content, new AudiobooksFragment()).commit();
	}


	@Override
	public void onAudiobookSelected(Audiobook audiobook) {
		Intent intent = new Intent();
		intent.putExtra("result", audiobook);
		setResult(REQUEST_AUDIOBOOK, intent);
		finish();
	}

	
}
