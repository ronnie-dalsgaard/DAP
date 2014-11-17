package rd.dap.dialogs;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import static rd.dap.activities.MainActivity.*;

import rd.dap.R;
import rd.dap.activities.MainActivity;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Callback;
import rd.dap.model.Data;

public class Dialog_import_export {
	private static final String TAG = "Dialog_import_export";
	private MainActivity activity;
	private RelativeLayout base;

	public Dialog_import_export(MainActivity activity) {
		this.activity = activity;
		this.base = activity.getBase();
	}
	
	public void show(){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_import_export, base, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Upload or Download");

		//Upload
		ImageButton up_btn = (ImageButton) dv.findViewById(R.id.dialog_impexp_upload);
		up_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gson gson = new Gson();
				String json = "";
				for(Bookmark bookmark : Data.getBookmarks()){
					json += gson.toJson(bookmark) + END + "\n";
				}
				Log.d(TAG, "onClick - upload: "+json);
				activity.upload(json); 
				dialog.dismiss();
			}
		});

		//Download
		ImageButton down_btn = (ImageButton) dv.findViewById(R.id.dialog_impexp_download);
		down_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.download(new Callback<String>() { 
					@Override public void onResult(String result) {
						Log.d(TAG, "onClick - download: "+result);
						if(result == null || result.isEmpty()) return;
						BookmarkManager bm = BookmarkManager.getInstance();
						AudiobookManager am = AudiobookManager.getInstance();
						Gson gson = new Gson();
						boolean changesHappened = false;
						for(String line : result.split(END)){
							System.out.println("Line = "+line);
							Bookmark fetched = gson.fromJson(line, Bookmark.class);
							Audiobook fetchedAudiobook = am.getAudiobook(fetched);
							if(bm.hasBookmark(fetched)){
								Bookmark exisisting = bm.getBookmark(fetched.getAuthor(), fetched.getAlbum());
								System.out.println("Bookmark:\n"+exisisting);
								System.out.println("Fetched:\n"+fetched);
								System.out.println("fetched.compareTo(existing) = "+fetched.compareTo(exisisting));

								if(fetched.compareTo(exisisting) > 0){
									exisisting.setTrackno(fetched.getTrackno());
									exisisting.setProgress(fetched.getProgress());
									changesHappened = true;
								}
							} else if(fetchedAudiobook != null){
								bm.createOrUpdateBookmark(activity.getFilesDir(), fetched, false);
								changesHappened = true;
							}
						}
						if(changesHappened){
							activity.displayBookmarks();
						}
					}
				});
				dialog.dismiss();
			}
		});

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("Cancel");
		right_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}

}
