RESTful Web Service using spring-boot, JPA, MySQL and Hibernate Validator


### Overview
* `REST API`:  is used to create API. We can define Request method to consume and produce desire output.
* `Spring Data JPA`: is a Java specification for managing relational data in Java applications. It allows us to access and persist data between Java object/ class and relational database.
* `Mysql`: MySQL is a database management system.
* `Hibernate-Validator`:  it used to runtime exception handling 

### Tools you will need
* Maven 3.0+ is your build tool
* Your favorite IDE but we will recommend `STS-4-4.4.1 version`. We use STS.
* MySQL Server
* JDK 1.8+

### Maven Dependencies
In this case, we'll learn how to validate domain objects in Spring Boot **by building a basic REST controller**
The controller will first take a domain object, then it will validate it with Hibernate Validator, and finally it will persist it into an `MySQL` database.
The project's dependencies are fairly standard:

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<scope>runtime</scope>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
	<groupId>org.hibernate.validator</groupId>
	<artifactId>hibernate-validator</artifactId>
</dependency>
```
As shown above, we included `spring-boot-starter-web` in our `pom.xml` file because we'll need it for creating the REST controller. Additionally, let's make sure to check the latest versions of `spring-boot-starter-jpa` and the `mysql-connector-java` on Maven Central.
And we also need to explicitly add the `hibernate-validator` dependency for enable validation.

### Simple Domain Class

Let’s add a few validations to the employee bean. Note that we are using `@Size, @Index` validations. We are using `@Size` to specify the minimum length and also a message when a validation error occurs.

##### @Index Uniqueness

The last optional parameter is a unique attribute, which defines whether the index is unique. A unique index ensures that the indexed fields don't store duplicate values. By default, it's false. If we want to change it, we can declare:

```
@Index(name = "uniqueIndex", columnList = "columnName", unique = true)
```

```
[main] DEBUG org.hibernate.SQL -
  alter table Student add constraint uniqueIndex unique (firstName)
```


```
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "employee",
		indexes = {@Index(columnList = "emp_phone", unique = true, name = "number")}
)
public class EmployeeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "emp_id")
	private Long employeeId;

	@Size(max = 20, min = 1, message = "employee name must be equal or less than '{max}'")
	@Column(name = "emp_name")
	private String employeeName;

	@Size(max = 6, min = 1, message = "employee gender must be equal or less than '{max}'")
	@Column(name = "emp_gender")
	private String employeeGender;

	@Size(max = 14, min = 1, message = "employee phone must be equal or less than '{max}'")
	@Column(name = "emp_phone")
	private String employeePhone;

}
```

### Create Spring JPA Repository

```
import org.springframework.data.jpa.repository.JpaRepository;
import com.ahasan.rest.entity.EmployeeEntity;

public interface EmployeeRepo extends JpaRepository<EmployeeEntity, Long> {
	public EmployeeEntity findByEmployeeId(Long empId);
}
```

Let's configure MySQL properties in an **application.properties**

```
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/spring_rest?useSSL=false&createDatabaseIfNotExist=true
spring.datasource.username=[username]
spring.datasource.password=[password]

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL57Dialect
spring.jpa.generate-ddl=true
spring.jpa.show-sql=true
server.port=8082
```

### Implementing a REST Controller

We will build a simple REST controller for our example. Create a new package package `com.ahasan.rest.controller` and class `EmployeeController`

```
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ahasan.rest.common.messages.BaseResponse;
import com.ahasan.rest.dto.EmployeeDTO;
import com.ahasan.rest.service.EmployeeService;

