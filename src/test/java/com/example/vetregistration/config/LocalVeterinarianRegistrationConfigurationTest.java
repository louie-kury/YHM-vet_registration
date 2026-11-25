package com.example.vetregistration.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.example.vetregistration.adapter.in.web.VeterinarianRegistrationController;
import com.example.vetregistration.application.port.in.RegisterVeterinarianUseCase;
import com.example.vetregistration.application.port.out.RegistrationAccountPort;
import com.example.vetregistration.application.port.out.VeterinarianRegistrationPort;

class LocalVeterinarianRegistrationConfigurationTest {

	private static final String ENABLED = "vet-registration.local-account.enabled=true";

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(LocalVeterinarianRegistrationConfiguration.class, VeterinarianRegistrationController.class)
			.withBean(VeterinarianRegistrationPort.class, () -> mock(VeterinarianRegistrationPort.class));

	@Test
	void enablesRegistrationWithLocalProfileAndExplicitOptIn() {
		contextRunner.withPropertyValues("spring.profiles.active=local", ENABLED).run(context -> {
			assertThat(context).hasSingleBean(RegistrationAccountPort.class);
			assertThat(context).hasSingleBean(RegisterVeterinarianUseCase.class);
			assertThat(context).hasSingleBean(VeterinarianRegistrationController.class);
		});
	}

	@Test
	void disablesRegistrationWithoutOptInEvenInLocalProfile() {
		assertDisabled("spring.profiles.active=local");
	}

	@Test
	void disablesRegistrationInNonLocalProfilesEvenWithOptIn() {
		assertDisabled("spring.profiles.active=prod", ENABLED);
		assertDisabled("spring.profiles.active=prod,local", ENABLED);
		assertDisabled(ENABLED);
	}

	private void assertDisabled(String... properties) {
		contextRunner.withPropertyValues(properties).run(context -> {
			assertThat(context).doesNotHaveBean(RegistrationAccountPort.class);
			assertThat(context).doesNotHaveBean(RegisterVeterinarianUseCase.class);
			assertThat(context).doesNotHaveBean(VeterinarianRegistrationController.class);
		});
	}

}
