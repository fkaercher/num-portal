# Num-portal

## Running using Docker

There are two options to run the portal using Docker:
  1. Running a demo including all componentes including an empty EHRbase
     - Run: `docker-compose -f compose-demo.yaml up`

  3. Running the portal with an existing EHRbase
     - Adappt the `.env.sample` file and save it as `.env`
     - Run: `docker-compose -f compose-demo-without-ehrbase.yaml up`
      
  - Go to http://localhost:4200 and login with user superadmin, password super
  - Go to user_details table in the numportal database and set superadmin user approved = true
  - reload the portal in your browser


## Building and running locally

1. Postgres should be up and running, instructions below

In the root folder of the project, open cmd and run:

1. Build app: `mvn clean install`
2. Run: `spring_profiles_active=local mvn spring-boot:run`

## Database 

Start a local instance of PostgreSQL: 

```
docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 postgres
```

## Swagger

http://localhost:8090/swagger-ui/index.html


## License

Copyright 2024 HiGHmed e.V.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
