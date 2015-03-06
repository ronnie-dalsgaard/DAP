package rd.dap.model;

public interface DriveHandler {
	public void upload(String json, Callback<String> callback);
	public void download(Callback<String> callback);
}
