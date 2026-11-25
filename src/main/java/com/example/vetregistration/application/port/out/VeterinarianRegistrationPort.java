package com.example.vetregistration.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.example.vetregistration.domain.VeterinarianRegistration;

public interface VeterinarianRegistrationPort {

	Optional<Long> findHospitalId(UUID hospitalUuid);

	boolean existsByLicenseNo(String licenseNo);

	void save(VeterinarianRegistration registration, long hospitalId);

}
