



package rd.dap.support;

import java.util.Iterator;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.IntentService;
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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

/* Store dele af denne klasse er kopieret fra developer.android.com/google/auth/api-client.html
 * Der er fortaget mindre justeringer.
 */

public class DriveHandler2 implements ConnectionCallbacks, OnConnectionFailedListener {
	private Activity activity;
	private final String TAG = "DriveHandler";
	private static GoogleApiClient client = null;
	private static final int REQUESTCODE_ERROR_DIALOG = 10001;
	public static final int REQUESTCODE_RESOLVE_ERROR = 11001;
	public static final int REQUEST_CODE_SEND = 11011;
	private static final String ERROR_DIALOG_TAG = "DriveHandler.ErrorDialogFragment";
	private boolean isResolvingError = false;

	public DriveHandler2(Activity activity) {
		this.activity = activity;
	}

	public void connect() {
		int s = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		switch(s){
		case ConnectionResult.SUCCESS:
			client = new GoogleApiClient.Builder(activity)
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
			GooglePlayServicesUtil.getErrorDialog(s, activity, REQUESTCODE_ERROR_DIALOG);
			break;
		}
	}
	public void disconnect(){
		if(client == null) return;
		client.disconnect();
	}

	public void findFile(String title){
		if(client == null) throw new RuntimeException("Not connected to Drive API");
		
		System.out.println("Firing service intent");
		Intent serviceIntent = new Intent(activity, DriveService.class);
		serviceIntent.putExtra("data", "datastring");
		activity.startService(serviceIntent);
	}	
	public void upload(){
		if(client == null) throw new RuntimeException("Not connected to Drive API");
		
		Query query = new Query.Builder()
        .addFilter(Filters.eq(SearchableField.TITLE, "test.txt"))
        .build();
		Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			@Override
			public void onResult(MetadataBufferResult result) {
				MetadataBuffer buffer = result.getMetadataBuffer();
				if(buffer.getCount() > 0){
					Iterator<Metadata> iterator = buffer.iterator();
					while(iterator.hasNext()){
						Metadata metadata = iterator.next();
						System.out.println("Title = "+metadata.getTitle());
						System.out.println("MIME type = "+metadata.getMimeType());
						System.out.println("Description = "+metadata.getDescription());
						System.out.println("Content availabillity = "+metadata.getContentAvailability());
						System.out.println("Extension = "+metadata.getFileExtension());
						System.out.println("File size = "+metadata.getFileSize());
						System.out.println("Is editable = "+metadata.isEditable());
					}
				} else {
					Log.d(TAG, "No files found");
				}
				
			}
		});
		
//		Drive.DriveApi.newContents(client).setResultCallback(new ResultCallback<ContentsResult>() {
//			@Override
//			public void onResult(ContentsResult result) {
//				// If the operation was not successful, we cannot do anything and must fail.
//				if (!result.getStatus().isSuccess()) {
//					Log.i(TAG, "Failed to create new contents.");
//					return;
//				}
//				Log.i(TAG, "New contents created.");
//
//				//Resolve data in string format
//				String data = "TEst";
//				//Create a writer from the content-result
//				Contents contents = result.getContents();
//				OutputStream stream = contents.getOutputStream();
//				OutputStreamWriter writer = new OutputStreamWriter(stream);
//				BufferedWriter out = new BufferedWriter(writer);
//				//Write the data
//				System.out.println("Data = "+data);
//				try {
//					out.write(data);
//					out.flush();
//					out.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//					Log.d(TAG, "Unable to write data");
//				}
//				
//
//				//Create metadata
//				MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//				.setMimeType("text/plain").setTitle("test.txt").build();
//
//				//Create intent
//				IntentSender intentSender = Drive.DriveApi
//						.newCreateFileActivityBuilder()
//						.setInitialMetadata(metadataChangeSet)
//						.setInitialContents(result.getContents())
//						.build(client);
//				try {
//					activity.startIntentSenderForResult(
//							intentSender, REQUEST_CODE_SEND, 
//							/*fillInIntent = */ null, 
//							/*flagsMask = */ 0, 
//							/*flagsValue = */ 0,
//							/*extraFlags = */ 0);
//				} catch (SendIntentException e) {
//					Log.i(TAG, "Failed to launch file chooser.");
//				}
//				/*
//				 * Now that an intent has been fired the result is a callback 
//				 * to onActivityResult with the requestCode REQUEST_CODE_SEND
//				 */
//			}
//		});
	}


	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (isResolvingError) { // Already attempting to resolve an error.
			return;
		} else if (result.hasResolution()) {
			try {
				isResolvingError = true;
				result.startResolutionForResult(activity, REQUESTCODE_RESOLVE_ERROR);
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
		Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onConnected");

		findFile("test.txt");
		System.out.println("Done looking for files");
//		upload();
	}
	@Override
	public void onConnectionSuspended(int result) {
		Toast.makeText(activity, "Connection suspende", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onConnectionSuspende: "+result);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUESTCODE_RESOLVE_ERROR:
			isResolvingError = false;
			if (resultCode == Activity.RESULT_OK) {
				System.out.println(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
				// Make sure the app is not already connected or attempting to connect
				if (!client.isConnecting() && !client.isConnected()) {
					client.connect();
				}
			}

		case REQUEST_CODE_SEND:
			if (resultCode == Activity.RESULT_OK) {
				Log.d(TAG, "File saved successfully");
			} else {
				Log.d(TAG, "File NOT saved");
			}
			break;
		}
	} 

	/* Creates a dialog for an error message */
	private void showErrorDialog(int errorCode) {
		// Create a fragment for the error dialog
		ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
		// Pass the error that should be displayed
		Bundle args = new Bundle();
		args.putInt(ERROR_DIALOG_TAG, errorCode);
		dialogFragment.setArguments(args);
		dialogFragment.show(activity.getFragmentManager(), "errordialog");
	}

	/* Called from ErrorDialogFragment when the dialog is dismissed. */
	public void onDialogDismissed() {
		isResolvingError = false;
	}

	/* A fragment to display an error dialog */
	public class ErrorDialogFragment extends DialogFragment {
		public ErrorDialogFragment() { }

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Get the error code and retrieve the appropriate dialog
			int errorCode = this.getArguments().getInt(ERROR_DIALOG_TAG);
			return GooglePlayServicesUtil.getErrorDialog(errorCode,
					this.getActivity(), REQUESTCODE_RESOLVE_ERROR);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			DriveHandler2.this.onDialogDismissed();
		}
	}



	public static class DriveService extends IntentService {
		private final String TAG = "DriveService";

		public DriveService() {
			super("DriveService");
			System.out.println("ServiceIntent.constructor");
		}


		@Override
		protected void onHandleIntent(Intent intent) {
			String data = intent.getDataString();
			System.out.println("Data = "+data);
			
			
			System.out.println("Querying Google Drive");
			
			Query query = new Query.Builder()
	        .addFilter(Filters.eq(SearchableField.TITLE, "test.txt"))
	        .build();
			Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
				@Override
				public void onResult(MetadataBufferResult result) {
					System.out.println("Got a result!");
					MetadataBuffer buffer = result.getMetadataBuffer();
					System.out.println("Buffer count = "+buffer.getCount());
					if(buffer.getCount() > 0){
						Iterator<Metadata> iterator = buffer.iterator();
						while(iterator.hasNext()){
							Metadata metadata = iterator.next();
							System.out.println(metadata.getTitle());
						}
					} else {
						Log.d(TAG, "No files found");
					}
				}
			});
		}
	}
}
