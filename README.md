Simple applications for reproducing of bug with logging of AccessLog.   
Please follow the steps to reproduce:
1. Run org.example.SimpleTomcatApplication#main   
This is simple Tomcat application. The port is 8081.
2. Run org.example.GatewayApplication#main with system property -Dreactor.netty.http.server.accessLogEnabled=true        
This is cloud gateway application based on Reactor Netty. The port is 8080.
As simple gateway it just redirects client requests to the first service (from step 1)
3. Run org.example.PipelinedHTTPRequestSender#main  
It will send two requests /test-api in one HTTP1 pipelined request to gateway service (from step 2).
4. Check access logs in console of gateway service. The format is incorrect  
127.0.0.1 - - [27/dec./2022:15:17:36 +0200] "GET /test-api HTTP/1.1" 200 40 127   
127.0.0.1 - - [null] "null null null" 200 40 1672143456978
5. Change the version of spring-cloud-dependencies in the module 'second-cloud-gateway-with-netty' to Hoxton.SR10   
   Rerun org.example.GatewayApplication
6. Repeat step 3. Check access logs in console of gateway service. The format is correct after version downgrade



