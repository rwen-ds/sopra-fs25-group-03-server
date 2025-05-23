# KindBridge

## Introduction

KindBridge is a community-driven platform designed to connect students who need help with those willing to lend a hand.
Many students face difficulties in finding accessible opportunities to contribute to social causes or get help with
everyday tasks, such as pet care, airport pickups, or advice on student life abroad. Our motivation is to provide a
transparent, efficient, and user-friendly platform that bridges the gap between those who need help and those who can
offer it, promoting kindness and support within the student community.

## Technologies

- **Google Translate API** – Assists users in translating messages.

## High-level components

### 1. REST API Layer (Controllers)
- **Role**: Handle HTTP requests, validate input parameters, and delegate calls to the corresponding service layer. Acts as the interface layer between frontend and backend, responsible for routing and data transfer.
- **Key Files**: 
  - [`RequestController`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/Requestcontroller.java) - Manages help request related API endpoints
  - [`UserController`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/Usercontroller.java) - Handles user authentication and user management
  - [`MessageController`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/MessageController.java) - Processes inter-user message communication
- **Related To**: `Service Layer`

### 2. Business Logic Layer (Services)  
- **Role**: Implement core business logic and workflows such as user registration, JWT authentication, request creation/updates, notification management, etc. Coordinate transactional operations across multiple repositories.
- **Key Files**:
  - [`UserService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java) - User management and authentication logic
  - [`RequestService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/RequestService.java) - Help request business logic
  - [`MessageService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/MessageService.java) - Message processing and management
  - [`NotificationService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/NotificationService.java) - Notification management, handles request status change notifications
- **Related To**: `Controllers`, `Repositories`, `External Services`

### 3. Data Access Layer (Repositories)
- **Role**: Provide database abstraction layer using Spring Data JPA for CRUD operations and custom queries. Manage entity lifecycle to optimize performance.
- **Key Files**: 
  - [`UserRepository`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/UserRepository.java) - User data access
  - [`RequestRepository`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/RequestRepository.java) - Request data access
  - [`MessageRepository`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/MessageRepository.java) - Message data access
  - [`NotificationRepository`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/NotificationRepository.java) - Notification data access
- **Related To**: `Services`, `Entities`

### 4. Data Model Layer (Entities)
- **Role**: Define domain models using JPA annotations for object-relational mapping. Represent the core data structures and entity relationships of the application.
- **Key Files**:
  - [`User`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/User.java) - User entity
  - [`Request`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Request.java) - Help request entity
  - [`Message`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Message.java) - Message entity
  - [`Notification`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Notification.java) - Notification entity
- **Related To**: `Repositories`

### 5. External Integration Layer
- **Role**: Integrate external services such as Google Cloud Translation API to provide additional functionality. Handle third-party API calls and error handling.
- **Key Files**: 
  - [`TranslationService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/TranslationService.java) - Google Translate API integration, supports multilingual message translation
- **Related To**: `Business Logic Layer`

## Launch & Deployment

To get started with the application, follow these steps:

1. **Clone the Repository**:
   Clone the repository to your local machine using the following command:
   ```bash
   git clone https://github.com/rwen-ds/sopra-fs25-group-03-server.git
   ```
2. **Build the Project**:
   Navigate to the project directory and run the following command to build the application:
    ```bash
   ./gradlew build
   ```
3. **Run the Application**:
   After the build is complete, start the backend application with the following command:
    ```bash
   ./gradlew bootRun
   ```
4. **Run Tests**:
   Run the tests with the following command:
   ```bash
   ./gradlew cleanTest test
   ```
5. **Deployment**:
   There are no additional dependencies or databases required to run the project locally. Once the changes are pushed to
   the GitHub repository, they will automatically trigger the deployment process.

## Roadmap

- **Multi-Volunteers**
  Implement a feature that allows multiple volunteers to be assigned to a single request. Volunteers can be able to
  collaborate on tasks, track their progress, and interact with each other and the requester seamlessly. This would
  enable users to get assistance from multiple volunteers for complex or time-sensitive requests, improving the overall
  efficiency of the platform and providing users with more support options.

- **Group Chat**
  Introduce a group chat feature where multiple users (requesters and volunteers) can communicate in real-time. The chat
  can allow seamless communication between all parties involved in a request.

## Authors and acknowledgment

* **Rong Wen**  - [@rwen-ds](https://github.com/rwen-ds)
* **Qinrui Deng** - [@mia-aiden](https://github.com/mia-aiden)
* **Yanjun Guo** - [@YanjunGuo1007](https://github.com/YanjunGuo1007)
* **Nanxin Wang** - [@adriaWG](https://github.com/adriaWG)

Special thanks to:

- [Google Maps API](https://developers.google.com/maps) for providing the maps feature.
- [Google Translate API](https://cloud.google.com/translate) for enabling multilingual support.
- **Our TA** for the guidance and feedback during the development process.
- **Sopra Team** for their support and insights throughout the project.
- **Group 5** for their valuable feedback.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.