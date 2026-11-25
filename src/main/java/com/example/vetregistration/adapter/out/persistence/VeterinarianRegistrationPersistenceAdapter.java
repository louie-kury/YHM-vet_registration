package com.example.vetregistration.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.stereotype.Repository;

import com.example.vetregistration.application.VeterinarianRegistrationException.DuplicateLicense;
import com.example.vetregistration.application.port.out.VeterinarianRegistrationPort;
import com.example.vetregistration.domain.VeterinarianRegistration;

import jakarta.persistence.EntityManager;

@Repository
public class VeterinarianRegistrationPersistenceAdapter implements VeterinarianRegistrationPort {

	private static final String LICENSE_INDEX = "uk_veterinarian_license_no";

	private final EntityManager entityManager;

	public VeterinarianRegistrationPersistenceAdapter(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Optional<Long> findHospitalId(UUID hospitalUuid) {
		return Optional.ofNullable(entityManager.createQuery(
				"select h.id from HospitalEntity h where h.uuid = :hospitalUuid", Long.class)
				.setParameter("hospitalUuid", hospitalUuid)
				.getSingleResultOrNull());
	}

	@Override
	public boolean existsByLicenseNo(String licenseNo) {
		return entityManager.createQuery(
				"select v.id from VeterinarianEntity v where v.licenseNo = :licenseNo", Long.class)
				.setParameter("licenseNo", licenseNo)
				.setMaxResults(1)
				.getSingleResultOrNull() != null;
	}

	@Override
	public void save(VeterinarianRegistration registration, long hospitalId) {
		VeterinarianEntity veterinarian = new VeterinarianEntity(registration);
		try {
			entityManager.persist(veterinarian);
			entityManager.flush();
		} catch (RuntimeException exception) {
			if (isDuplicateLicense(exception)) {
				throw new DuplicateLicense(exception);
			}
			throw exception;
		}

		entityManager.persist(new VetHospitalAffiliationEntity(veterinarian.getId(), hospitalId,
				registration.affiliation()));
		entityManager.flush();
	}

	private static boolean isDuplicateLicense(Throwable exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConstraintViolationException violation && violation.getKind() == ConstraintKind.UNIQUE) {
				String constraintName = violation.getConstraintName();
				return constraintName != null
						&& LICENSE_INDEX.equalsIgnoreCase(constraintName.substring(constraintName.lastIndexOf('.') + 1));
			}
		}
		return false;
	}

}
