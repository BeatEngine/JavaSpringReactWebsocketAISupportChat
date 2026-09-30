FROM node:jod-alpine AS frontend
WORKDIR /
COPY AIChatSupportToolingDemo AIChatSupportToolingDemo
COPY environmentconfigurations.env .
WORKDIR /AIChatSupportToolingDemo/src/main/react
#Build Frontend
RUN ["sh", "build.sh"]
WORKDIR /
FROM gradle:jdk26-corretto AS backend
WORKDIR /
COPY --from=frontend /AIChatSupportToolingDemo /AIChatSupportToolingDemo
COPY --from=frontend environmentconfigurations.env .
WORKDIR /AIChatSupportToolingDemo
#Build Backend
RUN ["chmod", "+x", "./gradlew"]
RUN ["./gradlew", "clean" ,"build", "--exclude-task", "test"]
#Start
ENTRYPOINT ["./gradlew", "bootRun", "--exclude-task", "test"]
