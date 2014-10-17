package rd.dap.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

public class ChangeBookmarkDialogFragment extends DialogFragment {

	public static final ChangeBookmarkDialogFragment newInstance(int position){
		ChangeBookmarkDialogFragment fragment = new ChangeBookmarkDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int position = getArguments().getInt("position");
//		final Bookmark bookmark = Data.getBookmarks().get(position);

		return new AlertDialog.Builder(getActivity())
		.setMessage("Change bookmark")
		.setPositiveButton("Edit bookmark", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
//				Intent intent = new Intent(getActivity(), AudiobookActivity.class);
//				intent.putExtra("state", STATE_EDIT);
//				intent.putExtra("bookmark", bookmark);
//				startActivity(intent);
				Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_LONG).show();
			}
		})
		.setNegativeButton("Delete bookmark", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				ConfirmDeleteBookmarkDialogFragment frag = ConfirmDeleteBookmarkDialogFragment.newInstance(position);
				frag.show(getFragmentManager(), "ConfirmDeleteBookmarkDialog");
			}
		})
		.create();
	}
}
