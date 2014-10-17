package rd.dap.dialogs;

import rd.dap.R;
import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class ConfirmDeleteAudiobookDialogFragment extends DialogFragment {
	private static final String TAG = "ConfirmDeleteAudiobookDialogFragment";
	private Changer changer;

	public static final ConfirmDeleteAudiobookDialogFragment newInstance(int position){
		ConfirmDeleteAudiobookDialogFragment fragment = new ConfirmDeleteAudiobookDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	@Override 
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
            changer = (Changer) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback");
        }
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int position = getArguments().getInt("position");
		final Audiobook audiobook = Data.getAudiobooks().get(position);
		return new AlertDialog.Builder(getActivity())
		.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_action_warning))
		.setTitle("Confirm delete")
		.setMessage(audiobook.getAuthor() + "\n" + audiobook.getAlbum())
		.setNegativeButton("Cancel", null) //Do nothing
		.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				if(audiobook.equals(Data.getCurrentAudiobook())){
					FragmentMiniPlayer miniplayer = changer.getMiniplayer();
					if(miniplayer != null){
						//stop and un-set as current
						miniplayer.getPlayer().pause();
						Data.setCurrentAudiobook(null);
						Data.setCurrentTrack(null);
						Data.setCurrentPosition(-1);

						//update the miniplayers view
						miniplayer.updateView();
					}
				}

				//Remove the audiobook
				AudiobookManager.getInstance().removeAudiobook(getActivity(), audiobook);
				Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
				BookmarkManager.getInstance().removeBookmark(getActivity(), bookmark);
				Log.d(TAG, "Deleting Audiobook:\n"+audiobook);
				Log.d(TAG, "Deleting Bookmark:\n"+bookmark);
				
				changer.updateAudiobooks();
				changer.updateBookmarks();
				changer.updateController();
			}
		})
		.create();
	}
}
