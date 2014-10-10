package rd.dap;

import java.io.File;

import rd.dap.fragments.FragmentFolder;
import rd.dap.fragments.FragmentFolder.Folder_Fragment_Observer;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class NewAudiobookActivity extends Activity implements Folder_Fragment_Observer {
	private FragmentFolder folderFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_audiobook);
				
		FragmentManager fm = getFragmentManager();
		folderFragment = (FragmentFolder) fm.findFragmentById(R.id.new_audiobook_folder_fragment);
		folderFragment.addObserver(this);
	}

	@Override
	public void folder_fragment_click() {
		//Do nothing
	}

	@Override
	public void folder_fragment_folder_selected(File folder) {
		if(folderFragment == null) return;
		folderFragment.setFolderName(folder.getName());
	}

}
