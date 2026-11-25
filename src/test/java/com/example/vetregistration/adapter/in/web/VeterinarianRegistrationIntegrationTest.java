package com.example.vetregistration.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.vetregistration.application.port.out.RegistrationAccountPort;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:vet-registration;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"eureka.client.enabled=false",
		"vet-registration.local-account.enabled=true"
})
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Sql(scripts = {"/hospital-search-test.sql", "/veterinarian-registration-test.sql",
		"file:db/migrations/001-veterinarian-license-unique.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class VeterinarianRegistrationIntegrationTest {

	private static final String PATH = "/api/v1/veterinarian-registrations";
	private static final String HOSPITAL_UUID = "12345678-1234-4234-a234-123456789010";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoSpyBean
	private RegistrationAccountPort accountPort;

	@BeforeEach
	void clearRegistrations() {
		jdbcTemplate.update("delete from vet_hospital_affiliation");
		jdbcTemplate.update("delete from veterinarian");
	}

	@Test
	void persistsVetAndAffiliationWithServerAssignedState() throws Exception {
		ObjectNode request = request(" \tVET-001\t ");
		request.put("name", "\u2002김수의\u00A0");
		request.put("major", " " + "수".repeat(100) + " ");
		request.put("specialty", " 반려동물 내과 ");
		request.put("hospitalUuid", " " + HOSPITAL_UUID + " ");

		String response = submit(request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.hospitalUuid").value(HOSPITAL_UUID))
				.andExpect(jsonPath("$.accountUuid").doesNotExist())
				.andReturn().getResponse().getContentAsString();

		String veterinarianUuid = jsonMapper.readTree(response).path("veterinarianUuid").asString();
		assertThat(UUID.fromString(veterinarianUuid)).isNotNull();
		Map<String, Object> stored = jdbcTemplate.queryForMap("""
				select v.uuid, v.account_uuid, v.name, v.major, v.specialty, v.license_no, v.is_available,
				       v.status, v.status_reason, v.created_at, v.updated_at, a.uuid as affiliation_uuid,
				       a.started_at, a.ended_at, a.current_flag, h.uuid as hospital_uuid
				from veterinarian v
				join vet_hospital_affiliation a on a.vet_id = v.id
				join hospital h on h.id = a.hospital_id
				""");
		assertThat(stored)
				.containsEntry("uuid", veterinarianUuid)
				.containsEntry("name", "김수의")
				.containsEntry("major", "수".repeat(100))
				.containsEntry("specialty", "반려동물 내과")
				.containsEntry("license_no", "VET-001")
				.containsEntry("status", "RESTRICTED")
				.containsEntry("status_reason", "LICENSE_UNVERIFIED")
				.containsEntry("hospital_uuid", HOSPITAL_UUID)
				.containsEntry("ended_at", null);
		assertThat(((Number) stored.get("is_available")).intValue()).isZero();
		assertThat(((Number) stored.get("current_flag")).intValue()).isOne();
		assertThat(stored.get("created_at")).isNotNull().isEqualTo(stored.get("updated_at"))
				.isEqualTo(stored.get("started_at"));
		assertThat(UUID.fromString((String) stored.get("account_uuid"))).isNotNull();
		assertThat(UUID.fromString((String) stored.get("affiliation_uuid"))).isNotNull();
		assertRegistrationCount(1);
	}

	@ParameterizedTest
	@MethodSource("invalidFields")
	void rejectsInvalidFieldValues(String field, String value) throws Exception {
		ObjectNode request = request("VET-INVALID");
		if (value == null) {
			request.remove(field);
		} else {
			request.put(field, value);
		}

		submit(request).andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.invalidFields[0]").value(field));
		assertRegistrationCount(0);
	}

	static Stream<Arguments> invalidFields() {
		return Stream.of(
				Arguments.of("name", (String) null),
				Arguments.of("major", (String) null),
				Arguments.of("specialty", (String) null),
				Arguments.of("licenseNo", (String) null),
				Arguments.of("hospitalUuid", (String) null),
				Arguments.of("name", " \u00A0\t"),
				Arguments.of("licenseNo", " "),
				Arguments.of("name", " 수".repeat(26)),
				Arguments.of("major", "수".repeat(101)),
				Arguments.of("specialty", "수".repeat(101)),
				Arguments.of("licenseNo", "1".repeat(101)),
				Arguments.of("hospitalUuid", "invalid-uuid"),
				Arguments.of("hospitalUuid", "1-1-1-1-1"));
	}

	@Test
	void rejectsMalformedJsonAndNonStringField() throws Exception {
		mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content("{"))
				.andExpect(status().isBadRequest());
		submit(request("VET-INVALID").put("licenseNo", 123))
				.andExpect(status().isBadRequest());
		assertRegistrationCount(0);
	}

	@Test
	void rejectsClientSuppliedAccountUuid() throws Exception {
		submit(request("VET-INVALID").put("accountUuid", UUID.randomUUID().toString()))
				.andExpect(status().isBadRequest());
		assertRegistrationCount(0);
	}

	@Test
	void returnsNotFoundWhenHospitalDoesNotExist() throws Exception {
		submit(request("VET-404").put("hospitalUuid", "12345678-1234-4234-a234-123456789099"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
		assertRegistrationCount(0);
	}

	@Test
	void returnsConflictForAnExistingLicenseNumber() throws Exception {
		submit(request("VET-DUP")).andExpect(status().isCreated());
		submit(request(" VET-DUP ")).andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
		assertRegistrationCount(1);
	}

	@Test
	void databaseRejectsOneConcurrentRequestAfterBothPassPrecheck() throws Exception {
		CyclicBarrier prechecksComplete = new CyclicBarrier(2);
		doAnswer(invocation -> {
			prechecksComplete.await(10, TimeUnit.SECONDS);
			return invocation.callRealMethod();
		}).when(accountPort).resolveAccountUuid();

		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<Integer> first = executor.submit(() -> submit(request("VET-RACE")).andReturn().getResponse().getStatus());
			Future<Integer> second = executor.submit(() -> submit(request("VET-RACE")).andReturn().getResponse().getStatus());
			assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
					.containsExactlyInAnyOrder(201, 409);
			assertRegistrationCount(1);
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void rollsBackVeterinarianWhenAffiliationInsertFails() throws Exception {
		submit(request("VET-ROLLBACK").put("hospitalUuid", "12345678-1234-4234-a234-123456789030"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.detail").value("수의사 정보를 저장할 수 없습니다."))
				.andExpect(jsonPath("$.trace").doesNotExist());
		assertRegistrationCount(0);
		submit(request("VET-ROLLBACK")).andExpect(status().isCreated());
		assertRegistrationCount(1);
	}

	@Test
	void exposesOpenApiForRegistration() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['" + PATH + "'].post.responses['201']").exists())
				.andExpect(jsonPath("$.paths['" + PATH + "'].post.responses['400']").exists())
				.andExpect(jsonPath("$.paths['" + PATH + "'].post.responses['404']").exists())
				.andExpect(jsonPath("$.paths['" + PATH + "'].post.responses['409']").exists())
				.andExpect(jsonPath("$.components.schemas.VeterinarianRegistrationRequest.properties.hospitalUuid.format")
						.value("uuid"))
				.andExpect(jsonPath("$.components.schemas.VeterinarianRegistrationRequest.properties.accountUuid")
						.doesNotExist());
	}

	private ResultActions submit(ObjectNode body) throws Exception {
		return mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsBytes(body)));
	}

	private ObjectNode request(String licenseNo) {
		return jsonMapper.createObjectNode()
				.put("name", "김수의")
				.put("major", "수의내과학")
				.put("specialty", "반려동물 내과")
				.put("licenseNo", licenseNo)
				.put("hospitalUuid", HOSPITAL_UUID);
	}

	private void assertRegistrationCount(int count) {
		assertThat(jdbcTemplate.queryForObject("select count(*) from veterinarian", Integer.class)).isEqualTo(count);
		assertThat(jdbcTemplate.queryForObject("select count(*) from vet_hospital_affiliation", Integer.class))
				.isEqualTo(count);
	}

}
