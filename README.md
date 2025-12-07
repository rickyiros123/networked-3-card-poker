# Networked 3-Card Poker

A client-server 3-card poker game built in **Java** with **JavaFX** for the frontend, **Java Sockets** for networking, and **Maven** for project management. Supports multiple concurrent players and implements full game logic, including hand evaluation, betting, dealer qualification, and payouts.  

---

## Features

- **Multithreaded server** supports up to 8 clients concurrently  
- **Client-server architecture** using Java Sockets  
- **JavaFX client** built with FXML and CSS for a clean UI  
- **Full game logic**: hand evaluation, betting, dealer qualification, payouts  
- **Maven project structure** for easy builds and dependency management  
- **JUnit 5 tests** for server-side logic  
- **UI wireframes and assets** prototyped in Figma  

---

## Tech Stack

- **Language:** Java 17+  
- **Frontend:** JavaFX, FXML, CSS  
- **Backend:** Java Sockets, multithreaded server  
- **Build Tool:** Maven  
- **Testing:** JUnit 5  
- **Design Tools:** Figma  

---

## Getting Started

### Prerequisites

- Java 17 or higher  
- Maven 3.8+  
- IDE such as Eclipse or IntelliJ (optional, can use terminal)  

### Running the Project

1. **Clone the repository**  
```bash
git clone https://github.com/<your-username>/networked-3-card-poker.git
cd networked-3-card-poker

2. **Build with Maven**
'''bash
mvn clean install

3. **Run the server**
'''bash
mvn exec:java

4. **Run the client(s)**
'''bash
mvn exec:java

5. **Connect multiple clients to the server and start playing. 

