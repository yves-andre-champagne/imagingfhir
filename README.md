# A HAPI-FHIR ImagingStudy Server Facade For A DicomWeb Server
 
 This project allow accessing the metadata content of a DicomWeb server as FHIR ImagingStudy.
 
 Currently the server only supports querying by PatientID. 
 
## Prerequisites
 JDK17 or later
 
 Apache Maven build tool (newest version)
  
## To Build
 mvn clean build
 
## To Run
 - Facing a DicomWeb server that supports HTTP basic authorization
    -  `./mvnw  spring-boot:run  -Dspring-boot.run.jvmArguments="-DQIDORS_BASE_URL=<dicomweb url>  -DBASIC_AUTH_PASSWORD=<password>    -DBASIC_AUTH_USERNAME=<user>"`
    
    
  - Facing a DicomWeb server that supports token basic authorization
    -  `./mvnw  spring-boot:run  -Dspring-boot.run.jvmArguments="-DQIDORS_BASE_URL=<dicomweb-url>  -DOIDC_CLIENT_ID=<clientid>   -DOIDC_CLIENT_SECRET=<secret> -DOIDC_TOKEN_ENDPOINT=<token-end-point> -DSPRING_PROFILES=oidctokenauth"`  
 

## To Access
`curl http://localhost:8080/fhir/r4/ImagingStudy?PatientID=<the-id>&_format=application/json`   