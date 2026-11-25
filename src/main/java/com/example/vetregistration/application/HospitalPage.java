package com.example.vetregistration.application;

import java.util.List;
import java.util.UUID;

public record HospitalPage(List<HospitalSummary> content, int page, int size, long totalElements) {

	public long totalPages() {
		return totalElements == 0 ? 0 : (totalElements - 1) / size + 1;
	}

	public record HospitalSummary(UUID uuid, String name, String roadAddress, String lotAddress, String phoneNumber) {
	}

}
