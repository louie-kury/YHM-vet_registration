package com.example.vetregistration.adapter.in.web;

import java.util.List;
import java.util.UUID;

import com.example.vetregistration.application.HospitalPage;

public record HospitalPageResponse(List<HospitalResponse> content, int page, int size, long totalElements,
		long totalPages) {

	static HospitalPageResponse from(HospitalPage result) {
		List<HospitalResponse> content = result.content().stream()
				.map(hospital -> new HospitalResponse(hospital.uuid(), hospital.name(), hospital.roadAddress(),
						hospital.lotAddress(), hospital.phoneNumber()))
				.toList();
		return new HospitalPageResponse(content, result.page(), result.size(), result.totalElements(), result.totalPages());
	}

	public record HospitalResponse(UUID uuid, String name, String roadAddress, String lotAddress, String phoneNumber) {
	}

}
