package rd.dap.fragments;

import rd.dap.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

	public class Picker extends Fragment {
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_picker, container, false); 
	        return view;
	    }
	}
