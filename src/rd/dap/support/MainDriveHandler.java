package rd.dap.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import rd.dap.model.Callback;
import rd.dap.model.DriveHandler;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

/* Store dele af denne klasse er kopieret fra developer.android.com/google/auth/api-client.html
 * Der er fortaget mindre justeringer.
 */
public abstract class MainDriveHandler extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, DriveHandler {
	private final String TAG = "DriveHandler";
	private static GoogleApiClient client = null;
	private boolean isResolvingError = false;
	private static final int DH_REQUEST_CODE_ERROR_DIALOG = 10001;
	private static final int DH_REQUEST_CODE_RESOLVE_ERROR = 11001;
	private static final String DH_ERROR_DIALOG_TAG = "DriveHandler.ErrorDialogFragment";
	private static final String DH_DRIVE_FILENAME = "bookmarks.dap";
	private static final String DH_DRIVE_FOLDERNAME = "DAP";
	public static final int SUCCESS = 0;
	public static final int FAILURE = -1;
	private enum Mode {DOWNLOAD, UPLOAD};
	
	@Override
	public void download(Callback<String> resultCallback){
		Log.d(TAG, "download");
		if(client == null) throw new RuntimeException("Not connected to Drive API");
		common_query_folder(null, Mode.DOWNLOAD, resultCallback);
	}
	@Override
	public void upload(final String data){
		Log.d(TAG, "upload");
		if(client == null) throw new RuntimeException("Not connected to Drive API");
		common_query_folder(data, Mode.UPLOAD, null);
	}

