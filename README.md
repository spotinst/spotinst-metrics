# Spotinst Java Service Template 

Template project for creating a new Spotinst Java service.

![spotinst-logo](src/main/resources/spotinst-logo.png)
![java-icon](src/main/resources/java-icon.png)

## What is it?
This project intended to be used when in need to create a new Java service.<br/>
It encapsulate the following Spotinst internal libraries:
* [`spotinst-commons`](https://github.com/spotinst/spotinst-commons) - Spotinst common utilities 
* [`spotinst-dropwizard`](https://github.com/spotinst/spotinst-dropwizard) - [Dropwizard](https://www.dropwizard.io) bootstrap framework
* [`spotinst-messaging`](https://github.com/spotinst/spotinst-messaging) - Spotinst SQS messaging 

## Requirements
- Java 21

## Getting Started

1. Git clone
2. Rename package to `com.spotinst.<service-name>`
3. Rename all `SampleXXX` classes to proper service name
4. Remove irrelevant content

## Renaming Service Name

1. In `settings.gradle` change the `rootProject.name` name 
2. Rename IntelliJ module to proper service name
3. Git push
* In `src/main/resources/banner.txt` change the content according to your service-name, using [`Text to ASCII Art Generator`](https://patorjk.com/software/taag/#p=display&f=Big%20Money-sw&t=Type%20Something%20)

## Notes
The template contains optional content such as customer facing service filters, rate limit filters/tasks etc..<br/>
When not in need, these classes should get removed from the project.

## Configuration
Service configuration YAML files contains coverage of all possible components exists on the java-service-template.<br/>
Make sure to remove unnecessary configuration and un-comment the ones that in need for the new service.<br/>
<br/>What's included in the service configuration?
- Credentials/Tokens
- Services URLs
- HTTP Client
- Rate Limiter
- Feature Flags
- Dropwizard Web Server
- Messaging Producer/Consumer
- Logging

## Repositories
Repositories should be created under package `com.spotinst.<service>.bl.repos` and registered to `xxxRepoManager`.<br/>
Every repository class name should comply to the `IxxxRepo` format and it's property name should be the name of the entity it represents:
```java
public class SampleRepoManager extends BaseRepoManager {
    
    public static IOrganizationRepo Organization;
    ...
}
```
There are two alternatives when creating a new repository:
1. Create a standard repository with full implementation  
2. Optional: `GenericRepo` for basic CRUD based entities
 
## Converters
Model conversion can be used by two different approaches:
- Orika mapper is available on the template project for mapping Java models with partial update support<br/>
- Manual conversion methods<br/>
<br/>
Make sure to follow coding guidelines by encapsulating entity mappings behaviour API <--> BL <--> DAL inside a single converter class.


