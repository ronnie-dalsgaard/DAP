package rd.dap.activities;

import java.io.File;
import java.util.ArrayList;

import rd.dap.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FileBrowserActivity extends Activity implements OnClickListener {
	public static final String[] TYPE_IMAGE_FILES = {".jpg", ".png"};
	public static final String[] TYPE_AUDIO_FILES = {".mp3"};
	public static final String TYPE_FOLDER = "folder";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_AUDIO = "audio";
	private String type, message;
	private File rootFolder;
	private ArrayList<File> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ScrollView scroller = new ScrollView(this);
		LinearLayout l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		scroller.addView(l);
		setContentView(scroller);

		type = getIntent().getStringExtra("type");
		if(type == null || type.isEmpty()) throw new RuntimeException("No type set");

		message = getIntent().getStringExtra("message");
		if(message == null || message.isEmpty()) throw new RuntimeException("No message set");

		String state = Environment.getExternalStorageState();
		if(!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//MEDIA_MOUNTED => read/write access, MEDIA_MOUNTED_READ_ONLY => read access
			throw new RuntimeException("No external storrage!");
		}

		rootFolder = Environment.getExternalStorageDirectory();

		LayoutInflater inflater = LayoutInflater.from(this);
		TextView v = (TextView) inflater.inflate(R.layout.activity_file_browser_message, l, false);
		v.setText(message);
		l.addView(v);

		list = new ArrayList<File>();
		for(File f : rootFolder.listFiles()){
			list.add(f);
		}

		for(File file : list){
			addChildren(file, l);
		}
	}

	@SuppressLint("InflateParams")
	private void addChildren(File file, LinearLayout l){
		LayoutInflater inflater = LayoutInflater.from(this);
		View item_layout = inflater.inflate(R.layout.activity_file_browser_item, null, false);
		//Must pass null as parent!

		//Expand
		ImageView expand_iv = (ImageView) item_layout.findViewById(R.id.file_browser_item_expand);
		if(file.isDirectory()){
			LinearLayout content = (LinearLayout) item_layout.findViewById(R.id.file_browser_item_content);
			expand_iv.setTag(new TagBundle(file, content, expand_iv, TagBundle.INIT));
			expand_iv.setOnClickListener(this);
		} else {
			expand_iv.setVisibility(View.GONE);			
		}

		//File name
		TextView name_tv = (TextView) item_layout.findViewById(R.id.file_browser_item_name);
		name_tv.setText(file.getName());
		
		//Icon
		ImageView icon_iv = (ImageView) item_layout.findViewById(R.id.file_browser_item_icon);
		Drawable icon = null;
		if(file.getPath().endsWith(".mp3")){
			icon = getResources().getDrawable(R.drawable.ic_action_headphones);
		} else if(file.getPath().endsWith(".jpg")){
			icon = getResources().getDrawable(R.drawable.ic_action_picture);
		} else if(file.isDirectory()){
			icon = getResources().getDrawable(R.drawable.ic_action_collection);
		}
		if(icon != null) icon_iv.setImageDrawable(icon);
		
		//Select item
		boolean addlistener = false;
		if(TYPE_FOLDER.equalsIgnoreCase(type) && file.isDirectory()){
			addlistener = true;
		} else if(TYPE_AUDIO.equalsIgnoreCase(type)){
			for(String s : TYPE_AUDIO_FILES){
				if(file.getName().endsWith(s)) addlistener = true;
			}
		} else if(TYPE_IMAGE.equalsIgnoreCase(type)){
			for(String s : TYPE_IMAGE_FILES){
				if(file.getName().endsWith(s)) addlistener = true;
			}
		}
		
		if(addlistener){
			name_tv.setTag(file);
			name_tv.setOnClickListener(this);
			icon_iv.setTag(file);
			icon_iv.setOnClickListener(this);
		}
		
		l.addView(item_layout);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.file_browser_item_expand: click_expand(v); break;
		case R.id.file_browser_item_icon: //fall through
		case R.id.file_browser_item_name: click_item(v); break;
		}
	}
	
	private void click_item(View v){
		File file = (File)v.getTag();
		int requestcode = getIntent().getIntExtra("requestcode", -1);
		Intent intent = new Intent();
		intent.putExtra("result", file.getAbsolutePath());
		setResult(requestcode, intent);
		finish();
	}

	private void click_expand(View v){
		TagBundle b = (TagBundle) v.getTag();

		switch(b.state){
		case TagBundle.INIT:
			for(File f : b.file.listFiles()){
				addChildren(f, b.l);
			}
			b.expand.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_expand));
			b.state = TagBundle.OPEN;
			break;
		case TagBundle.OPEN:
			b.l.setVisibility(View.GONE);
			b.expand.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_next_item));
			b.state = TagBundle.CLOSED;
			break;
		case TagBundle.CLOSED:
			b.l.setVisibility(View.VISIBLE);
			b.expand.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_expand));
			b.state = TagBundle.OPEN;
			break;
		}
	}

	private class TagBundle {
		static final int INIT = 0;
		static final int OPEN = 1;
		static final int CLOSED = 2;

		public File file;
		public LinearLayout l;
		public ImageView expand;
		public int state;
		public TagBundle(File file, LinearLayout l, ImageView expand, int state){
			this.file = file;
			this.l = l;
			this.expand = expand;
			this.state = state;
		}
	}
}
