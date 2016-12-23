# Running the Spark RDF Analyzer
In this tutorial we will show how you can run the Spark RDF Analyzer right from eclipse using a Docker container for the Tomcat webservice.

## Setup Prerequisites
- Download Docker from [https://www.docker.com/products/docker](https://www.docker.com/products/docker) and follow their installation instructions.
- Get an official Tomcat image (we will use Tomcat 8.0 with Java 8):

```Dockerfile
docker pull tomcat:8.0-jre8
```

- Create a file <b>/my/path/to/tomcat-users.xml</b>, which will be injected into the Docker container to allow us to access the Tomcat manager and executing scripts using the text API of Tomcat.

```XML
<?xml version="1.0" encoding="UTF-8"?>
<tomcat-users xmlns="http://tomcat.apache.org/xml"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
              version="1.0">
	<user username="manager" password="manager" roles="manager-gui" />
	<user username="demo" password="demo" roles="manager-script" />
</tomcat-users>
```

- Create one folder <b>/my/path/to/data</b> where you put in all the datasets that should be available to the running RDF Analyzer.
- By executing the following command, you can run the Tomcat container. 

```bash
docker run \
	-dit \
	--name tomcat \
	-p 8080:8080 \
	-v /my/path/to/tomcat-users.xml:/usr/local/tomcat/conf/tomcat-users.xml \
	-v /my/path/to/data:/home/data \
	tomcat:jre8 \
&& docker logs -f tomcat
```

- You will have to setup a local maven profile that will be used to overwrite the maven variables during the deployment process.
This way we can ensure that real credentials are hidden from public and excluded from the POM.
    - Open your local settings file, located at <b>${user.home}/.m2/settings.xml</b>.<br>
    If the file is not already there, simply create it!
    - Insert the following lines:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<settings>
        <servers>
                <server>
                        <id>tomcat-localhost</id>
                        <username>demo</username>
                        <password>demo</password>
                </server>
        </servers>

        <profiles>
                <profile>
                        <id>tomcat-localhost</id>
                        <properties>
                                <tomcat.deploy.server>tomcat-localhost</tomcat.deploy.server>
                                <tomcat.deploy.url>http://127.0.0.1:8080/manager/text</tomcat.deploy.url>
                        </properties>
                </profile>
        </profiles>
</settings>
```


## Run the Spark RDF Analyzer
- Check out the project and import it into eclipse as a maven project.
- Set up a new run configuration for it. 
    - Right click on the project > "Run as" > "Maven build..."
    - Set following properties:
        - Name: RDF Analyzer (Tomcat)
        - Goals: clean tomcat7:redeploy
        - Profiles: tomcat-localhost
        - Parameter: p.type=war
- Click "Run" and check the console output in eclipse. The war file gets deployed to the running Tomcat instance.
- Go back to the console, where Tomcat logs to and wait for the completion of deploament.
- Open your web browser at [http://127.0.0.1:8080/spark-rdfanalyzer2/](http://127.0.0.1:8080/spark-rdfanalyzer2/) and you should see the RDF Analyzer running.
Unless you changed the mount path, your datasets will be available under <b>/home/data</b> inside the container.
