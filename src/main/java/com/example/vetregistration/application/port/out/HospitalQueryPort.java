package com.example.vetregistration.application.port.out;

import com.example.vetregistration.application.HospitalPage;

public interface HospitalQueryPort {

	HospitalPage findPage(String keyword, int page, int size);

}
