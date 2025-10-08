1. See if the user of ObjectStorageUseCase is needed. 
   1. If yes, create commands for arguments.
2. Create two different versions for migration. One prod, one dev.
3. See if adding jenkins is a good idea
4. See if adding a healthcheck is a good idea
5. Write commends where is needed
6. Remove "var" variable types form WebConfig.java
7. Add id to Happening domain object
8. Change the implementation of findAllInGivenRange() from java to SQL
9. Refactor create method
10. See how to auto generate the location id in LocationService
    1. Look if it's necessary to change the domain, cause probably yes
11. Update the schema in incidentRequestDto
12. Change Media to Multipart File in IncidentDetailedResponseDto
13. See if it's necessary to change MediaDto to Multipart File in IncidentPreviewResponseDto
    1. In this case check if Media domain object it's in general required.
14. Media mapper is not used, the idea above probably is correct
15. Remove AdminController 
16. Update Incident and Feed controllers swaggers
17. See how to get rid of MediaPreviewFactory
18. Implement required mappers in adapter/Outbound
19. Rename the required repos. They are still named with "fake"
20. Check persistence in adapter/outbound/repo
21. WRITE THE TESTS
22. See how to create fixtures!!!
