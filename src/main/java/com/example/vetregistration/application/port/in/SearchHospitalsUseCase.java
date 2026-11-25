package com.example.vetregistration.application.port.in;

import com.example.vetregistration.application.HospitalPage;

public interface SearchHospitalsUseCase {

	HospitalPage search(String keyword, int page, int size);

}
