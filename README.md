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
7. Docker Volumes and Storage
9. Docker Compose
10. Docker Registry
11. Multi-stage Docker Builds
12. Orchestrating Docker with Kubernetes (Introduction)
13. Domain name mapping
14. Docker Scout and Docker Init

# Notes

1. What is Docker?
   
   Docker is a platform that allows developers to build, package, and run applications in containers. Containers are lightweight, portable, and self-sufficient units that contain everything needed    to run a piece of software — including the code, runtime, libraries, and dependencies — ensuring consistency across different environments.
   
2. Docker Architecture:
   
   Docker Engine : Docker Engine is main component of Docker. It’s the runtime that builds and runs containers. It is a container engine that manages everything behind the scenes. Docker engine       contains Docker Daemon (dockerd)m, Docker CLI, Docker Client (docker).
   
   Docker Daemon : The Docker Daemon (dockerd) is the core background service of Docker. It is the component that actually builds images, runs containers, and manages Docker objects.
   
   Docker CLI : The Docker CLI (Command Line Interface) is the user-facing tool that you use to interact with Docker.
   
   Docker Client : The Docker Client is a user-facing UI application that allows user to talk to Docker Engine.
   
   Docker API : The Docker API is the RESTful interface that allows programs to communicate with the Docker Daemon (dockerd). It is the core communication layer of Docker Engine.

4. Installing docker and docker compose
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
5. Dockerfile: A Dockerfile is a text file that contains a set of instructions to build a Docker image.
6. Docker Image: A Docker image is a read-only template used to create Docker containers. It contains everything a container needs to run an application: code, runtime, libraries, environment         variables, and configuration files.
7. Docker Container : A Docker container is a lightweight, standalone, and executable package that runs an application. It is created from a Docker image, and it contains everything the 			    application needs to run: code, runtime, system tools, libraries, and settings.

8. Pulling and running the docker image
   ```
    docker login										# Command to login to docker account
    docker run -e MYSQL_ROOT_PASSWORD=root -d mysql
   ``` 
9. Write a Springboot app and use docker file ro create a docker image and run the container.
   
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
    FROM maven:3.8.3-openjdk-17 AS builder
    WORKDIR /app
    COPY . /app
    RUN mvn clean install 
    
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
docker run -d --name <name> <image_name>
docker inspect network
docker network create <network_name> 				# Custom Bridge Network
docker exec -it ecb9f1ae7e43 mysql -u root -p
docker volume create <volume_name>
docker volume inspect <volume_name>
docker run -d --name sql-db --network islam-network -v <volume_name>:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
docker run -d --name sql-db --network islam-network -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
docker run -d -p 8080:8080 --name springboot-box --network islam-network  springbootapp
docker run -d --name sql-db --network islam-network -v mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=taskdb mysql
docker compose up
docker compose down
docker rm $(docker ps -aq)
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
