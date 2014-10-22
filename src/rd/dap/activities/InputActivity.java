package rd.dap.activities;

import java.util.ArrayList;
import java.util.List;

import rd.dap.R;
import rd.dap.R.id;
import rd.dap.R.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class InputActivity extends Activity {
	public static final int REQUEST_EDIT_AUTHOR = 1200;
	public static final int REQUEST_EDIT_ALBUM = 1201;
	public static final int REQUEST_EDIT_COVER = 1202;
	public static final int REQUEST_EDIT_TRACK_TITLE = 1203;
	public static final int REQUEST_EDIT_TRACK_FILE = 1204;
	
	private int requestCode;
	private ArrayList<String> list; 
	private EditText et;
	private InputAdapter adapter;

	
	@Override @SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input);
		
		requestCode = getIntent().getIntExtra("requestcode", -1);
		if(requestCode == -1) throw new RuntimeException("InputActivity - No requestcode supplied");
		
		list = (ArrayList<String>) getIntent().getSerializableExtra("list");
		if(list == null) throw new RuntimeException("InputActivity - No list supplied");
		
		et = (EditText) findViewById(R.id.input_et);

		String value = getIntent().getStringExtra("value");
		if(value != null && !value.isEmpty())
			et.setText(value);
		
		adapter = new InputAdapter(this, R.id.input_item_tv, list);
		
		ListView lv = (ListView) findViewById(R.id.input_lv);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				 et.setText(list.get(position));
				
			}
		});
		
		ImageButton btn = (ImageButton) findViewById(R.id.input_btn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("result", et.getText().toString());
				InputActivity.this.setResult(requestCode, intent);
				finish();
			}
		});
		
	}
	
	private class InputAdapter extends ArrayAdapter<String>{
		private List<String> list;
		
		public InputAdapter(Context context, int resource, List<String> list) {
			super(context, resource, list);
			this.list = list;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			ViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.input_item, parent, false);
				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.
				
				holder = new ViewHolder();
				holder.input_et = (TextView) convertView.findViewById(R.id.input_item_tv);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.input_et.setText(list.get(position));
			return convertView;
		}
	}
	static class ViewHolder{
		TextView input_et;
	}
}
