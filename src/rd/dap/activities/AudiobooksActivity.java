package rd.dap.activities;

import rd.dap.fragments.AudiobooksFragment;
import android.app.Activity;
import android.os.Bundle;

public class AudiobooksActivity extends Activity {
	public static final int REQUEST_AUDIOBOOK = 8801;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState == null)
            getFragmentManager().beginTransaction().add(android.R.id.content, new AudiobooksFragment()).commit();
	}
}
