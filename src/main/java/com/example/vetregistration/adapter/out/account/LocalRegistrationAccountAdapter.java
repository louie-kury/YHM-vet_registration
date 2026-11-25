package com.example.vetregistration.adapter.out.account;

import java.util.UUID;

import com.example.vetregistration.application.port.out.RegistrationAccountPort;

public final class LocalRegistrationAccountAdapter implements RegistrationAccountPort {

	@Override
	public UUID resolveAccountUuid() {
		return UUID.randomUUID();
	}

}
