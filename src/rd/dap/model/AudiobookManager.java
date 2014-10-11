package rd.dap.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import rd.dap.support.AlbumFolderFilter;
import rd.dap.support.Mp3FileFilter;
import rd.dap.support.TrackList;
import android.os.Environment;

public final class AudiobookManager {
	private static AudiobookManager instance = new AudiobookManager();
	private static final File root = Environment.getExternalStorageDirectory();
	private static final File home = new File(root.getPath() + File.separator +"Audiobooks");
	private ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();

	public static AudiobookManager getInstance(){
		return instance; //Eager singleton
	}
	
	private AudiobookManager(){};
	
	//create a file in internal storage
	//File file = new File(context.getFilesDir(), filename);
	
	//CRUD Audiobook
	public void addAudiobook(Audiobook audiobook){ audiobooks.add(audiobook); }
	public ArrayList<Audiobook> getAudiobooks(){ return audiobooks; }
	public void updateAudiobook(Audiobook audiobook, Audiobook original_audiobook) {
		for(Audiobook element : getAudiobooks()){
			if(element.equals(original_audiobook)){
				element.setAudiobook(audiobook);
			}
		}	
	}
	public void removeAudiobook(Audiobook audiobook) { audiobooks.remove(audiobook); }
	
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
