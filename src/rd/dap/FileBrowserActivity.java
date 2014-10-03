package rd.dap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowserActivity extends Activity {
	private static final String TAG = "FileBrowserActivity";
	private static final String[] TYPE_IMAGE = {".jpg", ".png"};
	private static final String[] TYPE_AUDIO = {".mp3"};
	private String type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.d(TAG, "onCreate");
		
		type = getIntent().getStringExtra("type");
		
		String state = Environment.getExternalStorageState();
		if(!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//MEDIA_MOUNTED => read/write access, MEDIA_MOUNTED_READ_ONLY => read access
			throw new RuntimeException("No external storrage!");
		}

		final File root = Environment.getExternalStorageDirectory();
		
		final ArrayList<File> list = new ArrayList<File>();
		for(File f : root.listFiles()){
			list.add(f);
		}
		
		ListView listview = (ListView) findViewById(R.id.main_list);
		final FileAdapter adapter = new FileAdapter(this, R.layout.file_browser_file_item, list);
		adapter.setRoot(root);
		adapter.setCurent(root);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
		});
		
		Log.d(TAG, "created");
	}

	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy");
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
				holder.path_tv = (TextView) convertView.findViewById(R.id.file_item_path_tv);
				holder.type_iv = (ImageView) convertView.findViewById(R.id.file_item_type_iv);
				holder.selecte_iv = (ImageView) convertView.findViewById(R.id.file_item_select_iv);
				
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
			holder.path_tv.setText(file.getAbsolutePath());
			Drawable ic = null;
			
			System.out.println(file.getPath() + " ::: " + file.isDirectory());
			if(file.getPath().endsWith(".mp3")){
				ic = context.getResources().getDrawable(R.drawable.ic_action_headphones);
			} else if(file.getPath().endsWith(".jpg")){
				ic = context.getResources().getDrawable(R.drawable.ic_action_picture);
			} else if(file.isDirectory()){
				ic = context.getResources().getDrawable(R.drawable.ic_action_collection);
			}
			if(ic != null) 	holder.type_iv.setImageDrawable(ic);
			
			holder.selecte_iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					System.out.println("Click on "+position);
					
				}
			});
			
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
		public TextView name_tv, path_tv;
		public ImageView type_iv, selecte_iv;
	}
}
