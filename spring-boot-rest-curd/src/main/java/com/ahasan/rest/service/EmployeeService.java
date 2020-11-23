package com.ahasan.rest.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ahasan.rest.dto.EmployeeDTO;
import com.ahasan.rest.entity.EmployeeEntity;
import com.ahasan.rest.repo.EmployeeRepo;

@Service
@Transactional
public class EmployeeService {

	@Autowired
	private EmployeeRepo employeeRepo;

	public List<EmployeeDTO> findEmployeeList() {
		return employeeRepo.findAll().stream().map(this::copyEmployeeEntityToDto).collect(Collectors.toList());
	}

	public EmployeeDTO findByEmployeeId(Long employeeId) {
		EmployeeEntity employeeEntity = employeeRepo.findByEmployeeId(employeeId);
		return copyEmployeeEntityToDto(employeeEntity);
	}

	public void createOrUpdateEmployee(EmployeeDTO employeeDTO) {
		EmployeeEntity employeeEntity = copyEmployeeDtoToEntity(employeeDTO);
		employeeRepo.save(employeeEntity);
	}

	public void deleteEmployeeById(Long employeeId) {
		employeeRepo.deleteById(employeeId);
	}

	private EmployeeDTO copyEmployeeEntityToDto(EmployeeEntity employeeEntity) {
		EmployeeDTO employeeDTO = new EmployeeDTO();
		BeanUtils.copyProperties(employeeEntity, employeeDTO);
		return employeeDTO;
	}

	private EmployeeEntity copyEmployeeDtoToEntity(EmployeeDTO employeeDTO) {
		EmployeeEntity employeeEntity = new EmployeeEntity();
		BeanUtils.copyProperties(employeeDTO, employeeEntity);
		return employeeEntity;
	}

}
