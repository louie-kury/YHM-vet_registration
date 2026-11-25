package com.example.vetregistration.application;

import org.springframework.stereotype.Service;

import com.example.vetregistration.application.port.in.SearchHospitalsUseCase;
import com.example.vetregistration.application.port.out.HospitalQueryPort;

@Service
public class HospitalSearchService implements SearchHospitalsUseCase {

	private final HospitalQueryPort hospitalQueryPort;

	public HospitalSearchService(HospitalQueryPort hospitalQueryPort) {
		this.hospitalQueryPort = hospitalQueryPort;
	}

	@Override
	public HospitalPage search(String keyword, int page, int size) {
		String normalizedKeyword = keyword == null ? "" : keyword.strip();
		return hospitalQueryPort.findPage(normalizedKeyword, page, size);
	}

}
