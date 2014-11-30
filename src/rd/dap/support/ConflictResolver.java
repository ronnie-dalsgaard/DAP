package rd.dap.support;

public class ConflictResolver implements iConflictResolver<String> {

	@Override
	public String resolveConflicts(String oldData, String newData) {
		//FIXME conflicts are not really resolved - new is written, old is discarded
		return newData;
	}

}
