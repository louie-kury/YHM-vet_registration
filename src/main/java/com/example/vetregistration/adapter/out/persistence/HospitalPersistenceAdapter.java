package com.example.vetregistration.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.vetregistration.application.HospitalPage;
import com.example.vetregistration.application.HospitalPage.HospitalSummary;
import com.example.vetregistration.application.port.out.HospitalQueryPort;

@Repository
public class HospitalPersistenceAdapter implements HospitalQueryPort {

	private final HospitalJpaRepository hospitalJpaRepository;

	HospitalPersistenceAdapter(HospitalJpaRepository hospitalJpaRepository) {
		this.hospitalJpaRepository = hospitalJpaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public HospitalPage findPage(String keyword, int page, int size) {
		String pattern = keyword.isEmpty() ? null : containsPattern(keyword);
		long totalElements = hospitalJpaRepository.countMatches(pattern);
		long offset = (long) page * size;
		if (offset >= totalElements) {
			return new HospitalPage(List.of(), page, size, totalElements);
		}

		List<HospitalSummary> content = hospitalJpaRepository.findMatches(pattern, size, offset).stream()
				.map(hospital -> new HospitalSummary(hospital.getUuid(), hospital.getName(), hospital.getRoadAddress(),
						hospital.getLotAddress(), hospital.getPhoneNumber()))
				.toList();
		return new HospitalPage(content, page, size, totalElements);
	}

	private static String containsPattern(String keyword) {
		return "%" + keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
	}

}
