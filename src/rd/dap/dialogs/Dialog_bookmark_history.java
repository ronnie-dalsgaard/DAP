package rd.dap.dialogs;

import java.util.ArrayList;

import rd.dap.R;
import rd.dap.activities.MainActivity;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.support.Time;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Dialog_bookmark_history {
	private MainActivity activity;
	private RelativeLayout base;
	private Bookmark bookmark;
	private Callback callback;
	
	public interface Callback {
		public void onItemSelected(BookmarkEvent event);
	}
	
	public Dialog_bookmark_history(MainActivity activity, Bookmark bookmark, Callback callback) {
		this.activity = activity;
		this.base = activity.getBase();
		this.bookmark = bookmark;
		this.callback = callback;
	}

	public void show(){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_history, base, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Bookmark history");

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Cancel button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_history_btn);
		left_btn.setText("Cancel");
		left_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		//List
		ListView lv = (ListView) dv.findViewById(R.id.dialog_history_list);
		lv.setAdapter(new BookmarkEventAdapter(activity, bookmark.getEvents()));
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dialog.dismiss();
				BookmarkEvent event = bookmark.getEvents().get(position);
				callback.onItemSelected(event);
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
	
	private class BookmarkEventAdapter extends ArrayAdapter<BookmarkEvent> {
		private ArrayList<BookmarkEvent> list;
		
		public BookmarkEventAdapter(Context context, ArrayList<BookmarkEvent> list) {
			super(context, R.layout.bookmark_history_list_item, list);
			this.list = list;
		}

		@Override
		public int getCount(){
			return list.size();
		}
		
		@Override @SuppressLint("InflateParams")
		public View getView(int position, View convertView, ViewGroup parent) {
		    ViewHolder holder;
		    if (convertView == null) {
		        LayoutInflater inflater;
		        inflater = LayoutInflater.from(getContext());
		        convertView = inflater.inflate(R.layout.bookmark_history_list_item, null);
		        holder = new ViewHolder();
		        holder.function_iv = (ImageView) convertView.findViewById(R.id.bookmark_history_list_item_function_iv);
		        holder.progress_tv = (TextView) convertView.findViewById(R.id.bookmark_history_list_item_progress);
		        holder.timestamp_tv = (TextView) convertView.findViewById(R.id.bookmark_history_list_item_timestamp);
		        convertView.setTag(holder);		        
		    } else { holder = (ViewHolder) convertView.getTag(); }

		    BookmarkEvent event = list.get(position);
		    if (event != null) {
		    	switch(event.getFunction()){
		    	case PLAY: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_action_play)); break;
		    	case NEXT: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_action_next)); break;
		    	case PREV: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_action_previous)); break;
		    	case FORWARD: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_action_fast_forward)); break;
		    	case REWIND: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_action_rewind)); break;
		    	case SEEK_PROGRESS: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.seek_progress)); break;
		    	case SEEK_TRACK: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.seek_track)); break;
		    	case SELECT: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.select_track)); break;
				case UNDO: holder.function_iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_action_undo)); break;
		    	}
		    	holder.progress_tv.setText((event.getTrackno()+1) + " / " + Time.toString(event.getProgress()));
		    	holder.timestamp_tv.setText("@ " + event.getTimestamp().toString(Time.TimeStamp.DAY_TIME));
		    }

		    return convertView;
		}

		class ViewHolder {
			public ImageView function_iv;
			public TextView progress_tv, timestamp_tv;
		}
	}

}
