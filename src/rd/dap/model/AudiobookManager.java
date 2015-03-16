package rd.dap.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import rd.dap.support.AlbumFolderFilter;
import rd.dap.support.Mp3FileFilter;
import rd.dap.support.TrackList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class AudiobookManager{
	private static final String TAG = "AudiobookManager";
	private static AudiobookManager instance = new AudiobookManager();
	private static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	private static HashSet<String> authors = new HashSet<String>();
	private static HashSet<String> albums = new HashSet<String>();

	public static AudiobookManager getInstance(){
		return instance; //Eager singleton
	}

	private AudiobookManager(){};

	//CRUD Audiobook
	public void addAudiobook(Context context, Audiobook audiobook){
		if(audiobooks.contains(audiobook)) return;
		audiobooks.add(audiobook);
		authors.add(audiobook.getAuthor());
		albums.add(audiobook.getAlbum());
		saveAudiobooks(context);
	}
	public void addAllAudiobooks(Context context, Collection<Audiobook> collection){
		for(Audiobook audiobook : collection){
			if(audiobooks.contains(audiobook)) continue;
			audiobooks.add(audiobook);
			authors.add(audiobook.getAuthor());
			albums.add(audiobook.getAlbum());
		}
		saveAudiobooks(context);
	}
	public Audiobook getAudiobook(String author, String album){
		if(author == null) return null;
		if(album == null) return null;
		for(Audiobook audiobook : audiobooks){
			if(author.equals(audiobook.getAuthor()) 
					&& album.equals(audiobook.getAlbum())){
				return audiobook;
			}
		}
		return null;
	}
	public Audiobook getAudiobook(Bookmark bookmark){
		if(bookmark == null) return null;
		String author = bookmark.getAuthor();
		String album = bookmark.getAlbum();
		return getAudiobook(author, album);
	}
	public ArrayList<Audiobook> getAudiobooks(){ return audiobooks; }
	public void updateAudiobook(Context context, Audiobook audiobook, Audiobook original_audiobook) {
		for(Audiobook element : getAudiobooks()){
			if(element.equals(original_audiobook)){
				element.setAudiobook(audiobook);

				authors.remove(original_audiobook.getAuthor());
				authors.add(audiobook.getAuthor());
			}
		}
		saveAudiobooks(context);
	}
	public ArrayList<Audiobook> removeAudiobook(Context context, Audiobook audiobook) { 
		audiobooks.remove(audiobook);
		authors.remove(audiobook.getAuthor());
		saveAudiobooks(context);
		return audiobooks;
	}
	public void removeAllAudiobooks(Context context){
		ArrayList<Audiobook> trash = new ArrayList<Audiobook>();
		trash.addAll(audiobooks);
		for(Audiobook audiobook : trash){
			audiobooks.remove(audiobook);
			authors.remove(audiobook.getAuthor());
		}
		saveAudiobooks(context);
	}


	//Load and save
	public void saveAudiobooks(Context context){
		Log.d(TAG, "saveAudiobooks");
		Gson gson = new Gson();
		String json = gson.toJson(audiobooks);

		//create a file in internal storage
		File file = new File(context.getFilesDir(), "audiobooks.dap"); //FIXME filename as constant
		try {
			FileWriter writer = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(json);
			out.close();
		} catch (IOException e) {
			Toast.makeText(context, "Unable to save audiobooks", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}


		//Save authors
		String authors_json = gson.toJson(authors);
		File authors_file = new File(context.getFilesDir(), "authors.dap"); //FIXME filename as constant
		try {
			FileWriter writer = new FileWriter(authors_file, false);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(authors_json);
			out.close();
		} catch (IOException e) {
			Toast.makeText(context, "Unable to save authors", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		//Save albums
		String albums_json = gson.toJson(albums);
		File albums_file = new File(context.getFilesDir(), "albums.dap"); //FIXME filename as constant
		try {
			FileWriter writer = new FileWriter(albums_file, false);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(albums_json);
			out.close();
		} catch (IOException e) {
			Toast.makeText(context, "Unable to save albums", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	public ArrayList<Audiobook> loadAudiobooks(Context context){
		Log.d(TAG, "loadAudiobooks");

		//Audiobooks
		File file = new File(context.getFilesDir(), "audiobooks.dap");
		try {
			FileInputStream stream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(reader);
			Gson gson = new Gson();
			ArrayList<Audiobook> list = gson.fromJson(in, new TypeToken<ArrayList<Audiobook>>(){}.getType());
			audiobooks.clear();
			audiobooks.addAll(list);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Authors
		File authors_file = new File(context.getFilesDir(), "authors.dap");
		try {
			FileInputStream stream = new FileInputStream(authors_file);
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(reader);
			Gson gson = new Gson();
			HashSet<String> set = gson.fromJson(in, new TypeToken<HashSet<String>>(){}.getType());
			authors.clear();
			authors.addAll(set);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Albums
		File albums_file = new File(context.getFilesDir(), "albums.dap");
		try {
			FileInputStream stream = new FileInputStream(albums_file);
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(reader);
			Gson gson = new Gson();
			HashSet<String> set = gson.fromJson(in, new TypeToken<HashSet<String>>(){}.getType());
			albums.clear();
			albums.addAll(set);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return audiobooks;
	}

	//Auto-detect
	private Audiobook autoCreateAudiobook(File album_folder, File home_folder, boolean incl_subfolders){
		Audiobook audiobook = new Audiobook();

		File folder = album_folder;
		LinkedList<String> tokens = new LinkedList<String>();
		while(!folder.getAbsolutePath().equalsIgnoreCase(home_folder.getAbsolutePath())){
			tokens.addFirst(folder.getName());
			folder = folder.getParentFile();
		}
		String _author = tokens.removeFirst();
		if(tokens.size() > 1){
			String _series = tokens.getFirst();
			audiobook.setSeries(_series);
		}
		String _album = tokens.removeFirst();
		while(!tokens.isEmpty()) { _album += " " + tokens.removeFirst(); }

		audiobook.setAuthor(_author);
		audiobook.setAlbum(_album);

		String cover = null;
		for(File file : album_folder.listFiles()){
			if("albumart.jpg".equalsIgnoreCase(file.getName())){
				cover = file.getAbsolutePath();
				break;
			}
		}
		if(cover != null) { 
			audiobook.setCover(cover);
			Bitmap bm = BitmapFactory.decodeFile(cover);
			float dstWidth = 86f; float dstHeight = 128f;
			float dstRatio = dstHeight / dstWidth;
			float ratio = (float)bm.getHeight() / bm.getWidth();
			if(ratio > dstRatio){ //tall image
				dstWidth = dstHeight / ratio;
			} else if(ratio < dstRatio){ //wide image
				dstHeight = dstWidth * ratio;
			}
			bm = Bitmap.createScaledBitmap(bm, (int)dstWidth, (int)dstHeight, true);
			audiobook.setThumbnail(bm);
			
			System.out.println("(AM.245)   "+_author + " ::: " + _album + "("+dstWidth+", "+dstHeight+" ->"+ratio+")");
		}

		ArrayList<File> filelist;
		if(incl_subfolders){
			filelist = collectFiles(new ArrayList<File>(), album_folder, new Mp3FileFilter());
		} else {
			filelist = new ArrayList<File>(Arrays.asList(album_folder.listFiles(new Mp3FileFilter())));			
		}

		TrackList playlist = new TrackList();
		Collections.sort(playlist, new Comparator<Track>() {
			@Override
			public int compare(Track lhs, Track rhs) {
				return lhs.getPath().compareToIgnoreCase(rhs.getPath());
			}
		});

		for(File file : filelist){
			Track track = new Track();
			track.setPath(file.getAbsolutePath());
			track.setTitle(file.getName().replace(".mp3", ""));
			if(cover != null) track.setCover(cover);

			playlist.add(track);
		}
		audiobook.setPlaylist(playlist);
		return audiobook;
	}
	public ArrayList<Audiobook> autodetect(File folder){
		ArrayList<Audiobook> list = new ArrayList<Audiobook>();

		String state = Environment.getExternalStorageState();
		if(!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//MEDIA_MOUNTED => read/write access, MEDIA_MOUNTED_READ_ONLY => read access
			throw new RuntimeException("No external storrage!");
		}

		ArrayList<File> album_folders = collectFiles(new ArrayList<File>(), folder, new AlbumFolderFilter());
		for(File album_folder : album_folders){
			Audiobook audiobook = autoCreateAudiobook(album_folder, folder, true);
			list.add(audiobook);
		}

		return list;
	}
	private ArrayList<File> collectFiles(ArrayList<File> list, File folder, FileFilter filter){
		if(list == null) throw new IllegalArgumentException("Must have a list");
		if(folder == null) throw new IllegalArgumentException("Must have a root folder");
		if(folder.listFiles() == null) return list;
		for(File file : folder.listFiles()){
			if(filter == null || filter.accept(file.getAbsoluteFile())) list.add(file);
			if(file.isDirectory()) collectFiles(list, file, filter);
		}
		return list;
	}

	//Authors & Albums
	public HashSet<String> getAuthors() { return authors; }
	public HashSet<String> getAlbums() { return albums; }
}
