# Project
This is a spring boot auth project. The project is a todo list application with 
authentication and authorization. The project is built using spring boot. The project is a simple todo list application that allows users to create, read, update, and delete 
their todo items. The project has 3 level of users: ADMIN, USER and OPERATOR. The ADMIN user can access 
all the endpoints, the USER can access only their own todo items, and the OPERATOR register users.

# Requirements
- User registration and login
- Create, read, update, and delete todo items
- Authentication and authorization using spring security
- In-memory h2 database for development and testing
- Lombok for reducing boilerplate code
- Gradle for build automation
- Mapstruct for mapping between entities and DTOs
- Environment variables for configuration
- Validation using javax.validation
- Exception handling using @ControllerAdvice
- Logging using slf4j and logback
- Pagination and sorting for todo items

# Rules
 - every use `this` or `super` to reference the current and parent class respectively.

# Project Structure
- config, controller, dto, entity, enumeration, exception, gateway, mapper, repository e service.