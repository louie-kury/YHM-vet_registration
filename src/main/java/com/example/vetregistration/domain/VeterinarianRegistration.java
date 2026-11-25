package com.example.vetregistration.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record VeterinarianRegistration(UUID uuid, UUID accountUuid, String name, String major, String specialty,
		String licenseNo, LocalDateTime registeredAt, boolean available, Status status, String statusReason,
		InitialAffiliation affiliation) {

	public static VeterinarianRegistration create(UUID accountUuid, String name, String major, String specialty,
			String licenseNo, UUID hospitalUuid, LocalDateTime registeredAt) {
		InitialAffiliation affiliation = new InitialAffiliation(UUID.randomUUID(), hospitalUuid, registeredAt, null, 1);
		return new VeterinarianRegistration(UUID.randomUUID(), accountUuid, name, major, specialty, licenseNo,
				registeredAt, false, Status.RESTRICTED, "LICENSE_UNVERIFIED", affiliation);
	}

	public enum Status {
		ACTIVE, SUSPENDED, RESTRICTED
	}

	public record InitialAffiliation(UUID uuid, UUID hospitalUuid, LocalDateTime startedAt, LocalDateTime endedAt,
			int currentFlag) {
	}

}
