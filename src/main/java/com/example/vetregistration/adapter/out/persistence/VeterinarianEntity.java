package com.example.vetregistration.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.vetregistration.domain.VeterinarianRegistration;
import com.example.vetregistration.domain.VeterinarianRegistration.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "veterinarian")
public class VeterinarianEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, nullable = false)
	private UUID uuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "account_uuid", length = 36, nullable = false)
	private UUID accountUuid;

	@Column(length = 50, nullable = false)
	private String name;

	@Column(length = 100, nullable = false)
	private String major;

	@Column(length = 100, nullable = false)
	private String specialty;

	@Column(name = "license_no", length = 100, nullable = false)
	private String licenseNo;

	@Column(name = "is_available", nullable = false)
	private boolean available;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private Status status;

	@Column(name = "status_reason", length = 255)
	private String statusReason;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected VeterinarianEntity() {
	}

	VeterinarianEntity(VeterinarianRegistration registration) {
		this.uuid = registration.uuid();
		this.accountUuid = registration.accountUuid();
		this.name = registration.name();
		this.major = registration.major();
		this.specialty = registration.specialty();
		this.licenseNo = registration.licenseNo();
		this.available = registration.available();
		this.status = registration.status();
		this.statusReason = registration.statusReason();
		this.createdAt = registration.registeredAt();
		this.updatedAt = registration.registeredAt();
	}

	Long getId() {
		return id;
	}

}
