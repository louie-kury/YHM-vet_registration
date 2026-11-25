package com.example.vetregistration.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.vetregistration.domain.VeterinarianRegistration.InitialAffiliation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vet_hospital_affiliation")
public class VetHospitalAffiliationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, nullable = false)
	private UUID uuid;

	@Column(name = "vet_id", nullable = false)
	private Long vetId;

	@Column(name = "hospital_id", nullable = false)
	private Long hospitalId;

	@Column(name = "started_at", nullable = false)
	private LocalDateTime startedAt;

	@Column(name = "ended_at")
	private LocalDateTime endedAt;

	@JdbcTypeCode(SqlTypes.TINYINT)
	@Column(name = "current_flag")
	private Integer currentFlag;

	protected VetHospitalAffiliationEntity() {
	}

	VetHospitalAffiliationEntity(long vetId, long hospitalId, InitialAffiliation affiliation) {
		this.uuid = affiliation.uuid();
		this.vetId = vetId;
		this.hospitalId = hospitalId;
		this.startedAt = affiliation.startedAt();
		this.endedAt = affiliation.endedAt();
		this.currentFlag = affiliation.currentFlag();
	}

}
