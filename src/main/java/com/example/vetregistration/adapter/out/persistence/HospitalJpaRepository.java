package com.example.vetregistration.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

interface HospitalJpaRepository extends Repository<HospitalEntity, Long> {

	String NAME_FILTER = " where (:pattern is null or h.name like :pattern escape '!')";

	@Query(value = "select count(*) from hospital h" + NAME_FILTER, nativeQuery = true)
	long countMatches(@Param("pattern") String pattern);

	@Query(value = "select h.id, h.uuid, h.name, h.road_address, h.lot_address, h.tel from hospital h"
			+ NAME_FILTER + " order by h.name asc, h.id asc limit :size offset :offset", nativeQuery = true)
	List<HospitalEntity> findMatches(@Param("pattern") String pattern, @Param("size") int size,
			@Param("offset") long offset);

}
