package rd.dap.dialogs;

import static rd.dap.AudiobookActivity.STATE_EDIT;
import rd.dap.AudiobookActivity;
import rd.dap.model.Audiobook;
import rd.dap.model.Data;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ChangeAudiobookDialogFragment extends DialogFragment {
	private static final String TAG = "ChangeAudiobookDialogFragment";
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	private static final int REQUEST_DELETE_AUDIOBOOK = 9003;

	public static final ChangeAudiobookDialogFragment newInstance(int position){
		Log.d(TAG, "newInstance");
		ChangeAudiobookDialogFragment fragment = new ChangeAudiobookDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateDialog");
		final int position = getArguments().getInt("position");
		final Audiobook audiobook = Data.getAudiobooks().get(position);

		return new AlertDialog.Builder(getActivity())
		.setMessage("Change audiobook")
		.setPositiveButton("Edit audiobook", new DialogInterface.OnClickListener() {
			
			@Override 
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "onClick - Edit audiobook");
				Intent intent = new Intent(getActivity(), AudiobookActivity.class);
				intent.putExtra("state", STATE_EDIT);
				intent.putExtra("audiobook", audiobook);
//				startActivity(intent);
				startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK); 
			}
		})
		.setNegativeButton("Delete audiobook", new DialogInterface.OnClickListener() {
			
			@Override 
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "onClick - Delete audiobook");
				ConfirmDeleteAudiobookDialogFragment frag = ConfirmDeleteAudiobookDialogFragment.newInstance(position);
				frag.show(getFragmentManager(), "ConfirmDeleteAudiobookDialog");
			}
		})
		.create();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "onActivityResult");
	}
}
