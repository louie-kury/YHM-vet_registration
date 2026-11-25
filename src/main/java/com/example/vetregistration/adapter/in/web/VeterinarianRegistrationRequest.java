package com.example.vetregistration.adapter.in.web;

import java.util.UUID;

import org.hibernate.validator.constraints.CodePointLength;

import com.example.vetregistration.application.port.in.RegisterVeterinarianUseCase.Command;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

public record VeterinarianRegistrationRequest(
		@NotBlank @CodePointLength(max = 50) @JsonDeserialize(using = TextDeserializer.class)
		@Schema(description = "이름", maxLength = 50, example = "김수의") String name,
		@NotBlank @CodePointLength(max = 100) @JsonDeserialize(using = TextDeserializer.class)
		@Schema(description = "전공", maxLength = 100, example = "수의내과학") String major,
		@NotBlank @CodePointLength(max = 100) @JsonDeserialize(using = TextDeserializer.class)
		@Schema(description = "전문분야", maxLength = 100, example = "반려동물 내과") String specialty,
		@NotBlank @CodePointLength(max = 100) @JsonDeserialize(using = TextDeserializer.class)
		@Schema(description = "면허번호", maxLength = 100, example = "VET-TEST-001") String licenseNo,
		@NotBlank @Pattern(regexp = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")
		@JsonDeserialize(using = TextDeserializer.class)
		@Schema(description = "선택한 병원 UUID", format = "uuid") String hospitalUuid) {

	public VeterinarianRegistrationRequest {
		name = strip(name);
		major = strip(major);
		specialty = strip(specialty);
		licenseNo = strip(licenseNo);
		hospitalUuid = strip(hospitalUuid);
	}

	Command toCommand() {
		return new Command(name, major, specialty, licenseNo, UUID.fromString(hospitalUuid));
	}

	@JsonAnySetter
	void rejectUnexpectedField(String name, Object value) {
		throw new IllegalArgumentException("Unknown registration field");
	}

	private static String strip(String value) {
		if (value == null) {
			return null;
		}
		int start = 0;
		int end = value.length();
		while (start < end && isWhitespace(value.charAt(start))) {
			start++;
		}
		while (end > start && isWhitespace(value.charAt(end - 1))) {
			end--;
		}
		return value.substring(start, end);
	}

	private static boolean isWhitespace(char character) {
		return Character.isWhitespace(character) || Character.isSpaceChar(character) || character == '\u0085';
	}

	public static final class TextDeserializer extends StdScalarDeserializer<String> {

		public TextDeserializer() {
			super(String.class);
		}

		@Override
		public String deserialize(JsonParser parser, DeserializationContext context) {
			if (parser.hasToken(JsonToken.VALUE_STRING)) {
				return parser.getString();
			}
			return (String) context.handleUnexpectedToken(String.class, parser);
		}

	}

}
