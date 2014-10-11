package rd.dap.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

/* Store dele af denne klasse er kopieret fra developer.android.com/google/auth/api-client.html
 * Der er fortaget mindre justeringer.
 */
public abstract class DriveHandler extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	private final String TAG = "DriveHandler";
	private static GoogleApiClient client = null;
	private boolean isResolvingError = false;
	private static final int DH_REQUEST_CODE_ERROR_DIALOG = 10001;
	private static final int DH_REQUEST_CODE_RESOLVE_ERROR = 11001;
	private static final int DH_REQUEST_CODE_UPLOAD = 11011;
	private static final int DH_REQUEST_CODE_DOWNLOAD = 11012;
	private static final int DH_REQUEST_CODE_UPDATE = 11013;
	private static final String DH_ERROR_DIALOG_TAG = "DriveHandler.ErrorDialogFragment";
	private static final String DH_DRIVE_FILENAME = "bookmarks.dap";
	public static final int SUCCESS = 0;
	public static final int FAILURE = -1;
	private int download_requestCode = -1;
	private int upload_requestCode = -1;
	private int update_requestCode = -1;

	protected void download(int requestCode){
		if(client == null) throw new RuntimeException("Not connected to Drive API");
		download_requestCode = requestCode;
		IntentSender i = Drive.DriveApi
				.newOpenFileActivityBuilder()
				.setMimeType(new String[] {"text/plain"})
				.build(client);
		try {
			startIntentSenderForResult(i, DH_REQUEST_CODE_DOWNLOAD, null, 0, 0, 0);
		} catch (SendIntentException e) {
			Log.i(TAG, "Failed to launch file chooser.");
		}
	}
	protected void upload(int requestCode, final String data){
		upload_requestCode = requestCode;
		Drive.DriveApi.newContents(client).setResultCallback(new ResultCallback<ContentsResult>() {
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

				//Create metadata
				MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
				.setMimeType("text/plain").setTitle(DH_DRIVE_FILENAME).build();

				//Create intent
				IntentSender intentSender = Drive.DriveApi
						.newCreateFileActivityBuilder()
						.setInitialMetadata(metadataChangeSet)
						.setInitialContents(result.getContents())
						.build(client);
				try {
					startIntentSenderForResult(
							intentSender, DH_REQUEST_CODE_UPDATE, 
							/*fillInIntent = */ null, 
							/*flagsMask = */ 0, 
							/*flagsValue = */ 0,
							/*extraFlags = */ 0);
				} catch (SendIntentException e) {
					Log.i(TAG, "Failed to launch file creator.");
				}
				/*
				 * Now that an intent has been fired the result is a callback 
				 * to onActivityResult with the requestCode REQUEST_CODE_SEND
				 */
			}
		});
	}
	protected void query(final int requestCode, String title){
		Query query = new Query.Builder()
		.addFilter(Filters.contains(SearchableField.TITLE, title))
		.build();
		Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			@Override
			public void onResult(MetadataBufferResult result) {
				MetadataBuffer buffer = result.getMetadataBuffer();
				Toast.makeText(DriveHandler.this, "Files found: "+buffer.getCount(), Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Files found: "+buffer.getCount());
				if(buffer.getCount() > 0){
					ArrayList<Metadata> list = new ArrayList<Metadata>();
					Iterator<Metadata> iterator = buffer.iterator();
					while(iterator.hasNext()){
						Metadata metadata = iterator.next();
						if(!metadata.isTrashed()){
							list.add(metadata);
						}
					}
					onDriveResult(requestCode, SUCCESS, list);
				} else {
					Log.d(TAG, "No files found");
					onDriveResult(requestCode, SUCCESS, new ArrayList<Metadata>());
				}
			}
		});
	}
	protected void getContents(final int requestCode, DriveFile df){
		df.openContents(client, DriveFile.MODE_READ_WRITE, new DownloadProgressListener() {
			@Override
			public void onProgress(long bytesDownloaded, long bytesExpected) {
				System.out.println(bytesDownloaded + " / " + bytesExpected);
			}
		}).setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
			@Override
			public void onResult(ContentsResult result) {
				if(!result.getStatus().isSuccess()){
					Toast.makeText(DriveHandler.this, "Failure to download", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Failure to download");
					return;
				}
				
				Contents contents = result.getContents();
				
				//This can be used with MODE_READ_WRITE
				FileDescriptor descriptor = contents.getParcelFileDescriptor().getFileDescriptor();
				FileInputStream stream = new FileInputStream(descriptor);
				
				//This can be used with MODE_READ_ONLY
				//(an outputStream can be used with MODE_WRITE_ONLY)
//				InputStream stream = contents.getInputStream();
				
				InputStreamReader reader = new InputStreamReader(stream);
				BufferedReader in = new BufferedReader(reader);
				StringBuilder stringbuilder = new StringBuilder();
				try {
					String line = in.readLine();
					while(line != null){
						stringbuilder.append(line);
						line = in.readLine();
					}
					String str = stringbuilder.toString();
					Log.d(TAG, "File contents = "+str);
					onDriveResult(requestCode, SUCCESS, str);
				} catch (IOException e) {
					Log.d(TAG, "Unable to read from stream");
					onDriveResult(requestCode, FAILURE);
				}
			}
		});
	}
	protected void update(final int requestCode, DriveFile currentBookmarkFile, final String data){
		currentBookmarkFile.openContents(client, DriveFile.MODE_READ_ONLY, new DownloadProgressListener() {
			@Override
			public void onProgress(long bytesDownloaded, long bytesExpected) {
				System.out.println(bytesDownloaded + " / " + bytesExpected);
			}
		}).setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
			@Override
			public void onResult(ContentsResult result) {
				if(!result.getStatus().isSuccess()){
					Toast.makeText(DriveHandler.this, "Failure to download", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Failure to download");
					return;
				}
				
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
			}
		});
	}

	//	protected void getBookmark_dap(int requestCode){
	//		String id = "DriveId:CAESHDBCMGI3ZkhZN0JtX21SMWh2VlhnNE9ITTBWREEY3gYg8PDwtqhR";
	//		DriveId driveId = DriveId.decodeFromString(id);
	//		DriveFile df = Drive.DriveApi.getFile(client, driveId);
	//		onDriveResult(requestCode, SUCCESS, df);
	//	}

	public abstract void onDriveResult(int requestCode, int result, Object... data);

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case DH_REQUEST_CODE_RESOLVE_ERROR:
			isResolvingError = false;
			if (resultCode == Activity.RESULT_OK) {
				System.out.println(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
				// Make sure the app is not already connected or attempting to connect
				if (!client.isConnecting() && !client.isConnected()) {
					client.connect();
				}
			}
			break;

		case DH_REQUEST_CODE_UPLOAD:
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(this, "Bookmarks uploaded", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "File saved successfully");
				onDriveResult(upload_requestCode, DriveHandler.SUCCESS);
			} else {
				Log.d(TAG, "File NOT saved");
				onDriveResult(upload_requestCode, DriveHandler.FAILURE);
			}
			upload_requestCode = -1;
			break;

		case DH_REQUEST_CODE_DOWNLOAD:
			if(data.getAction() != Intent.ACTION_PICK){
				System.out.println("WRONG ACTION...!");
				return;
			}
			if (resultCode == RESULT_OK) {
				DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
				System.out.println("driveID = "+driveId);
				System.out.println("driveId as String: " + driveId.encodeToString());
				DriveFile df = Drive.DriveApi.getFile(client, driveId);

				onDriveResult(download_requestCode, SUCCESS, df);
			} else {
				onDriveResult(download_requestCode, FAILURE);
			}
			download_requestCode = -1;
			break;
			
		case DH_REQUEST_CODE_UPDATE:
			if(resultCode == Activity.RESULT_OK){
				Toast.makeText(this, "Bookmarks uploaded", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "File saved successfully");
				onDriveResult(update_requestCode, DriveHandler.SUCCESS);
			} else {
				Log.d(TAG, "File NOT saved");
				onDriveResult(update_requestCode, DriveHandler.FAILURE);
			}
			update_requestCode = -1;
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
			DriveHandler.this.onDialogDismissed();
		}
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
