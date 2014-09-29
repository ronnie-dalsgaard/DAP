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
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextInputDialog extends DialogFragment {
	private ArrayList<String> authors = new ArrayList<String>();
	
	public TextInputDialog(){
		for(int i = 0; i < 10; i++){
		authors.add("Dennis Jürgensen");
		authors.add("Rick Riordan");
		authors.add("John G. Hemry");
		}
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Translucent);
        builder.setTitle("Title");
        View v = inflater.inflate(R.layout.edittext_and_list, null, false);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.et_list);
        final EditText et = (EditText) v.findViewById(R.id.et_list_et);
        
        for(final String author : authors){
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
        return builder.create();
	}
}
