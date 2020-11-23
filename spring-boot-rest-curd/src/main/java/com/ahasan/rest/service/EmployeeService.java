package com.ahasan.rest.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ahasan.rest.dto.EmployeeDTO;
import com.ahasan.rest.entity.StrudentEntity;
import com.ahasan.rest.repo.EmployeeRepo;

@Service
@Transactional
public class EmployeeService {

	@Autowired
	private EmployeeRepo employeeRepo;

	public List<EmployeeDTO> findEmpList() {
		return employeeRepo.findAll().stream().map(this::copyEmployeEntityToDto).collect(Collectors.toList());
	}

	public EmployeeDTO findByEmpId(Long empId) {
		StrudentEntity employeeEntity = employeeRepo.findByEmployeeId(empId);
		return copyEmployeEntityToDto(employeeEntity);
	}

	public void createOrUpdateEmployee(EmployeeDTO employeeDTO) {
		StrudentEntity employeeEntity = copyEmployeDtoToEntity(employeeDTO);
		employeeRepo.save(employeeEntity);
	}

	public void deleteEmployee(Long empId) {
		employeeRepo.deleteById(empId);
	}

	private EmployeeDTO copyEmployeEntityToDto(StrudentEntity employeeEntity) {
		EmployeeDTO employeeDTO = new EmployeeDTO();
		BeanUtils.copyProperties(employeeEntity, employeeDTO);
		return employeeDTO;
	}

	private StrudentEntity copyEmployeDtoToEntity(EmployeeDTO employeeDTO) {
		StrudentEntity employeeEntity = new StrudentEntity();
		BeanUtils.copyProperties(employeeDTO, employeeEntity);
		return employeeEntity;
	}

}
