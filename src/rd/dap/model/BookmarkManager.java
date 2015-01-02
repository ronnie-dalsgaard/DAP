package rd.dap.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BookmarkManager{ //Singleton
	private static final String TAG = "BookmarkManager";
	private static BookmarkManager instance = new BookmarkManager();
	private ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
	
	private BookmarkManager() { }
	public static BookmarkManager getInstance() { return instance; }
	
	//CRUD bookmarks
	public ArrayList<Bookmark> getBookmarks() { return bookmarks; }
	public Bookmark createOrUpdateBookmark(File filesDir, Bookmark bookmark, boolean force){
		String author = bookmark.getAuthor();
		String album = bookmark.getAlbum();
		int trackno = bookmark.getTrackno();
		int progress = bookmark.getProgress();
		return createOrUpdateBookmark(filesDir, author, album, trackno, progress, force);
	}
	public Bookmark createOrUpdateBookmark(File filesDir, String author, String album, int trackno, int progress, boolean force){
		boolean found = false;
		Bookmark result = null;
		for(Bookmark bookmark : bookmarks){
			if(bookmark.isSame(author, album)){
				result = bookmark;
				found = true;
				if(force){ //set bookmark no matter the trackno and progress
					bookmark.setTrackno(trackno);
					bookmark.setProgress(progress);
					break;
				}
				
				if(trackno < bookmark.getTrackno()){
					break;
				} else if(trackno > bookmark.getTrackno()){
					bookmark.setTrackno(trackno);
					bookmark.setProgress(progress);
				} else if(progress > bookmark.getProgress()){
					bookmark.setProgress(progress);
				}
				break;
			}
		}
		if(!found){
			Bookmark bookmark = new Bookmark(author, album, trackno, progress);
			bookmarks.add(bookmark);
			result = bookmark;
		}
		return saveBookmarks(filesDir) ? result : null;
	}
	public Bookmark getBookmark(String author, String album){
		for(Bookmark bookmark : bookmarks){
			if(bookmark.isSame(author, album)) 
				return bookmark;
		}
		return null;
	}
	public Bookmark getBookmark(Audiobook audiobook){ //Overloading
		return getBookmark(audiobook.getAuthor(), audiobook.getAlbum());
	}
	public boolean removeBookmark(Context context, String author, String album){
		if(author == null || album == null) return false;
		Bookmark delete = null;
		for(Bookmark bookmark : bookmarks){
			if(author.equals(bookmark.getAuthor()) && album.equals(bookmark.getAlbum())){
				delete = bookmark;
			}
		}
		if(delete != null){
			boolean result = bookmarks.remove(delete);
			saveBookmarks(context.getFilesDir());
			return result;
		}
		return false;
	}
	public boolean removeBookmark(Context context, Bookmark delete) { //Overloading
		if(delete == null) return false;
		return removeBookmark(context, delete.getAuthor(), delete.getAlbum());
	}
	public boolean hasBookmark(Bookmark bookmark){
		if(bookmark == null) return false;
		for(Bookmark b : bookmarks){
			if(bookmark.isSame(b)) return true;
		}
		return false;
	}
	
	//Load and save bookmarks
	public ArrayList<Bookmark> loadBookmarks(File filesDir){
		Log.d(TAG, "loadBookmarks");
		File file = new File(filesDir, "bookmarks.dap");
		try {
			FileInputStream stream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(reader);
			Gson gson = new Gson();
			ArrayList<Bookmark> list = gson.fromJson(in, new TypeToken<ArrayList<Bookmark>>(){}.getType());
			if(list == null) list = new ArrayList<Bookmark>();
			bookmarks.clear();
			bookmarks.addAll(list);
			in.close();
			return bookmarks;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public boolean saveBookmarks(File filesDir){
		Log.d(TAG, "saveBookmarks");
		Gson gson = new Gson();
		String json = gson.toJson(bookmarks);
		
		//create a file in internal storage
		File file = new File(filesDir, "bookmarks.dap"); //FIXME filename as constant
		try {
			FileWriter writer = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(json);
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
