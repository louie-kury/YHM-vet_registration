package com.example.vetregistration.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(LocalRegistrationEnabled.OnlyLocalProfile.class)
@ConditionalOnProperty(name = "vet-registration.local-account.enabled", havingValue = "true")
public @interface LocalRegistrationEnabled {

	class OnlyLocalProfile implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			String[] profiles = context.getEnvironment().getActiveProfiles();
			if (profiles.length == 0) {
				profiles = context.getEnvironment().getDefaultProfiles();
			}
			return profiles.length == 1 && "local".equals(profiles[0]);
		}

	}

}
