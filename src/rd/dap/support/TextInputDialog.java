package rd.dap.support;

import java.util.ArrayList;

import rd.dap.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class TextInputDialog extends DialogFragment {
	private ArrayList<String> list = new ArrayList<String>();
	private Dialog dialog;
	private String title, message, value;
	
	public TextInputDialog(String title, String message, String value, ArrayList<String> list){
		this.title = title;
		this.message = message;
		this.value = value;
		this.list.addAll(list); //Defensice copy
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Translucent);
        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);
        View v = inflater.inflate(R.layout.edittext_and_list, null, false);
        final LinearLayout layout = (LinearLayout) v.findViewById(R.id.et_list);
        final EditText et = (EditText) v.findViewById(R.id.et_list_et);
        if(value != null && !value.isEmpty()) et.setText(value);
        
        for(final String author : list){
        	TextView child = new TextView(getActivity());
        	child.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        	int small = Math.round(getActivity().getResources().getDimension(R.dimen.margin_small));
        	int big = Math.round(getActivity().getResources().getDimension(R.dimen.margin_big));
        	child.setPadding(big, small, big, small);
        	child.setTextColor(getActivity().getResources().getColor(R.color.white));
        	child.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					et.setText(author);
				}
			});
        	child.setText(author);
        	layout.addView(child);
        }
        
        builder.setView(v);
        dialog = builder.create();
        
        ImageButton btn = (ImageButton) v.findViewById(R.id.et_list_btn);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				setResult(et.getText().toString());
			}
		});
        
        return dialog;
	}
	
	public abstract void setResult(String result);
}