	private void common_query_folder(final String data, final Mode mode, final Callback<String> resultCallback){
		Query query = new Query.Builder()
		.addFilter(Filters.eq(SearchableField.TITLE, DH_DRIVE_FOLDERNAME))
		.addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
		.build();
		Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			
			@Override
			public void onResult(MetadataBufferResult result) {
				MetadataBuffer buffer = result.getMetadataBuffer();
				if(buffer.getCount() > 0){
					ArrayList<Metadata> list = new ArrayList<Metadata>();
					Iterator<Metadata> iterator = buffer.iterator();
					while(iterator.hasNext()){
						Metadata metadata = iterator.next();
						if(!metadata.isTrashed()){
							list.add(metadata);
						}
					}
					if(!list.isEmpty()) {
						DriveId id = list.get(0).getDriveId();
						DriveFolder folder = Drive.DriveApi.getFolder(client, id);
						
						//Query file no matter what
						common_query_file(folder, data, mode, resultCallback);
						
					} else {
						switch(mode){
						case DOWNLOAD: resultCallback.onResult(Callback.NO_FOLDER); break;
						case UPLOAD: upload_create_folder(data); break; 
						}
						
					}
					
				} else {
					switch(mode){
					case DOWNLOAD: resultCallback.onResult(Callback.NO_FOLDER); break;
					case UPLOAD: upload_create_folder(data); break; 
					}
				}
				buffer.close();
			}
		});
	}
	private void common_query_file(final DriveFolder folder, final String data, final Mode mode, final Callback<String> resultCallback){
		Query query = new Query.Builder()
		.addFilter(Filters.eq(SearchableField.TITLE, DH_DRIVE_FILENAME))
		.addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
		.build();
		Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			
			@Override
			public void onResult(MetadataBufferResult result) {
				MetadataBuffer buffer = result.getMetadataBuffer();
				if(buffer.getCount() > 0){
					ArrayList<Metadata> list = new ArrayList<Metadata>();
					Iterator<Metadata> iterator = buffer.iterator();
					while(iterator.hasNext()){
						Metadata metadata = iterator.next();
						if(!metadata.isTrashed()){
							list.add(metadata);
						}
					}
					if(!list.isEmpty()) {
						DriveId id = list.get(0).getDriveId();
						DriveFile file = Drive.DriveApi.getFile(client, id);
						
						//Read file no matter what
						common_read(file, data, mode, resultCallback);
						
					} else {
						switch(mode){
						case DOWNLOAD: resultCallback.onResult(Callback.NO_FILE); break;
						case UPLOAD: upload_create_contents(folder, data); break; 
						}
					}
					
				} else {
					switch(mode){
					case DOWNLOAD: resultCallback.onResult(Callback.NO_FILE); break;
					case UPLOAD: upload_create_contents(folder, data); break; 
					}
				}
				buffer.close();
			}
		});
	}
	private void common_read(final DriveFile file, final String newData, final Mode mode, final Callback<String> resultCallback){
		file.open(client, DriveFile.MODE_READ_ONLY, new DownloadProgressListener() {

			@Override
			public void onProgress(long bytesDownloaded, long bytesExpected) {
				System.out.println(bytesDownloaded + " / " + bytesExpected);
			}
		}).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

			@Override
			public void onResult(DriveContentsResult result) {
				DriveContents contents = result.getDriveContents();

				//Input
				InputStream stream = contents.getInputStream();
				InputStreamReader reader = new InputStreamReader(stream);
				BufferedReader in = new BufferedReader(reader);

				try {
					//Read
					StringBuilder stringbuilder = new StringBuilder();
					String line = in.readLine();
					while(line != null){
						stringbuilder.append(line);
						line = in.readLine();
					}
					String oldData = stringbuilder.toString();
					in.close();
					Log.d(TAG, "File contents (before) = "+oldData);
					
					switch(mode){
					case DOWNLOAD: resultCallback.onResult(oldData); break;
					case UPLOAD: upload_write(file, oldData, newData); 
					}
					
				} catch (IOException e) {
					Log.d(TAG, "Failed to read/write contents");
					e.printStackTrace();
				}
			}
		});
	}
	
	private void upload_create_folder(final String data){
		Log.d(TAG, "upload_create_folder");
		//Create metadata
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
			.setMimeType(DriveFolder.MIME_TYPE)
			.setTitle(DH_DRIVE_FOLDERNAME)
			.build();
		
		Drive.DriveApi.getRootFolder(client)
			.createFolder(client, changeSet)
			.setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
				
				@Override
				public void onResult(DriveFolderResult result) {
					// If the operation was not successful, we cannot do anything and must fail.
					if (!result.getStatus().isSuccess()) {
						Log.i(TAG, "Failed to create new folder.");
						return;
					}
					Log.i(TAG, "New folder created.");
					DriveFolder folder = result.getDriveFolder();
					upload_create_contents(folder, data);
				}
			});
	}
	private void upload_create_contents(final DriveFolder folder, final String data){
		Log.d(TAG, "upload_create_contents");
		Drive.DriveApi.newContents(client)
			.setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
				
				@Override
				public void onResult(ContentsResult result) {
					// If the operation was not successful, we cannot do anything and must fail.
					if (!result.getStatus().isSuccess()) {
						Log.i(TAG, "Failed to create new contents.");
						return;
					}
					Log.i(TAG, "New contents created.");
					
					//Create a writer from the content-result
					Contents contents = result.getContents();
					OutputStream stream = contents.getOutputStream();
					OutputStreamWriter writer = new OutputStreamWriter(stream);
					BufferedWriter out = new BufferedWriter(writer);
					//Write the data
					try {
						out.write(data);
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						Log.d(TAG, "Unable to write data");
					}
					
					upload_create_file(folder, contents);
				}
			});
	}
	private void upload_create_file(DriveFolder folder, Contents contents){
		Log.d(TAG, "upload_create_file");
		//Create metadata
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
			.setMimeType("text/plain")
			.setTitle(DH_DRIVE_FILENAME)
			.build();

		folder.createFile(client, changeSet, contents)
			.setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
				
				@Override
				public void onResult(DriveFileResult result) {
					// If the operation was not successful, we cannot do anything and must fail.
					if (!result.getStatus().isSuccess()) {
						Log.i(TAG, "Failed to create new file.");
						return;
					}
					Log.i(TAG, "New file created.");
				}
			});
	}
	private void upload_write(DriveFile file, final String oldData, final String newData){	
		file.open(client, DriveFile.MODE_WRITE_ONLY, new DownloadProgressListener() {
			
			@Override
			public void onProgress(long bytesDownloaded, long bytesExpected) {
				System.out.println(bytesDownloaded + " / " + bytesExpected);
			}
		}).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
			
			@Override
			public void onResult(DriveContentsResult result) {
				DriveContents contents = result.getDriveContents();
				
				//Output
				OutputStream out = contents.getOutputStream();
				
				try {
					//Resove conflicts
					String resolved = upload_resolve_conflicts(oldData, newData);
					
					//Write
					out.write(resolved.getBytes());
					out.flush();
					out.close();
					
					Log.d(TAG, "File contents (after) = "+resolved);
					
					contents.commit(client, null).setResultCallback(new ResultCallback<Status>() {
						
						@Override
						public void onResult(Status status) {
							Log.d(TAG, "Status: " + (status.isSuccess() ? "Success" : "Failure"));
						}
					});
				} catch (IOException e) {
					Log.d(TAG, "Failed to read/write contents");
					e.printStackTrace();
				}
			}
		});
		
		
	}
	private String upload_resolve_conflicts(String oldData, String newData){
		//FIXME conflicts are not really resolved - new is written, old is discarded
		return newData;
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case DH_REQUEST_CODE_RESOLVE_ERROR:
			isResolvingError = false;
			if (resultCode == Activity.RESULT_OK) {
				Log.i(TAG, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
				// Make sure the app is not already connected or attempting to connect
				if (!client.isConnecting() && !client.isConnected()) {
					client.connect();
				}
			}
			break;
		}
	} 

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (isResolvingError) { // Already attempting to resolve an error.
			return;
		} else if (result.hasResolution()) {
			try {
				isResolvingError = true;
				result.startResolutionForResult(this, DH_REQUEST_CODE_RESOLVE_ERROR);
			} catch (SendIntentException e) {
				// There was an error with the resolution intent. Try again.
				client.connect();
			}
		} else {
			// Show dialog using GooglePlayServicesUtil.getErrorDialog()
			showErrorDialog(result.getErrorCode());
			isResolvingError = true;
		}
	}
	@Override
	public void onConnected(Bundle connectionHint) {
//		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onConnected");
	}
	@Override
	public void onConnectionSuspended(int result) {
		Toast.makeText(this, "Connection suspende", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onConnectionSuspende: "+result);
	}

	private void showErrorDialog(int errorCode) {
		/* Creates a dialog for an error message */

		// Create a fragment for the error dialog
		ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
		// Pass the error that should be displayed
		Bundle args = new Bundle();
		args.putInt(DH_ERROR_DIALOG_TAG, errorCode);
		dialogFragment.setArguments(args);
		dialogFragment.show(getFragmentManager(), "errordialog");
	}
	public void onDialogDismissed() {
		/* Called from ErrorDialogFragment when the dialog is dismissed. */
		isResolvingError = false;
	}

	public class ErrorDialogFragment extends DialogFragment {
		/* A fragment to display an error dialog */
		public ErrorDialogFragment() { }

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Get the error code and retrieve the appropriate dialog
			int errorCode = this.getArguments().getInt(DH_ERROR_DIALOG_TAG);
			return GooglePlayServicesUtil.getErrorDialog(errorCode,
					this.getActivity(), DH_REQUEST_CODE_RESOLVE_ERROR);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			MainDriveHandler.this.onDialogDismissed();
		}
	}

	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		
		//connect to Google Drive
		connect();
	}
	@Override
	public void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		
		//disconnet from Google Drive
		disconnect();
	}
	
	public void connect() {
		int playServiceAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		switch(playServiceAvailable){
		case ConnectionResult.SUCCESS:
			client = new GoogleApiClient.Builder(this)
			.addApi(Drive.API)
			.addScope(Drive.SCOPE_FILE)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();

			if (!isResolvingError) client.connect();

			break;

		case ConnectionResult.SERVICE_MISSING:
		case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
		case ConnectionResult.SERVICE_DISABLED:
			GooglePlayServicesUtil.getErrorDialog(playServiceAvailable, this, DH_REQUEST_CODE_ERROR_DIALOG);
			break;
		}
	}
	public void disconnect(){
		if(client == null) return;
		client.disconnect();
	}
}
