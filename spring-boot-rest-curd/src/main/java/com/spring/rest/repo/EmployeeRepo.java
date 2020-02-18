package com.spring.rest.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.rest.entity.EmployeeEntity;

public interface EmployeeRepo extends JpaRepository<EmployeeEntity, Long> {

	public EmployeeEntity findByEmployeeId(Long empId);

}
