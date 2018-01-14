**Требования к окружению**
* Java 1.8
* Maven

**Запуск**

`mvn clean install test surefire-report:report`

В результате выполнения команды будут выполнены автотесты из `src/test/java/ru/hh/school/test/VacanciesSearchTest.java`
и построены отчеты:
* В `target\surefire-reports` будут представлены текстовый и подробный xml-отчеты.
* В `target\site` будет представлен html отчет.