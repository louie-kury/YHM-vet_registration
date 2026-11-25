package com.example.vetregistration.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vetregistration.application.port.in.SearchHospitalsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
public class HospitalController {

	private final SearchHospitalsUseCase searchHospitalsUseCase;

	public HospitalController(SearchHospitalsUseCase searchHospitalsUseCase) {
		this.searchHospitalsUseCase = searchHospitalsUseCase;
	}

	@GetMapping("/api/v1/hospitals")
	@Operation(summary = "병원 목록 및 검색", description = "병원명을 부분 검색합니다. 검색어가 없으면 목록을 조회합니다. "
			+ "병원명 오름차순으로 정렬하고 동명 병원은 ID 오름차순으로 정렬합니다.", responses = {
					@ApiResponse(responseCode = "200", description = "병원 목록 조회 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 페이지 번호 또는 페이지 크기", content = @Content)
			})
	public HospitalPageResponse search(
			@Parameter(description = "병원명에 포함된 검색어", example = "동물")
			@RequestParam(name = "keyword", required = false) String keyword,
			@Parameter(description = "0부터 시작하는 페이지 번호")
			@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
			@Parameter(description = "페이지당 병원 수 (1~100)")
			@RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
		return HospitalPageResponse.from(searchHospitalsUseCase.search(keyword, page, size));
	}

}
