package com.example.vetregistration.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:hospital-search;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"eureka.client.enabled=false"
})
@AutoConfigureMockMvc
@Sql(scripts = "/hospital-search-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class HospitalSearchIntegrationTest {

	private static final String PATH = "/api/v1/hospitals";
	private static final String FIRST_UUID = "12345678-1234-4234-a234-123456789010";
	private static final String SECOND_UUID = "12345678-1234-4234-a234-123456789020";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listsHospitalsInNameAndIdOrderWithDefaultPage() throws Exception {
		mockMvc.perform(get(PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.content.length()").value(3))
				.andExpect(jsonPath("$.content[0].uuid").value(FIRST_UUID))
				.andExpect(jsonPath("$.content[1].uuid").value(SECOND_UUID))
				.andExpect(jsonPath("$.content[2].name").value("나래동물병원"));
	}

	@Test
	void trimsKoreanKeywordAndReturnsAddressesForSameNameHospitals() throws Exception {
		mockMvc.perform(get(PATH).param("keyword", "  온동물  "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].name").value("가온동물병원"))
				.andExpect(jsonPath("$.content[0].roadAddress").value("서울 달길 10"))
				.andExpect(jsonPath("$.content[0].lotAddress").value("서울 달동 10"))
				.andExpect(jsonPath("$.content[0].phoneNumber").value("02-555-0010"))
				.andExpect(jsonPath("$.content[1].name").value("가온동물병원"))
				.andExpect(jsonPath("$.content[1].roadAddress").value("서울 별길 20"))
				.andExpect(jsonPath("$.content[1].lotAddress").value("서울 별동 20"));
	}

	@Test
	void treatsWhitespaceOnlyKeywordAsAbsent() throws Exception {
		mockMvc.perform(get(PATH).param("keyword", " \t "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(3));
	}

	@ParameterizedTest
	@ValueSource(strings = {"일치없음", "%", "_", "!"})
	void returnsEmptyPageForUnmatchedLiteralKeyword(String keyword) throws Exception {
		mockMvc.perform(get(PATH).param("keyword", keyword))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.totalPages").value(0));
	}

	@Test
	void movesBetweenPagesWithStableTiebreaker() throws Exception {
		mockMvc.perform(get(PATH).param("keyword", "가온").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].uuid").value(FIRST_UUID));
		mockMvc.perform(get(PATH).param("keyword", "가온").param("page", "1").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].uuid").value(SECOND_UUID))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void returnsEmptyContentAndPreservesTotalsWhenPageIsOutOfRange() throws Exception {
		mockMvc.perform(get(PATH).param("page", Integer.toString(Integer.MAX_VALUE)).param("size", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.page").value(Integer.MAX_VALUE))
				.andExpect(jsonPath("$.size").value(100))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(1));
	}

	@ParameterizedTest
	@CsvSource({"page, -1", "page, abc", "size, -1", "size, 0", "size, 101", "size, abc"})
	void rejectsInvalidPagination(String parameter, String value) throws Exception {
		mockMvc.perform(get(PATH).param(parameter, value)).andExpect(status().isBadRequest());
	}

}
