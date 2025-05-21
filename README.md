# KindBridge

## Introduction

KindBridge is a community-driven platform designed to connect students who need help with those willing to lend a hand. Many students face difficulties in finding accessible opportunities to contribute to social causes or get help with everyday tasks, such as pet care, airport pickups, or advice on student life abroad. Our motivation is to provide a transparent, efficient, and user-friendly platform that bridges the gap between those who need help and those who can offer it, promoting kindness and support within the student community. 

## Technologies

- **Long Polling** – For real-time message.
- **Google Translate API** – Assist users in translating messages.
- **Google Map API** - Assist users in understanding requests
  

## High-level components

-  **Request Market**
   - **Role**: Displays all available requests and features an interactive map using the Google Maps JavaScript API, allowing users to view request locations.
   - **Key File**: [`RequestService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/RequestService.java)
   - **Related To**: `Request` 

- **Request**
   - **Role**: Represents the details of a request. Allows users to view profiles of both the request creator and volunteer. Additionally, it can open Google Maps based on the selected location.
   - **Key File**: [`RequestService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/RequestService.java)
   - **Related To**: `Request Market` , `Profile` 
  
- **Profile**
   - **Role**: Displays a user's basic information and shows their request history and feedback.
   - **Key File**: [`UserService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java), [`RequestService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/RequestService.java)
   - **Related To**: `Request` , `Notification`

- **Notification**
   - **Role**: Manages notifications related to user activities, informing users of status changes in their requests. It also provides interactive buttons to manage requests.
   - **Key File**: [`NotificationService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/NotificationService.java)
   - **Related To**: `Message`, `Request`, `Profile`

- **Message**
   - **Role**: Facilitates communication between users.
   - **Key File**: [`MessageService`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/MessageService.java), [`MessageRepository`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/MessageRepository.java)
   - **Related To**: `Request`, `Notification`, `Profile`

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
   There are no additional dependencies or databases required to run the project locally. Once the changes are pushed to the GitHub repository, they will automatically trigger the deployment process.

## Roadmap

- **Multi-Volunteers**
   Implement a feature that allows multiple volunteers to be assigned to a single request. Volunteers can be able to collaborate on tasks, track their progress, and interact with each other and the requester seamlessly. This would enable users to get assistance from multiple volunteers for complex or time-sensitive requests, improving the overall efficiency of the platform and providing users with more support options.

- **Group Chat**
   Introduce a group chat feature where multiple users (requesters and volunteers) can communicate in real-time. The chat can allow seamless communication between all parties involved in a request.

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