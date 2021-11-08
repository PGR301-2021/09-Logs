# Logz.io

I denne labben skal vi se hvor enkelt det er å sende logger til en SAAS tjeneste som heter logz.io fra Spring Boot applikasjonen. Vi skal også se på 12 factor prinsippet "Config" ved å ikke hardkode token og endepunlt for Logz.io i property filer. 

I denne øvelsen kan det være lurt å bruke en enkel Spring Boot applikasjon som kan deployes til Heroku. 

## Registrer deg på logz.io

https://app.logz.io - Det er meldt om problemer med å gjør sign up med Chrome og enkelt ad- blokkere. Bruk annen nettleser hvis dere ikke kommer dere forbi side en i registreringsskjemaet. 

## Ship logs 

* Se på "Ship logs" fanen i Logz.io, og let deg frem til "Libraries" og "Java Logback appender". Kopier logback.xml inn i en applikasjon du 
har gjort fra tidligere Lab (src/main/resources)
* Se på kodeeksemplet og lag punker i applikasjonen din som logger på ulike lognivåer 

Start applikasjonen lokalt - og du vil se at det kommer mindre logger i konsollet. Se på "Live view" i logz.io - loggene fra applikasjonen slal nå havne der.

## Pass på å logge både til standard out og Logz.io

Modifiser Logback.xml slik at du også kan se loggene uten å gå til logz.io

```xml
<!-- Use debug=true here if you want to see output from the appender itself -->
<configuration>
    <!-- Closes gracefully and finishes the log drain -->
    <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d %-5level [%thread] %logger{0}: %msg%n</pattern>
            <outputPatternAsHeader>true</outputPatternAsHeader>
        </encoder>
    </appender>

    <appender name="LogzioLogbackAppender" class="io.logz.logback.LogzioLogbackAppender">
        <token>${LOGZ_TOKEN}</token>
        <logzioUrl>https://listener-eu.logz.io:8071</logzioUrl>
        <logzioType>bankapp_weblogs</logzioType>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
        <inMemoryQueue>true</inMemoryQueue>
        <inMemoryQueueCapacityBytes>-1</inMemoryQueueCapacityBytes>
    </appender>

    <logger name="org.springframework.web.filter.CommonsRequestLoggingFilter">
        <level value="DEBUG"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="LogzioLogbackAppender"/>
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>


```
## Ikke eksponer hemmelighet !

Ta ut logzio URL pg logzio token til miljøvariabel. Dette kan gjøres ved å sette inn $LOGZ_TOKEN og $LOGZ_URL inn i logback filen, og sette verdiene LOGZ_URL og LOGZ_TOKEN som en miljøvariabel 

## Lek med Logz.io

Bli kjent med minst kibana og "Discover" panelet, der man kan søke etter logger og filtrere på ulike felter 
