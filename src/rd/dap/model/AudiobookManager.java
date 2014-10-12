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

import rd.dap.support.AlbumFolderFilter;
import rd.dap.support.Mp3FileFilter;
import rd.dap.support.TrackList;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class AudiobookManager {
	private static AudiobookManager instance = new AudiobookManager();
	private static final File root = Environment.getExternalStorageDirectory();
	private static final File home = new File(root.getPath() + File.separator +"Audiobooks");
	private ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();

	public static AudiobookManager getInstance(){
		return instance; //Eager singleton
	}
	
	private AudiobookManager(){};
	
	//CRUD Audiobook
	public void addAudiobook(Context context, Audiobook audiobook){ 
		audiobooks.add(audiobook);
		saveAudiobooks(context);
	}
	public ArrayList<Audiobook> getAudiobooks(Context context){ return audiobooks; }
	public void updateAudiobook(Context context, Audiobook audiobook, Audiobook original_audiobook) {
		for(Audiobook element : getAudiobooks(context)){
			if(element.equals(original_audiobook)){
				element.setAudiobook(audiobook);
			}
		}
		saveAudiobooks(context);
	}
	public void removeAudiobook(Context context, Audiobook audiobook) { 
		audiobooks.remove(audiobook);
		saveAudiobooks(context);
	}
	
	//Load and save
	private void saveAudiobooks(Context context){
		Gson gson = new Gson();
		String json = gson.toJson(audiobooks);
		System.out.println(json);
		
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
	}
	public ArrayList<Audiobook> loadAudiobooks(Context context){
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
		return audiobooks;
	}
	
	public Audiobook autoCreateAudiobook(File album_folder, boolean incl_subfolders){
		Audiobook audiobook = new Audiobook();
		audiobook.setAuthor(album_folder.getParentFile().getName());
		audiobook.setAlbum(album_folder.getName());

		String cover = null;
		for(File file : album_folder.listFiles()){
			if("albumart.jpg".equalsIgnoreCase(file.getName())){
				cover = file.getAbsolutePath();
				break;
			}
		}
		if(cover != null) { audiobook.setCover(cover); }

		ArrayList<File> filelist = new ArrayList<File>(Arrays.asList(album_folder.listFiles(new Mp3FileFilter())));
		TrackList playlist = new TrackList();
		//TODO sort by filename
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
	public ArrayList<Audiobook> autodetect(){
		ArrayList<Audiobook> list = new ArrayList<Audiobook>();

		String state = Environment.getExternalStorageState();
		if(!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//MEDIA_MOUNTED => read/write access, MEDIA_MOUNTED_READ_ONLY => read access
			throw new RuntimeException("No external storrage!");
		}

		ArrayList<File> albums = collectFiles(new ArrayList<File>(), home, new AlbumFolderFilter());
		for(File album_folder : albums){
			Audiobook audiobook = autoCreateAudiobook(album_folder, true);
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

}
