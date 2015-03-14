package rd.dap.model;

public interface GenericCallback<T> {
	public static final String NO_FOLDER = "no_folder";
	public static final String NO_FILE = "no_file";
	public void onResult(T result);
}
