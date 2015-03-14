package rd.dap.model;

public interface DriveHandler {
	public void upload(String json, GenericCallback<String> callback);
	public void download(GenericCallback<String> callback);
}
