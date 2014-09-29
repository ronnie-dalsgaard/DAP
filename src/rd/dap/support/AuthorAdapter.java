package rd.dap.support;

import java.util.List;

import rd.dap.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AuthorAdapter extends ArrayAdapter<String>{
	public static final int TYPE_COUNT = 2, TYPE_INPUT=0, TYPE_NAME=1;
	
	private List<String> authors;

	public AuthorAdapter(Context context, int resource, List<String> authors) {
		super(context, resource, authors);
		this.authors = authors;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		switch(type){
		case TYPE_INPUT:
			InputViewHolder inputViewHolder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				inputViewHolder = new InputViewHolder();
				convertView = inflater.inflate(R.layout.input_item, parent, false);
				inputViewHolder.input_item_et = (EditText) convertView.findViewById(R.id.input_item_et);
				inputViewHolder.input_item_btn = (ImageButton) convertView.findViewById(R.id.input_item_btn);
				
				
				inputViewHolder.input_item_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				    @Override
				    public void onFocusChange(View v, boolean hasFocus) {
				        if (hasFocus) {
				        	Toast.makeText(getContext(), "EDIT_TEXT", Toast.LENGTH_SHORT).show();
				            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
				        }
				    }
				});
				
				convertView.setTag(inputViewHolder);
			} else {
				inputViewHolder = (InputViewHolder) convertView.getTag();
			}
			inputViewHolder.input_item_et.setText("Hello World!");
			
			break;
		case TYPE_NAME:
			TextView tv;
			if(convertView == null){
				tv = new TextView(getContext());
				tv.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
				int padding = (int) getContext().getResources().getDimension(R.dimen.margin_small);
				tv.setPadding(padding, padding, padding, padding);
				tv.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
				tv.setTextColor(getContext().getResources().getColor(R.color.white));
			} else {
				tv = (TextView) convertView;
			}
			tv.setText(authors.get(position));
			convertView = tv;
			
			break;
		}
		
		return convertView;
	}
	
	@Override
	public int getItemViewType(int position) {
		if(position == authors.size()) return TYPE_INPUT;
		else return TYPE_NAME;
	}

	@Override
	public int getViewTypeCount() { return TYPE_COUNT; }
	
	@Override
    public int getCount() { return authors.size() + 1; }
	
	static class InputViewHolder {
		public EditText input_item_et;
		public ImageButton input_item_btn;
	}
}
