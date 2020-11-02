package com.spring.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spring.rest.controller.EmployeeController;
import com.spring.rest.service.EmployeeService;

@ExtendWith(MockitoExtension.class)
class ApplicationTester {
	
	@InjectMocks
	EmployeeController employeeController;
	
	@Mock
	EmployeeService employeeService;

	@Test
	void contextLoads() {
	}

}
