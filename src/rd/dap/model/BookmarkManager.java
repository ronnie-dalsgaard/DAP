package rd.dap.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BookmarkManager { //Singleton
	private static BookmarkManager instance = new BookmarkManager();
	private ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
	
	private BookmarkManager() { }
	public static BookmarkManager getInstance() { return instance; }
	
	//CRUD bookmarks
	public boolean createOrUpdateBookmark(File filesDir, String author, String album, int trackno, int progress){
		boolean found = false;
		for(Bookmark bookmark : bookmarks){
			if(bookmark.matches(author, album)){
				bookmark.setTrackno(trackno);
				bookmark.setProgress(progress);
				found = true;
				break;
			}
		}
		if(!found){
			Bookmark bookmark = new Bookmark(author, album, trackno, progress);
			bookmarks.add(bookmark);
		}
		return saveBookmarks(filesDir);
	}
	public Bookmark getBookmark(File filesDir, String author, String album){
		loadBookmarks(filesDir);
		for(Bookmark bookmark : bookmarks){
			if(bookmark.matches(author, album)) 
				return bookmark;
		}
		return null;
	}
	public Bookmark getBookmark(File filesDir, Audiobook audiobook){ //Overloading
		return getBookmark(filesDir, audiobook.getAuthor(), audiobook.getAlbum());
	}
	public boolean deleteBookmark(File filesDir, String author, String album){
		/*
		 * When comparing bookmarks, trackno and progress is ignored
		 */
		Bookmark delete = new Bookmark(author, album, 0, 0);
		boolean result = bookmarks.remove(delete);
		return result && saveBookmarks(filesDir);
	}
	
	//Load and save bookmarks
	public void loadBookmarks(File filesDir){
		File file = new File(filesDir, "bookmarks.dap");
		try {
			FileInputStream stream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(reader);
			Gson gson = new Gson();
			ArrayList<Bookmark> list = gson.fromJson(in, new TypeToken<ArrayList<Bookmark>>(){}.getType());
			bookmarks.clear();
			bookmarks.addAll(list);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean saveBookmarks(File filesDir){
		Gson gson = new Gson();
		String json = gson.toJson(bookmarks);
		System.out.println(json);
		
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
