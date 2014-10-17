package rd.dap.model;

public interface DriveHandler {
	public void upload(String json);
	public void download(Callback<String> callback);
}
