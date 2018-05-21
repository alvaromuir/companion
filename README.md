Companion is an apriori based market basket analysis tool to deliver realtime results for recommendation engines
---

Last update: 05.20.2018

#### Overview ####
Exploratory analysis was composed in Python 3.x

Data models built in [Python 3.4.x](https://www.python.org/) leveraging the [apriori alogrithm]([https://en.wikipedia.org/wiki/Apriori_algorithm).
Further exploration available in the [notebooks](https://onestash.verizon.com/users/v603497/repos/companion/browse/notebooks) 
path with dataset samples located in the [data](https://onestash.verizon.com/users/v603497/repos/companion/browse/data) directory.

Microservice developed for reactive scalability with the following components:

- [Scala 2.11.8](https://www.scala-lang.org/) - Core development language
- [Akka-Http](https://doc.akka.io/docs/akka-http/current/introduction.html) - Http stack for REST interface
- [Akka-Alpakka](https://developer.lightbend.com/docs/alpakka/current/) - Enterprise data integration middleware
- [Typesave Slick](http://slick.lightbend.com/)  - Database abstraction layer
- [H2](http://www.h2database.com/html/main.html) - Presistant in-mem storage



#### Building the microservice ####

To build, using SBT v0.13 or higher, navigate to the `microservices` directory and run:  
`$ sbt clean assembly`  
or  
`$ sbt 'set test in assembly := {}' clean assembly`  
to skip tests.

The compiled jar can be run from targets/scala-2.11/companion-v{version num}.jar

---

#### running the application #####
A config file, [application.conf](https://onestash.verizon.com/users/v603497/repos/companion/browse/microservice/src/main/resources/application.conf) 
(example found in `microservice/src/main/resources/application.conf`) can be created and adjusted for your environment.

By placing a csv file in the same format as the [test file](https://onestash.verizon.com/users/v603497/repos/companion/browse/microservice/src/test/resources) 
located in `microservice/src/test/resources/test.csv` in a path designated in your config file, the server can be started with a java command:  
`$ java -Dconfig.file=<path/to/your/>.conf -jar companion-v{version num}.jar`  
The system will initialize an in-memory data-store with all records of the data file.


To test, send POST request to the /api/{version}/recommend endpoint in the following format:  
`{"browsedSku": "<sku number>", "cartSkus": ["<sku number>", "<sku number>", ...], "compatibleDevice": "<sku number>"}`  
At the time of this writing, the API is in version 1 (v1)

Via curl, an example may look like :  
`$curl -X POST -H "Content-Type: application/json" -d '{"browsedSku": "MP822LL/A", "cartSkus": ["WTLRBSILBK"], "compatibleDevice": "MP822LL/A"}' http://localhost:8080/api/v1/recommend`

---

#### Initial Development ####
Model Development: [Supriya P Gharpure](supriya.gharpure@verizonwireless.com), Verizon IT Analytics Data Science  
App Development: [Alvaro Muir](alvaro.muir@one.verizon.com), Verizon IT Analytics Data Engineering 
