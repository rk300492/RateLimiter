
#### INTRO:

This is the code for my version of the rate limiting module. The basic idea of this project is to be able to plug in this module on top of any existing HTTP servers. So , it basically acts as micro service, whose main task is to do rate limitation.
*******
#### HOW TO RUN THE CODE:

* You can only run one server but you can spawn multiple clients on this program as it is asynchronous and multi threaded.
* Start the app using following VM Args: 

##### *Server:*

~~~
-Xms2G
-Xmx2G
-Drl.mode=SERVER
-Drl.port=8010
-Drl.host=localhost
-Drl.throttleLimit=100
-Drl.minutesToThrottle=60
~~~
##### *Client:*
~~~
-Xms2G
-Xmx2G
-Drl.mode=CLIENT
-Drl.port=8010
-Drl.host=localhost
~~~

*******
#### STRUCTURE OF CODE: 

The module is constructed in a pretty self explanatory manner. The "Main.java" class is where we can spawn a server of client based on the mode we give in our VM args. From there on, the control goes to either the Server.java or Client.java and all its corresponding activites take place. The rate limiting module logic is entirely in the Throttler.java and other utilities and constants are placed in a separate package.

*******
#### ARCHITECTURE DIAGRAM:
![GitHub Logo](/architecture.png)

*******
#### FEATURES & IMPROVEMENTS:

* The server is asynchronous and multi threaded so scalability wise, it cant handle more requests. 
* The throttling mechanism is modular and easy to configure . Also it can be made more complex depending on the scenario.
* This code can be extended to actually receive a valid http request and contact the http service to get back the webpage. Meaning, this server can actually be extended to service the webpage directly instead of console outputs. 
* Some factors such as throttle factor can reside in a config file instead of being in a VM argument. 
* Certainly, a better exception handling system can be designed here. 
* Pings to http service can also be made asynchronous making the server to handle other requests instead of waiting on the pings.

*******
#### EXAMPLE RUN:

Below shows a trial run of the application: 

##### *Server side:*

~~~
Feb. 27, 2020 7:20:49 PM server.Server start
INFO: Beginning the rate limiting module server ...

Feb. 27, 2020 7:21:02 PM server.Server$ServerSocketTask run
INFO: A new client with id:f10785dd-6511-4fa2-91b4-43e3a949d0ac has started ! Listening to messages ... 

Feb. 27, 2020 7:21:38 PM server.Server$ServerSocketTask$ServerReadHandler completed
INFO: Received request from client:ClientID=f10785dd-6511-4fa2-91b4-43e3a949d0ac,RequestID=1,RequestType=GET,Repetition=15

Feb. 27, 2020 7:21:38 PM server.Server$ServerSocketTask processRequest
INFO: Clientf10785dd-6511-4fa2-91b4-43e3a949d0ac  has requested to ping the airtasker webpage :15 times
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200
https://airtasker.com		Status:-> Green <-	Code: 200

Feb. 27, 2020 7:22:24 PM server.Server$ServerSocketTask$ServerReadHandler completed
INFO: Received request from client:ClientID=f10785dd-6511-4fa2-91b4-43e3a949d0ac,RequestID=2,RequestType=GET,Repetition=3

Feb. 27, 2020 7:22:24 PM server.Server$ServerSocketTask processRequest
INFO: Clientf10785dd-6511-4fa2-91b4-43e3a949d0ac  has requested to ping the airtasker webpage :3 times

Feb. 27, 2020 7:22:32 PM server.Server$ServerSocketTask$ServerReadHandler completed
INFO: Received request from client:ClientID=f10785dd-6511-4fa2-91b4-43e3a949d0ac,RequestID=3,RequestType=CLOSE,Repetition=0

Feb. 27, 2020 7:22:32 PM server.Server$ServerSocketTask processRequest
INFO: Client:f10785dd-6511-4fa2-91b4-43e3a949d0ac has requested to close this connection with id:f10785dd-6511-4fa2-91b4-43e3a949d0ac
~~~

##### *Client Side:*

~~~
Feb. 27, 2020 7:21:02 PM client.Client start
INFO: Client has begun processing ...
Feb. 27, 2020 7:21:02 PM client.Client start
INFO: Client connection has been established!. ID:f10785dd-6511-4fa2-91b4-43e3a949d0ac

How many times do you want to ping the http server? (Enter a number)
15
Ping Status (status code : no.of occurences) : {200=10, 429=5} ; Message : Too many request has been received from this client, please try your requests after 2020-02-27, 19:26:38 pm

Do you wish to continue? (Y/N)
y

How many times do you want to ping the http server? (Enter a number)
3
Ping Status (status code : no.of occurences) : {429=3} ; Message : Too many request has been received from this client, please try your requests after 2020-02-27, 19:26:38 pm

Do you wish to continue? (Y/N)
n

Feb. 27, 2020 7:22:32 PM client.Client start
INFO: Client is exiting... BYE !

Process finished with exit code 0

~~~
*******

#### REFERENCES:
*Some referred Rate Limiter examples:*

* [Rate Limiter Example in Java from Medium](https://medium.com/@aayushbhatnagar_10462/rate-limiting-implementation-example-in-java-7831923e5de3)
* [How to ping a URL](https://crunchify.com/how-to-get-ping-status-of-any-http-end-point-in-java/)
* [Multi Threading the server](https://www.baeldung.com/java-executor-service-tutorial)
* [Multi Threading example](https://dzone.com/articles/java-concurrency-multi-threading-with-executorserv)
* [Asyn Socket Server Design](https://www.baeldung.com/java-nio2-async-socket-channel#the-server-with-completionhandler)

*******
