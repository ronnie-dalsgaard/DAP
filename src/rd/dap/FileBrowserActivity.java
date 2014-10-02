package rd.dap;

import java.io.File;
import java.util.ArrayList;

import rd.dap.support.FileAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FileBrowserActivity extends Activity {
	private static final String TAG = "FileBrowserActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_browser);
		Log.d(TAG, "onCreate");
		
		String state = Environment.getExternalStorageState();
		if(!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//MEDIA_MOUNTED => read/write access, MEDIA_MOUNTED_READ_ONLY => read access
			throw new RuntimeException("No external storrage!");
		}

		final File root = Environment.getExternalStorageDirectory();
		System.out.println("Root: "+root.getAbsolutePath());
		
		final ArrayList<File> list = new ArrayList<File>();
		for(File f : root.listFiles()){
			list.add(f);
		}
		
		ListView listview = (ListView) findViewById(R.id.file_browser_listview);
		final FileAdapter adapter = new FileAdapter(this, R.layout.file_browser_file_item, list);
		adapter.setRoot(root);
		adapter.setCurent(root);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				File file = list.get(position);
				if(file.getPath().endsWith("jpg")){
					int requestcode = getIntent().getIntExtra("requestcode", -1);
					if(requestcode > 0){
						Intent intent = new Intent();
						intent.putExtra("result", file.getAbsolutePath());
						setResult(requestcode, intent);
						finish();
					}
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
	
}
