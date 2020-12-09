## RESTful Web Service using spring-boot, JPA, MySQL and Hibernate Validator


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

When we create an index in that way, we add a uniqueness constraint on our columns, similarly, how as a unique attribute on `@Column` annotation do.` @Index` has an advantage over `@Column` due to the possibility to declare multi-column unique constraint:

```
@Index(name = "uniqueMulitIndex", columnList = "firstName, lastName", unique = true)
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
import org.springframework.validation.annotation.Validated;
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




###  spring-boot-rest-data-jpa project run
1. `git clone https://github.com/ahasanhabibsumon/spring-boot-rest-data-jpa.git`
2. `project import any IDE`
3. `Go to application.properties and make sure databasename, username, password`
4. `Run spring boot project`
5. `open postman and import in postman REST-API-CRUD.postman_collection file `


