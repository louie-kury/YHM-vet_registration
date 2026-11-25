package com.example.vetregistration.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.vetregistration.adapter.out.account.LocalRegistrationAccountAdapter;
import com.example.vetregistration.application.VeterinarianRegistrationService;
import com.example.vetregistration.application.port.in.RegisterVeterinarianUseCase;
import com.example.vetregistration.application.port.out.RegistrationAccountPort;
import com.example.vetregistration.application.port.out.VeterinarianRegistrationPort;

@Configuration(proxyBeanMethods = false)
@LocalRegistrationEnabled
public class LocalVeterinarianRegistrationConfiguration {

	@Bean
	RegistrationAccountPort localRegistrationAccountPort() {
		return new LocalRegistrationAccountAdapter();
	}

	@Bean
	RegisterVeterinarianUseCase registerVeterinarianUseCase(VeterinarianRegistrationPort registrationPort,
			RegistrationAccountPort accountPort) {
		return new VeterinarianRegistrationService(registrationPort, accountPort, Clock.systemUTC());
	}

}
