# базовый докер образ
# можно создать образ поверх другого образа, в кт всё уже установлено
# создаем свой докер образ на основе установочного образа. Как его выбрать? - на основании того, какие
# утилиты нам понадобятся. Мне понад.maven
# идем в маркетплейс всех докер образов - docker hub
FROM maven:3.9.6-eclipse-temurin-21

# дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# работаем из папки /app
WORKDIR /app

# копируем помник
COPY pom.xml .

# загружаем зависимости и кешируем
RUN mvn dependency:go-offline

# копируем весь проект
COPY src ./src

# теперь внутри есть зависимости, есть весь проект и мы готовы запускать  тесты

USER root

# что хотим делать/запускать:
# mvn test -P api  запуск только апи тестов
# mvn -DskipTests=true surefire-report:report  запуск только шурфаир-репортов
# лог выводился не в консоль, а в файл
# bash file
CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
    } 2>&1 | tee /app/logs/run.log"

