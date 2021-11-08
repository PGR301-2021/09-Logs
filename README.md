# Logz.io

I denne labben skal vi se hvor enkelt det er å sende logger til en SAAS tjeneste som heter logz.io fra Spring Boot applikasjonen. Vi skal også se på 12 factor prinsippet "Config" ved å ikke hardkode token og endepunlt for Logz.io i property filer. 

I denne øvelsen kan det være lurt å bruke en enkel Spring Boot applikasjon som kan deployes til Heroku. 

## Registrer deg på logz.io

https://app.logz.io *
* Velg "free trial". 
* Bruk Frankfurt (AWS) som destinasjon
* Velg "Send your logs" 
* ![Alt text](img/1.png  "a title")
* Velg Java Application
* ![Alt text](img/2.png  "a title")
* Velg "Logback" fanen
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
    <!-- Use shutdownHook so that we can close gracefully and finish the log drain -->
    <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>
    <appender name="LOGZIO" class="io.logz.logback.LogzioLogbackAppender">
        <token>$LOGZ_TOKEN</token>
        <logzioUrl>https://listener.logz.io:8071</logzioUrl>
        <logzioType>myType</logzioType>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
    </appender>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%-4relative [%thread] %-5level %logger{35} - %msg %n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="LOGZIO" />
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>


```
## Ikke eksponer hemmelighet !

Ta ut logzio URL pg logzio token til miljøvariabel. Dette kan gjøres ved å sette inn $LOGZ_TOKEN og $LOGZ_URL inn i logback filen, og sette verdiene LOGZ_URL og LOGZ_TOKEN som en miljøvariabel 

## Lek med Logz.io

Bli kjent med minst 

* Live Tail
* kibana og "Discover" panelet, der man kan søke etter logger og filtrere på ulike felter i loggene
* Visialiering. Prøv gjerne mer avanserte funksjoner - men i alle fall et Pie chart som fordeler logger i INFO, DEBUG, ERROR
