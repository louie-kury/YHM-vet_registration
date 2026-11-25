package com.example.vetregistration.adapter.out.persistence;

import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hospital")
@Immutable
public class HospitalEntity {

	@Id
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, nullable = false)
	private UUID uuid;

	@Column(length = 100, nullable = false)
	private String name;

	@Column(name = "road_address", length = 255)
	private String roadAddress;

	@Column(name = "lot_address", length = 255)
	private String lotAddress;

	@Column(name = "tel", length = 20)
	private String phoneNumber;

	protected HospitalEntity() {
	}

	UUID getUuid() {
		return uuid;
	}

	String getName() {
		return name;
	}

	String getRoadAddress() {
		return roadAddress;
	}

	String getLotAddress() {
		return lotAddress;
	}

	String getPhoneNumber() {
		return phoneNumber;
	}

}
