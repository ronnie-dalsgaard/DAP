package rd.dap;

import static rd.dap.AudiobookActivity.STATE_EDIT;
import static rd.dap.AudiobookActivity.STATE_NEW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Data;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class AudiobookGridFragment extends Fragment implements /*MiniPlayerObserver,*/ OnItemClickListener, OnItemLongClickListener{
	private static final String TAG = "AudiobookGridActivity";
	private static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	private static ImageAdapter adapter;
	public static FragmentMiniPlayer miniplayer;
	private GridView grid;
	private static final int REQUEST_NEW_AUDIOBOOK = 9001;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;

	public AudiobookGridFragment(FragmentMiniPlayer miniplayer) {
		AudiobookGridFragment.miniplayer = miniplayer;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		adapter = new ImageAdapter(getActivity(), R.layout.cover_view, audiobooks);

		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				AudiobookManager am = AudiobookManager.getInstance();
				ArrayList<Audiobook> loadedList = am.loadAudiobooks(getActivity()); 
				audiobooks.clear();
				audiobooks.addAll(loadedList);
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				Log.d(TAG, "onPostExecute - audiobooks loaded");
				for(Audiobook a : audiobooks) System.out.println(a);
				getActivity().runOnUiThread(new Runnable() {
					@Override public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		}.execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.activity_grid_with_miniplayer, container, false);
		
		grid = (GridView) v.findViewById(R.id.grid_layout_gv);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(this);
		grid.setOnItemLongClickListener(this);
		
		return v;
	}
	
//	Log.d(TAG, "menu_item_new_audiobook");
//	intent = new Intent(this, FileBrowserActivity.class);
//	intent.putExtra("type", TYPE_FOLDER);
//	intent.putExtra("message", "Select folder");
//	intent.putExtra("requestcode", REQUEST_NEW_AUDIOBOOK);
//	startActivityForResult(intent, REQUEST_NEW_AUDIOBOOK);

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		Log.d(TAG, "onItemClick");
		Data.setAudiobook(audiobooks.get(index));
		Data.setPosition(0);
		Data.setTrack(Data.getAudiobook().getPlaylist().get(Data.getPosition()));
		
		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
		miniplayer.reload();
		miniplayer.updateView();
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		Log.d(TAG, "onItemLongClick");
		ChangeAudiobookDialogFragment frag = ChangeAudiobookDialogFragment.newInstance(position);
		frag.show(getFragmentManager(), "ChagenAudiobookDialogFragment");
		
		return true; //consume click
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
		case REQUEST_NEW_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_NEW_AUDIOBOOK");
			if(data == null) return;
			String folder_path = data.getStringExtra("result");
			File folder = new File(folder_path);
			AudiobookManager manager = AudiobookManager.getInstance();
			Audiobook audiobook = manager.autoCreateAudiobook(folder, true);
			Intent intent = new Intent(getActivity(), AudiobookActivity.class);
			intent.putExtra("state", STATE_NEW);
			intent.putExtra("audiobook", audiobook);
			startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK);
			break;
		case REQUEST_EDIT_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_AUDIOBOOK");
			audiobooks.clear();
			audiobooks.addAll(AudiobookManager.getInstance().getAudiobooks(getActivity()));
			adapter.notifyDataSetChanged();
		}
	}

	class ImageAdapter extends ArrayAdapter<Audiobook> {
		private List<Audiobook> audiobooks;

		public ImageAdapter(Context context, int resource, List<Audiobook> audiobooks) {
			super(context, resource, audiobooks);
			this.audiobooks = audiobooks;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.cover_view, parent, false);
				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

				holder = new ViewHolder();
				holder.cover_iv = (ImageView) convertView.findViewById(R.id.cover_view_cover_iv);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Audiobook audiobook = audiobooks.get(position);
			if(audiobook.getCover() != null){
				String cover = audiobook.getCover();
				Bitmap bm = BitmapFactory.decodeFile(cover);
				holder.cover_iv.setImageBitmap(bm);
			} else {
				Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
				holder.cover_iv.setImageDrawable(drw);
			}

			return convertView;
		}
	}
	static class ViewHolder {
		public ImageView cover_iv;
	}

	public static class ChangeAudiobookDialogFragment extends DialogFragment {
		public static final ChangeAudiobookDialogFragment newInstance(int position){
			ChangeAudiobookDialogFragment fragment = new ChangeAudiobookDialogFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			fragment.setArguments(bundle);
			return fragment ;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int position = getArguments().getInt("position");
			final Audiobook audiobook = audiobooks.get(position);

			return new AlertDialog.Builder(getActivity())
			.setMessage("Change audiobook")
			.setPositiveButton("Edit audiobook", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(getActivity(), AudiobookActivity.class);
					intent.putExtra("state", STATE_EDIT);
					intent.putExtra("audiobook", audiobook);
					startActivity(intent);
				}
			})
			.setNegativeButton("Delete audiobook", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					ConfirmDeleteDialogFragment frag = ConfirmDeleteDialogFragment.newInstance(position);
					frag.show(getFragmentManager(), "ConfirmDeleteDialogFragment");
				}
			})
			.create();
		}
	}
	public static class ConfirmDeleteDialogFragment extends DialogFragment {
		public static final ConfirmDeleteDialogFragment newInstance(int position){
			ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			fragment.setArguments(bundle);
			return fragment ;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int position = getArguments().getInt("position");
			final Audiobook audiobook = audiobooks.get(position);
			return new AlertDialog.Builder(getActivity())
			.setMessage("Confirm delete "+audiobook.getAuthor() + " - " + audiobook.getAlbum())
			.setPositiveButton("Cancel", null) //Do nothing
			.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					//stop and un-set as current
					miniplayer.getPlayer().pause();
					Data.setAudiobook(null);
					Data.setTrack(null);
					Data.setPosition(-1);

					//update the miniplayers view
					miniplayer.updateView();

					//Remove the audiobook
					AudiobookManager.getInstance().removeAudiobook(getActivity(), audiobook);

					//update the list
					audiobooks.remove(audiobook);
					adapter.notifyDataSetChanged();
				}
			})
			.create();
		}
	}

}
