package com.example.vetregistration.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.example.vetregistration.application.VeterinarianRegistrationException.DuplicateLicense;
import com.example.vetregistration.application.VeterinarianRegistrationException.HospitalNotFound;
import com.example.vetregistration.application.port.in.RegisterVeterinarianUseCase;
import com.example.vetregistration.application.port.out.RegistrationAccountPort;
import com.example.vetregistration.application.port.out.VeterinarianRegistrationPort;
import com.example.vetregistration.domain.VeterinarianRegistration;

public class VeterinarianRegistrationService implements RegisterVeterinarianUseCase {

	private final VeterinarianRegistrationPort registrationPort;
	private final RegistrationAccountPort accountPort;
	private final Clock clock;

	public VeterinarianRegistrationService(VeterinarianRegistrationPort registrationPort,
			RegistrationAccountPort accountPort, Clock clock) {
		this.registrationPort = registrationPort;
		this.accountPort = accountPort;
		this.clock = clock;
	}

	@Override
	@Transactional
	public Result register(Command command) {
		long hospitalId = registrationPort.findHospitalId(command.hospitalUuid()).orElseThrow(HospitalNotFound::new);
		if (registrationPort.existsByLicenseNo(command.licenseNo())) {
			throw new DuplicateLicense();
		}

		UUID accountUuid = accountPort.resolveAccountUuid();
		LocalDateTime registeredAt = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
		VeterinarianRegistration registration = VeterinarianRegistration.create(accountUuid, command.name(),
				command.major(), command.specialty(), command.licenseNo(), command.hospitalUuid(), registeredAt);
		registrationPort.save(registration, hospitalId);
		return new Result(registration.uuid(), registration.affiliation().hospitalUuid());
	}

}
