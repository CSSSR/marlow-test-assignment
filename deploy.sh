./atm-service/gradlew clean jibDockerBuild -p ./atm-service &&
  ./audit-service/gradlew clean jibDockerBuild -p ./audit-service &&
  docker compose up --build --force-recreate
