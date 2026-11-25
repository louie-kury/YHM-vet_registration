package com.example.vetregistration.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.vetregistration.application.port.in.RegisterVeterinarianUseCase;
import com.example.vetregistration.application.port.in.RegisterVeterinarianUseCase.Result;
import com.example.vetregistration.config.LocalRegistrationEnabled;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@LocalRegistrationEnabled
public class VeterinarianRegistrationController {

	private final RegisterVeterinarianUseCase registerVeterinarianUseCase;

	public VeterinarianRegistrationController(RegisterVeterinarianUseCase registerVeterinarianUseCase) {
		this.registerVeterinarianUseCase = registerVeterinarianUseCase;
	}

	@PostMapping(value = "/api/v1/veterinarian-registrations", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "선택한 병원에 수의사 등록", description = "로컬 대체 계정 참조로 수의사와 최초 소속을 생성합니다. "
			+ "면허 미검증 상태(RESTRICTED)로 시작합니다.", responses = {
				@ApiResponse(responseCode = "201", description = "등록 성공",
						content = @Content(schema = @Schema(implementation = VeterinarianRegistrationResponse.class))),
				@ApiResponse(responseCode = "400", description = "잘못된 입력",
						content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
								schema = @Schema(implementation = ProblemDetail.class))),
				@ApiResponse(responseCode = "404", description = "선택한 병원이 존재하지 않음",
						content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
								schema = @Schema(implementation = ProblemDetail.class))),
				@ApiResponse(responseCode = "409", description = "이미 등록된 면허번호",
						content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
								schema = @Schema(implementation = ProblemDetail.class)))
			})
	public VeterinarianRegistrationResponse register(@Valid @RequestBody VeterinarianRegistrationRequest request) {
		Result result = registerVeterinarianUseCase.register(request.toCommand());
		return new VeterinarianRegistrationResponse(result.veterinarianUuid(), result.hospitalUuid());
	}

}
