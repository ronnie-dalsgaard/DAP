package rd.dap.dialogs;

import static rd.dap.model.BookmarkManager.END;

import java.util.ArrayList;
import java.util.LinkedList;

import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.model.BookmarkManager;
import rd.dap.model.GenericCallback;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class Dialog_import_export extends CustomDialog {
	private Callback callback;
	
	public interface Callback {
		public void download(GenericCallback<String> callback);
		public void upload(String json, GenericCallback<String> callback);
		public void displayBookmarks(ArrayList<Bookmark> bookmarks);
	}

	public Dialog_import_export(Activity activity, ViewGroup parent, Callback callback) {
		super(activity, parent);
		this.callback = callback;
	}
	
	public void show(){
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_import_export, parent, false);

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
				for(Bookmark bookmark : BookmarkManager.getInstance().getBookmarks()){
					String line = gson.toJson(bookmark) + END + "\n";
					json += line;
				}
				callback.upload(json, new GenericCallback<String>() {
					@Override
					public void onResult(final String result) {
						Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
					}
				}); 
				dialog.dismiss();
			}
		});

		//Download
		ImageButton down_btn = (ImageButton) dv.findViewById(R.id.dialog_impexp_download);
		down_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.download(new GenericCallback<String>() { 
					@Override public void onResult(String result) {
						System.out.println("----- DOWNLOAD COMPLETE -----");
						if(result == null || result.isEmpty() || result.contains(NO_FILE)){
							Toast.makeText(activity, "Bad result: \"" + result + "\"", Toast.LENGTH_LONG).show();
							return;
						}
						BookmarkManager bm = BookmarkManager.getInstance();
						AudiobookManager am = AudiobookManager.getInstance();
						Gson gson = new Gson();
						boolean changesHappened = false;
						for(String line : result.split(END)){
							System.out.println("Line = "+line);
							Bookmark fetched = gson.fromJson(line, Bookmark.class);
							Audiobook fetchedAudiobook = am.getAudiobook(fetched);
							System.out.println("Fetched audiobook: "+fetchedAudiobook);
							if(bm.hasBookmark(fetched)){
								System.out.println("Has bookmark");
								Bookmark exisisting = bm.getBookmark(fetched.getAuthor(), fetched.getAlbum());
								if(fetched.compareTo(exisisting) > 0){
									exisisting.setTrackno(fetched.getTrackno());
									exisisting.setProgress(fetched.getProgress());
									LinkedList<BookmarkEvent> events = new LinkedList<BookmarkEvent>();
									events.addAll(fetched.getEvents());
									exisisting.setEvents(events);
									exisisting.addEvent(new BookmarkEvent(BookmarkEvent.Function.DOWNLOAD, fetched.getTrackno(), fetched.getProgress()));
									changesHappened = true;
								}
							} else if(fetchedAudiobook != null){
								System.out.println("No bookmark, but matching audiobook exists...");
								/* Bookmark b = */ bm.createOrUpdateBookmark(activity.getFilesDir(), fetched, false);
								changesHappened = true;
							}
						}
						if(changesHappened){
							callback.displayBookmarks(bm.getBookmarks());
						}
						Toast.makeText(activity, "Download complete", Toast.LENGTH_SHORT).show();
					}
				});
				dialog.dismiss();
			}
		});

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new ExitListener());

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("Cancel");
		right_btn.setOnClickListener(new ExitListener());

		dialog.setContentView(dv);
		dialog.show();
	}

}
