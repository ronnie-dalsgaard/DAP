package rd.dap.support;

import java.io.File;
import java.util.List;

import rd.dap.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<File> {
	private List<File> list;
	private Context context;
	
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
			convertView = inflater.inflate(R.layout.file_item, parent, false);
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

		holder.name_tv.setText(file.getName());
		holder.path_tv.setText(file.getAbsolutePath());
		Drawable ic = null;
		if(file.isDirectory()){
			ic = context.getResources().getDrawable(R.drawable.ic_action_collection);
		} else if(file.getName().endsWith(".mp3")){
			ic = context.getResources().getDrawable(R.drawable.ic_action_headphones);
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
	
	static class ViewHolder {
		public TextView name_tv, path_tv;
		public ImageView type_iv, selecte_iv;
	}

}
