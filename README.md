## RESTful Web Service using spring-boot, JPA, MySQL and Hibernate Validator


### Overview
* `REST API`:  is used to create API. We can define Request method to consume and produce desire output.
* `Spring Data JPA`: is a Java specification for managing relational data in Java applications. It allows us to access and persist data between Java object/ class and relational database.
* `Mysql`: MySQL is a database management system.
* `Hibernate-Validator`:  it used to runtime exception handling 

### tools you will need
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

### **A Simple Domain Class**

Let’s add a few validations to the employee bean. Note that we are using `@Size, @Index` validations.

**@Index Uniqueness**
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




###  spring-boot-rest-data-jpa project run
1. `git clone https://github.com/ahasanhabibsumon/spring-boot-rest-data-jpa.git`
2. `project import any IDE`
3. `Go to application.properties and make sure databasename, username, password`
4. `Run spring boot project`
5. `open postman and import in postman REST-API-CRUD.postman_collection file `


