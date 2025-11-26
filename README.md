# Docker Notes

## Topics:

1. Introduction to Docker
2. Docker Architecture
3. Installing Docker
4. Docker File
5. Docker Images
6. Docker Container
7. Pulling and running the docker image
8. Dockerfile for Springboot Application
9. Docker Networking
10. Docker Volumes and Storage
11. Docker Compose
12. Docker Registry
13. Multi-stage Docker Builds
14. Spring-mysql-app with Mysql todo app on a custom bridge network(islam-network)
15. Docker Scout and Docker Init

# Notes

1. What is Docker?
   
   Docker is a platform that allows developers to build, package, and run applications in containers. Containers are lightweight, portable, and self-sufficient units that contain everything needed    to run a piece of software — including the code, runtime, libraries, and dependencies — ensuring consistency across different environments.
   
2. Docker Architecture:
   
   Docker Engine : Docker Engine is main component of Docker. It’s the runtime that builds and runs containers. It is a container engine that manages everything behind the scenes. Docker engine       contains Docker Daemon (dockerd)m, Docker CLI, Docker Client (docker).
   
   Docker Daemon : The Docker Daemon (dockerd) is the core background service of Docker. It is the component that actually builds images, runs containers, and manages Docker objects.
   
   Docker CLI : The Docker CLI (Command Line Interface) is the user-facing tool that you use to interact with Docker.
   
   Docker Client : The Docker Client is a user-facing UI application that allows user to talk to Docker Engine.
   
   Docker API : The Docker API is the RESTful interface that allows programs to communicate with the Docker Daemon (dockerd). It is the core communication layer of Docker Engine.

3. Installing docker and docker compose
   ```
   # MacOS Steps
   brew install docker docker-compose

   # Linux Step
   sudo yum update -y
   sudo yum install -y docker
   sudo systemctl start docker
   sudo systemctl enable docker
   sudo usermod -aG docker $USER
   newgrp docker
  
    #verify
   docker --version
   docker compose version

   ```
4. Dockerfile: A Dockerfile is a text file that contains a set of instructions to build a Docker image.
5. Docker Image: A Docker image is a read-only template used to create Docker containers. It contains everything a container needs to run an application: code, runtime, libraries, environment         variables, and configuration files.
6. Docker Container : A Docker container is a lightweight, standalone, and executable package that runs an application. It is created from a Docker image, and it contains everything the 			    application needs to run: code, runtime, system tools, libraries, and settings.

7. Pulling and running the docker image
   ```
    docker login										# Command to login to docker account
    docker run -e MYSQL_ROOT_PASSWORD=root -d mysql
   ``` 
