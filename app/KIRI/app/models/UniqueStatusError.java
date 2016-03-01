package models;

/**
 * Kelas ini untuk menangani kasus: - Credential Fail - Session Expired
 * 
 * @author Tommy Adhitya The
 */
public class UniqueStatusError extends Exception {
	private String status;

	public UniqueStatusError(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}