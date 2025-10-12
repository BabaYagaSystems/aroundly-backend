1. Create two different versions for migration. One prod, one dev.
   2. finish the changes creation.
2. See if adding jenkins is a good idea
3. See if adding a healthcheck is a good idea
4. Write commends where is needed
5. Remove "var" variable types form WebConfig.java
6. Add id to Happening domain object
7. Change the implementation of findAllInGivenRange() from java to SQL
8. Refactor create method
9. See how to auto generate the location id in LocationService
   1. Look if it's necessary to change the domain, cause probably yes
10. Update the schema in incidentRequestDto
11. Change Media to Multipart File in IncidentDetailedResponseDto
12. See if it's necessary to change MediaDto to Multipart File in IncidentPreviewResponseDto
    1. In this case check if Media domain object it's in general required.
13. Media mapper is not used, the idea above probably is correct
14. Remove AdminController 
15. Update Incident and Feed controllers swaggers
16. See how to get rid of MediaPreviewFactory
17. Implement required mappers in adapter/Outbound
18. Rename the required repos. They are still named with "fake"
19. Check persistence in adapter/outbound/repo
20. WRITE THE TESTS
21. See how to create fixtures!!!
