package com.example.vetregistration.application.port.in;

import java.util.UUID;

public interface RegisterVeterinarianUseCase {

	Result register(Command command);

	record Command(String name, String major, String specialty, String licenseNo, UUID hospitalUuid) {
	}

	record Result(UUID veterinarianUuid, UUID hospitalUuid) {
	}

}
