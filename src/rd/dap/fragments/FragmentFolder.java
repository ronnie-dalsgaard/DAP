package rd.dap.fragments;

import java.io.File;
import java.util.ArrayList;

import rd.dap.FileBrowserActivity;
import rd.dap.R;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class FragmentFolder extends Fragment implements OnClickListener {
	private final String TAG = "Folder_Fragment";
	private static final int REQUEST_CODE = 20001;
	private TextView folder_tv;
	private CheckBox subfolders_cb;
	
	private ArrayList<Folder_Fragment_Observer> observers = new ArrayList<Folder_Fragment_Observer>();
	public interface Folder_Fragment_Observer{
		public void folder_fragment_click();
		public void folder_fragment_folder_selected(File folder, boolean incl_subfolders);
	}
	public void addObserver(Folder_Fragment_Observer observer) { observers.add(observer); }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.fragment_folder, container, false);

		folder_tv = (TextView) v.findViewById(R.id.folder_fragment_tv);
		folder_tv.setOnClickListener(this);
		
		subfolders_cb = (CheckBox) v.findViewById(R.id.folder_fragment_subfolder_cb);
		subfolders_cb.setOnClickListener(this);
		
//		tracklist = (LinearLayout) v.findViewById(R.id.new_audiobook_tracklist);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.folder_fragment_tv:
			for(Folder_Fragment_Observer observer : observers){
				observer.folder_fragment_click();
			}
			Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
			intent.putExtra("type", FileBrowserActivity.TYPE_FOLDER);
			intent.putExtra("message", "Select album folder");
			intent.putExtra("requestcode", REQUEST_CODE);
			startActivityForResult(intent, REQUEST_CODE);
			break;
		case R.id.folder_fragment_subfolder_cb:
			
			
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
		case REQUEST_CODE:
			if(data == null) return;
			String result = data.getStringExtra("result");
			File folder = new File(result);
			for(Folder_Fragment_Observer observer : observers){
				observer.folder_fragment_folder_selected(folder, subfolders_cb.isChecked());
			}
			break;
		}
	}

	public void setFolderName(String foldername){
		if(folder_tv == null) return;
		folder_tv.setText(foldername);
	}
}
