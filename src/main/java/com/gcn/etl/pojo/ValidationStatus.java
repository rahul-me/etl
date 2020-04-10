package com.gcn.etl.pojo;

import java.util.List;

public class ValidationStatus {
	private Boolean isDataValid;
	private Boolean isMissingPointRepaired;
	private Boolean isSpike;
	private String message;
	private List<MissingPoint> missingIntervalOrUsage;

	public Boolean getIsDataValid() {
		return isDataValid;
	}

	public void setIsDataValid(Boolean isDataValid) {
		this.isDataValid = isDataValid;
	}

	public Boolean getIsMissingPointRepaired() {
		return isMissingPointRepaired;
	}

	public void setIsMissingPointRepaired(Boolean isMissingPointRepaired) {
		this.isMissingPointRepaired = isMissingPointRepaired;
	}

	public Boolean getIsSpike() {
		return isSpike;
	}

	public void setIsSpike(Boolean isSpike) {
		this.isSpike = isSpike;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<MissingPoint> getMissingIntervalOrUsage() {
		return missingIntervalOrUsage;
	}

	public void setMissingIntervalOrUsage(List<MissingPoint> missingIntervalOrUsage) {
		this.missingIntervalOrUsage = missingIntervalOrUsage;
	}

	@Override
	public String toString() {
		return "ValidationStatus [isDataValid=" + isDataValid + ", isMissingPointRepaired=" + isMissingPointRepaired
				+ ", isSpike=" + isSpike + ", message=" + message + ", missingIntervalOrUsage=" + missingIntervalOrUsage + "]";
	}
}
