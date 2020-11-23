package com.ahasan.rest.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ahasan.rest.entity.StrudentEntity;

public interface EmployeeRepo extends JpaRepository<StrudentEntity, Long> {

	public StrudentEntity findByEmployeeId(Long empId);

}
