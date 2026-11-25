package com.example.vetregistration.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.vetregistration.application.VeterinarianRegistrationException.DuplicateLicense;
import com.example.vetregistration.application.VeterinarianRegistrationException.HospitalNotFound;

@RestControllerAdvice(assignableTypes = VeterinarianRegistrationController.class)
class VeterinarianRegistrationExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(VeterinarianRegistrationExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail invalidFields(MethodArgumentNotValidException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"필수 입력값, 길이 또는 UUID 형식을 확인해 주세요.");
		problem.setProperty("invalidFields", exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField())
				.distinct()
				.sorted()
				.toList());
		return problem;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail invalidBody() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 본문의 JSON 필드와 형식을 확인해 주세요.");
	}

	@ExceptionHandler(HospitalNotFound.class)
	ProblemDetail hospitalNotFound() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "선택한 병원을 찾을 수 없습니다.");
	}

	@ExceptionHandler(DuplicateLicense.class)
	ProblemDetail duplicateLicense() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "이미 등록된 면허번호입니다.");
	}

	@ExceptionHandler(DataAccessException.class)
	ProblemDetail storageFailure(DataAccessException exception) {
		logger.error("Veterinarian registration persistence failed ({})", exception.getClass().getSimpleName());
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "수의사 정보를 저장할 수 없습니다.");
	}

}