@Validated
@RestController
@RequestMapping("/employee")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	@GetMapping(value = "/find")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
		List<EmployeeDTO> list = employeeService.findEmployeeList();
		return new ResponseEntity<List<EmployeeDTO>>(list, HttpStatus.OK);
	}

	@GetMapping(value = "/find/by-id")
	public ResponseEntity<EmployeeDTO> getEmployeeById(@NotNull(message = "Id can't be null") @RequestParam Long id) {
		EmployeeDTO list = employeeService.findByEmployeeId(id);
		return new ResponseEntity<EmployeeDTO>(list, HttpStatus.OK);
	}

	@PostMapping(value = { "/add", "/update" })
	public ResponseEntity<BaseResponse> createOrUpdateEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
		BaseResponse response = employeeService.createOrUpdateEmployee(employeeDTO);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<BaseResponse> deleteEmployeeById(@PathVariable("id") Long id) {
		BaseResponse response = employeeService.deleteEmployeeById(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
```

### Implementing a Service Class

```
package com.ahasan.rest.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ahasan.rest.common.exceptions.CustomDataIntegrityViolationException;
import com.ahasan.rest.common.exceptions.RecordNotFoundException;
import com.ahasan.rest.common.messages.BaseResponse;
import com.ahasan.rest.common.messages.CustomMessage;
import com.ahasan.rest.common.utils.Topic;
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
		if (employeeRepo.existsById(employeeId)) {
			EmployeeEntity employeeEntity = employeeRepo.findByEmployeeId(employeeId);
			return copyEmployeeEntityToDto(employeeEntity);
		}else {
			throw new RecordNotFoundException(CustomMessage.DOESNOT_EXIT + employeeId);
		}
	}

	public BaseResponse createOrUpdateEmployee(EmployeeDTO employeeDTO) {
		try {
			EmployeeEntity employeeEntity = copyEmployeeDtoToEntity(employeeDTO);
			employeeRepo.save(employeeEntity);
		}  catch (DataIntegrityViolationException ex) {
			throw new CustomDataIntegrityViolationException(ex.getCause().getCause().getMessage());
		}
		return new BaseResponse(Topic.EMPLOYEE.getName() + CustomMessage.SAVE_SUCCESS_MESSAGE);
	}

	public BaseResponse deleteEmployeeById(Long employeeId) {
		if (employeeRepo.existsById(employeeId)) {
			employeeRepo.deleteById(employeeId);
		} else {
			throw new RecordNotFoundException(CustomMessage.NO_RECOURD_FOUND + employeeId);
		}
		return new BaseResponse(Topic.EMPLOYEE.getName() + CustomMessage.DELETE_SUCCESS_MESSAGE);
	
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

```



### Customizing Validation Response

Let’s define a simple validation response bean.

```

public class ErrorResponse {
	public ErrorResponse(String message, List<String> details) {
		super();
		this.message = message;
		this.details = details;
	}
	
	private String message;
	private List<String> details;

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getDetails() {
		return details;
	}
	public void setDetails(List<String> details) {
		this.details = details;
	}
}
```

### Create GlobalExceptionHandler.java class

```
package com.ahasan.rest.common.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

	private String INCORRECT_REQUEST = "INCORRECT_REQUEST";
	private String BAD_REQUEST = "BAD_REQUEST";
	private String CONFLICT = "CONFLICT";

	@ExceptionHandler(RecordNotFoundException.class)
	public final ResponseEntity<ErrorResponse> handleUserNotFoundException(RecordNotFoundException ex, WebRequest request) {
		List<String> details = new ArrayList<>();
		details.add(ex.getLocalizedMessage());
		ErrorResponse error = new ErrorResponse(INCORRECT_REQUEST, details);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MissingHeaderInfoException.class)
	public final ResponseEntity<ErrorResponse> handleInvalidTraceIdException(MissingHeaderInfoException ex, WebRequest request) {
		List<String> details = new ArrayList<>();
		details.add(ex.getLocalizedMessage());
		ErrorResponse error = new ErrorResponse(BAD_REQUEST, details);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public final ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
		List<String> details = ex.getConstraintViolations().parallelStream()
					.map(e -> e.getMessage()).collect(Collectors.toList());
		ErrorResponse error = new ErrorResponse(BAD_REQUEST, details);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CustomDataIntegrityViolationException.class)
	public final ResponseEntity<ErrorResponse> dataIntegrityViolationException(CustomDataIntegrityViolationException ex, WebRequest request) {
		String[] detail = ex.getLocalizedMessage().split("Detail: Key ");
		ErrorResponse error = new ErrorResponse(CONFLICT, Arrays.asList(detail));
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}
}
```

### Exception(Error) Handling for RESTful Services

**1.1** Spring Boot provides a good default implementation for exception handling for RESTful Services.Let’s quickly look at the default Exception Handling features provided by Spring Boot.

Resource Not Present Heres what happens when you fire a request to a resource not 

found: `http://localhost:8082/employee/save`

```
{
    "timestamp": "2020-12-01T06:33:14.020+0000",
    "status": 404,
    "error": "Not Found",
    "message": "No message available",
    "path": "/employee/save"
}
```

**1.2** if the `@Validated` is failed, it will trigger a `ConstraintViolationException`, we can override the error code like this :

```
@ExceptionHandler(ConstraintViolationException.class)
public final ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
	List<String> details = ex.getConstraintViolations().parallelStream()
					.map(e -> e.getMessage()).collect(Collectors.toList());
	ErrorResponse error = new ErrorResponse(BAD_REQUEST, details);
	return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
}
```

API Testing using `curl`

```
curl --location --request POST 'http://localhost:8082/employee/add' \
--header 'Content-Type: application/json' \
--data-raw '{
    "employeeName": "MD. ahasan habib sumon",
    "employeeGender": "femalea",
    "employeePhone": "2222"
}'
```
Output Message
```
{
    "message": "BAD_REQUEST",
    "details": [
        "employee name must be equal or less than '20'",
        "employee gender must be equal or less than '6'"
    ]
}
```

**1.3  Record Not Found Validation Path Variables  and Request Param ** 

If given value not found from database then will show **RecordNotFoundException**  for  `Path Variables`  and `Request Param`:

```
@ExceptionHandler(RecordNotFoundExcException(Error) Handling for RESTful Serviceseption.class)
public final ResponseEntity<ErrorResponse> handleUserNotFoundException(RecordNotFoundException ex, WebRequest request) {
	List<String> details = new ArrayList<>();
	details.add(ex.getLocalizedMessage());
	ErrorResponse error = new ErrorResponse(INCORRECT_REQUEST, details);
	return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
}
```

```
package com.ahasan.rest.common.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public RecordNotFoundException(String message) {
        super(message);
    }
}
```

The default error message is good, just the error code 404 is not suitable.

```
curl --location --request GET 'http://localhost:8082/employee/find/by-id?id=12'

{
    "message": "INCORRECT_REQUEST",
    "details": [
        "Does not exist for given value: 12"
    ]
}
```

**1.4  Request Param Validation**

Apply `@Validated` on class level, and add the `javax.validation.constraints.*` annotations on `Request Param` like this :

```
import org.springframework.validation.annotation.Validated;

@RestController
@Validated // class level
public class BookController {
	@GetMapping(value = "/find/by-id")
	public ResponseEntity<EmployeeDTO> getEmployeeById(
						@NotNull(message = "Id can't be null") @RequestParam Long id) {
		EmployeeDTO list = employeeService.findByEmployeeId(id);
		return new ResponseEntity<EmployeeDTO>(list, HttpStatus.OK);
	}
}
```

The default error message, just the error code 400 is not suitable.

```
curl --location --request GET 'http://localhost:8082/employee/find/by-id?id='

{
    "message": "BAD_REQUEST",
    "details": [
        "Id can't be null"
    ]
}
```





###  spring-boot-rest-data-jpa project run

1. `git clone https://github.com/ahasanhabibsumon/spring-boot-rest-data-jpa.git`
2. `project import any IDE`
3. `Go to application.properties and make sure databasename, username, password`
4. `Run spring boot project`
5. `open postman and import in postman REST-API-CRUD.postman_collection file `