8. Write a Springboot app and use docker file ro create a docker image and run the container.
   
   A. Create a simple Sprinboot app with just one endpoint /greeting (use the project simple-spring-boot-app)
   
   B. build the project using mvn clean package
   
   C. Create a dockerfile in the project with below content
   
   ```
   FROM openjdk:17-ea-17-jdk-slim
   WORKDIR /islam
   COPY target/spring-app.jar app.jar
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

   Below dockerfile will build the code and create the image
   ```
    # ------------ Stage 1: Build the app ------------
    FROM maven:3.8.3-openjdk-17 AS builder
    WORKDIR /app
    COPY . /app
    RUN mvn clean install 

    # ------------ Stage 2: Run the app ------------
    FROM openjdk:17-ea-17-jdk-slim
    WORKDIR /app
    COPY --from=builder /app/target/*.jar /app/target/app.jar
    CMD ["java", "-jar", "/app/target/app.jar"]
   ```
   
   C. Build the image and run the container using the below command
   
   ```
   docker build -t spring-app .
   docker run -p 8080:8080 spring-app
   ```
   D. Connect to the interactive Bash shell inside a running container using the below command
   
   ```
   docker exec -it <container_id> bash
   docker logs <container_id>
   ```
9. Docker Networking

   Different type of Docker networks: Bridge, Custom Bridge, Host, None, Overlay, MacVlan, IPVlan

   Bridge: This is the default network that Docker uses when you run containers without specifying a network. A bridge between docker network and host network that gets created. Containers get an     internal IP range. port mapping is required.

   Host: The container uses the host’s network directly, no port mapping required. The container listens on the host’s real ports.

   Custom Bridge: Helps in creating better isolation, containers only see other containers in the same network. Containers on the same custom bridge automatically resolve each other by name.

   Command to create a custom bridge
   ```
   docker network create <network_name> 				# Custom Bridge Network
   ```
   None: The none network is a special built-in network mode that disables all networking for a container.

   Overlay : A Docker overlay network is a multi-host virtual network that connects containers running on different Docker hosts (machines). It’s commonly used with Docker Swarm.

   MacVlan: Macvlan assigns each container its own unique MAC address, so the network sees each container as a separate physical machine.

   IPvlan : IPvlan networks create multiple IP addresses but do NOT create unique MAC addresses for each container.

   Docker does not allow you to create additional none or host networks, only custom bridge is allowed.

10. Docker Volumes and Storage

    This is a way to persist the data even when the container is restarted or destroyed.

    Step 1. Create the volume by using the below command
    ```
    docker volume create mysql-data
    ```

    Step 2. Run the container with and without volume
    ```
    # Without volume
    docker run -d --name sql-db --network islam-network -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql

    # With volume
    docker run -d --name sql-db --network islam-network -v mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
    ```
    Step 3. Inspect the volume
    ```
    docker volume inspect mysql-data 
    ```

    Another way

    Step 1. Create a folder with /Users/islam/Documents/Docker/spring-mysql/mysql-data

    Step 2. Run the container with the local volume
    ```
    docker run -d --name sql-db --network islam-network -v /Users/islam/Documents/Docker/spring-mysql/mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
    ```
11. Docker Compose

    Docker Compose is a tool for defining and running multi-container Docker applications using a single configuration file (usually docker-compose.yml).

    Below is the docker file for the project spring-mysql with non local volume
    ```
    services:
     sqldb:
       container_name: sql-db
       image: mysql
       environment:
         MYSQL_DATABASE: "taskdb"
         MYSQL_ROOT_PASSWORD: "root"
       volumes:
         - mysql-data:/var/lib/mysql
       networks:
         - my-network
   
     spring-mysql-app:
       build: .
       image: spring-mysql-app
       container_name: spring-mysql-app
       depends_on:
         - sqldb
       ports:
         - "8080:8080"
       networks:
         - my-network
   
      volumes:
        mysql-data:
          name: mysql-data
      
      networks:
        my-network:
          name: islam-network

    ```

    Below is the docker file for the project spring-mysql with local volume
    ```
    services:
     sqldb:
       container_name: sql-db
       image: mysql
       environment:
         MYSQL_DATABASE: "taskdb"
         MYSQL_ROOT_PASSWORD: "root"
       volumes:
         - /Users/islam/Documents/Docker/spring-mysql/mysql-data:/var/lib/mysql
       networks:
         - islam-network
   
     spring-mysql-app:
       build: .
       image: spring-mysql-app
       container_name: spring-mysql-app
       depends_on:
         - sqldb
       ports:
         - "8080:8080"
       networks:
         - islam-network
   
      networks:
        islam-network:
          name: islam-network

    ```

    Docker COmpose Commands
    ```
    docker compose up
    docker compose up -d
    docker compose down
    docker compose down --rmi all
    ```
    
    In docker compose we can use Healthcheck to monitor if the service is completely up and ready.

12. Docker Registry

    A Docker registry is an application used to store and distribute Docker images. Like Docker Hub or AWS ECR

    Command to push the images to docker hub
    ```
    docker image tag <image_name> islam123786/<image_name>
    docker push islam123786/<image_name>
    ```
13. Multi-stage Docker Builds

      Without multistage build
    
      ```
      FROM openjdk:17-ea-17-jdk-slim
      WORKDIR /islam
      COPY target/spring-app.jar app.jar
      ENTRYPOINT ["java", "-jar", "app.jar"]
      ```
      
      With multistage build
      ```
       # ------------ Stage 1: Build the app ------------
       FROM maven:3.8.3-openjdk-17 AS builder
       WORKDIR /app
       COPY . /app
       RUN mvn clean install 
   
       # ------------ Stage 2: Run the app ------------
       FROM openjdk:17-ea-17-jdk-slim
       WORKDIR /app
       COPY --from=builder /app/target/*.jar /app/target/app.jar
       CMD ["java", "-jar", "/app/target/app.jar"]
      ```

14. Steps to run the project(spring-myapp) by docker CLI

    Commands
    ```
    docker build -t spring-mysql .
    docker network create islam-network
    docker volume create mysql-data
    docker run -d --name sql-db --network islam-network -v mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
    docker run -d -p 8080:8080--network islam-network  spring-mysql

    OR

    docker build -t spring-mysql .
    docker network create islam-network
    # Create a folder with /Users/islam/Documents/Docker/spring-mysql/mysql-data
    docker run -d --name sql-db --network islam-network -v /Users/islam/Documents/Docker/spring-mysql/mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
    docker run -d -p 8080:8080--network islam-network  spring-mysql
    ```

15. Docker Scout is a tool that analyzes your Docker images for Vulnerabilities (CVEs) and Outdated dependencies. Alternate of Trivy

    Command
    ```
    docker scout quickview spring-mysql-app:latest
    ```
    
    Docker init analyzes your project and automatically generates : Dockerfile, docker-compose.yml, .dockerignore




### Docker Commands
```
docker login										# Command to login to docker account
docker pull <image_name>:<tag>  					# Pull image from Docker Hub
docker run <image_name>								# run the image
docker rmi <image_id>								# delete image using the id
docker kill <container_name>
docker stop <container_id>
docker start <container_id>
docker run -e MYSQL_ROOT_PASSWORD=root -d mysql

mvn clean package
docker build -t <image_name> . 						# build image from docker file
docker build --platform linux/arm64 -t <image_name>:<image_tag> .
docker run -p host_port:docker_port <image_name> 	# run the container from docker image
docker logs <container_id>
docker exec -it <container_id> bash
docker attach <container_id>

docker network ls
docker network inspect islam-network
docker network rm islam-network
docker network create <network_name> 				# Custom Bridge Network

docker run -d --name <name> <image_name>
docker exec -it ecb9f1ae7e43 mysql -u root -p

docker volume create <volume_name>
docker volume inspect <volume_name>
docker run -d --name sql-db --network islam-network -v <volume_name>:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
docker run -d --name sql-db --network islam-network -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
docker run -d -p 8080:8080 --name springboot-box --network islam-network  springbootapp
docker run -d --name sql-db --network islam-network -v mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
docker compose up
docker compose down
docker rm -f $(docker ps -aq)
docker rmi -f $(docker images -aq)
docker image prune -a -f  

docker image tag <image_name> islam123786/<image_name>
docker push islam123786/<image_name>

Installing docker
Write docker file
Create docker image using springboot app and run the container
Deploy two tier(springboot service and mysql) application in a custom network
create volume to persist data even after container is stopped
write docker compose for the above two tier application

Two different docker files:

FROM maven:3.8.3-openjdk-17 AS builder
WORKDIR /app
COPY . /app
RUN mvn clean install -DskipTests=true

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/target/app.jar
ENTRYPOINT ["java", "-jar", "/app/target/app.jar"]

#--------------------------------------------------------

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/spring-mysql.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


ECR Artifactory:
aws ecr create-repository --repository-name islam-springboot --region ap-south-1
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 591093779046.dkr.ecr.ap-south-1.amazonaws.com
docker tag springboot-ecr 591093779046.dkr.ecr.ap-south-1.amazonaws.com/islam-springboot
docker push 591093779046.dkr.ecr.ap-south-1.amazonaws.com/islam-springboot
```
