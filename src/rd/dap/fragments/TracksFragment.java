package rd.dap.fragments;

import java.util.ArrayList;

import rd.dap.R;
import rd.dap.activities.PlayerHandler;
import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import rd.dap.services.PlayerService;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TracksFragment extends Fragment implements OnItemClickListener {
	private GridView gridview;
	private GridViewAdapter adapter;
	private PlayerService player;
	private ArrayList<Track> tracks; 
	
	public TracksFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Audiobook audiobook = null;
		try {
			PlayerHandler playerhandler = (PlayerHandler) getActivity();
			player = playerhandler.getPlayer();
			if(player != null) audiobook = player.getAudiobook();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement PlayerHandler");
		}
		tracks = audiobook == null ? new ArrayList<Track>() : audiobook.getPlaylist();
		
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_tracks_grid, container, false);
		gridview = (GridView) layout.findViewById(R.id.fragment_tracks_grid);
		
		
		adapter = new GridViewAdapter(getActivity(), tracks);
		gridview.setAdapter(adapter);

		gridview.setOnItemClickListener(this);
		
		return layout;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	public class GridViewAdapter extends ArrayAdapter<Track> {
		private Context context;
		@SuppressWarnings("unused")
		private ArrayList<Track> list;

		public GridViewAdapter(Context context, ArrayList<Track> list) {
			super(context, R.layout.fragment_tracks_grid_item, list);
			this.context = context;
			this.list = list;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				convertView = inflater.inflate(R.layout.fragment_tracks_grid_item, parent, false);
				holder = new ViewHolder();
				holder.trackno_tv = (TextView) convertView.findViewById(R.id.fragment_tracks_grid_item_trackno_tv);
				holder.circle_iv = (ImageView) convertView.findViewById(R.id.fragment_tracks_grid_item_circle_iv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			int trackno = position;
			holder.trackno_tv.setText(String.format("%02d", trackno));
			holder.circle_iv.setVisibility((player.getTrackno() == trackno) ? View.VISIBLE : View.INVISIBLE);
			return convertView;
		}
	}

	static class ViewHolder {
		TextView trackno_tv;
		ImageView circle_iv;
	}

	public void displayTracks(ArrayList<Track> tracks) {
		this.tracks = tracks;
		if(adapter != null) adapter.notifyDataSetChanged();
	}
}
