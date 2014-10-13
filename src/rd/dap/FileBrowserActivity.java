package rd.dap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.model.Data;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowserActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	private static final String TAG = "FileBrowserActivity";
	private static final String[] TYPE_IMAGE = {".jpg", ".png"};
	private static final String[] TYPE_AUDIO = {".mp3"};
	public static final String TYPE_FOLDER = "folder";
	private String type, message;
	private File root;
	private ArrayList<File> list;
	private FileAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_miniplayer);
		Log.d(TAG, "onCreate");

		type = getIntent().getStringExtra("type");
		if(type == null || type.isEmpty()) throw new RuntimeException("No type set");
		
		message = getIntent().getStringExtra("message");
		if(message == null || message.isEmpty()) throw new RuntimeException("No message set");

		String state = Environment.getExternalStorageState();
		if(!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//MEDIA_MOUNTED => read/write access, MEDIA_MOUNTED_READ_ONLY => read access
			throw new RuntimeException("No external storrage!");
		}

		root = Environment.getExternalStorageDirectory();

		list = new ArrayList<File>();
		for(File f : root.listFiles()){
			list.add(f);
		}

		ListView listview = (ListView) findViewById(R.id.list_layout_lv);
		LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		TextView v = (TextView) inflater.inflate(R.layout.file_browser_message, listview, false);
		v.setText(message);
		listview.addHeaderView(v);
		adapter = new FileAdapter(this, R.layout.file_browser_file_item, list);
		adapter.setRoot(root);
		adapter.setCurent(root);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(this);
		listview.setOnItemLongClickListener(this);
		
		FragmentManager fm = getFragmentManager();
		FragmentMiniPlayer miniplayer = (FragmentMiniPlayer) fm.findFragmentById(R.id.list_layout_mini_player);
		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);

		Log.d(TAG, "created");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= 1; //compensate for header
		File file = list.get(position);

		//Validate file
		boolean accept = false;
		String[] accepted = {};
		if("image".equalsIgnoreCase(type)) accepted = TYPE_IMAGE;
		if("audio".equalsIgnoreCase(type)) accepted = TYPE_AUDIO;
		for(String type : accepted){
			if(file.getPath().endsWith(type)) {
				accept = true; break;
			}
		}
		if(accept){
			int requestcode = getIntent().getIntExtra("requestcode", -1);
			if(requestcode > 0){
				Intent intent = new Intent();
				intent.putExtra("result", file.getAbsolutePath());
				setResult(requestcode, intent);
				finish();
			}

			//Go into folder
		} else if(file.isDirectory()){
			list.clear();
			if(!file.equals(root)){
				list.add(file.getParentFile());
			}
			for(File f : file.listFiles()){
				list.add(f);
			}
			adapter.setCurent(file);
			adapter.notifyDataSetChanged();
		}
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		position -= 1; //compensate for header
		Toast.makeText(FileBrowserActivity.this, "LongClick", Toast.LENGTH_SHORT).show();
		selectFolder(position);
		return true;
	}
	
	
	private void selectFolder(int position){
		if(TYPE_FOLDER.equalsIgnoreCase(type)){
			File file = list.get(position);
			int requestcode = getIntent().getIntExtra("requestcode", -1);
			if(requestcode > 0){
//				Toast.makeText(FileBrowserActivity.this, file.getName(), Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.putExtra("result", file.getAbsolutePath());
				setResult(requestcode, intent);
				finish();
			}
		}
	}

	class FileAdapter extends ArrayAdapter<File> {
		private List<File> list;
		private Context context;
		private File root, current;

		public FileAdapter(Context context, int resource, List<File> list) {
			super(context, resource, list);
			this.list = list;
			this.context = context;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			ViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.file_browser_file_item, parent, false);
				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

				holder = new ViewHolder();
				holder.name_tv = (TextView) convertView.findViewById(R.id.file_item_name_tv);
				holder.type_iv = (ImageView) convertView.findViewById(R.id.file_item_type_iv);
				holder.cb = (CheckBox) convertView.findViewById(R.id.file_item_cb);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			File file = list.get(position);

			if(!current.equals(root)){
				holder.name_tv.setText(position == 0? ".." : file.getName());
			} else {
				holder.name_tv.setText(file.getName());
			}
			Drawable ic = null;

			if(file.getPath().endsWith(".mp3")){
				ic = context.getResources().getDrawable(R.drawable.ic_action_headphones);
			} else if(file.getPath().endsWith(".jpg")){
				ic = context.getResources().getDrawable(R.drawable.ic_action_picture);
			} else if(file.isDirectory()){
				ic = context.getResources().getDrawable(R.drawable.ic_action_collection);
			}
			if(ic != null) 	holder.type_iv.setImageDrawable(ic);

			if(TYPE_FOLDER.equalsIgnoreCase(type)){
				holder.cb.setVisibility(View.VISIBLE);
				holder.cb.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectFolder(position);
					}
				});
			} else {
				holder.cb.setVisibility(View.GONE);
			}
			
			return convertView;
		}

		public void setCurent(File current){
			this.current = current;
		}

		public void setRoot(File root) {
			this.root = root;
		}

	}
	static class ViewHolder {
		public TextView name_tv;
		public ImageView type_iv;
		public CheckBox cb;
	}
	
	
}
