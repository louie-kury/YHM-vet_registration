package com.example.vetregistration.application;

public abstract class VeterinarianRegistrationException extends RuntimeException {

	private VeterinarianRegistrationException(Throwable cause) {
		super(null, cause);
	}

	public static final class HospitalNotFound extends VeterinarianRegistrationException {

		public HospitalNotFound() {
			super(null);
		}

	}

	public static final class DuplicateLicense extends VeterinarianRegistrationException {

		public DuplicateLicense() {
			super(null);
		}

		public DuplicateLicense(Throwable cause) {
			super(cause);
		}

	}

}
