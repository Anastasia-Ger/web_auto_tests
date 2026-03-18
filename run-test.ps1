# НАСТРОЙКА
param(
 [string]$TEST_PROFILE = "api"
)

$IMAGE_NAME = "nbank-tests"

# собираем докер-образ
docker build -t $IMAGE_NAME .

# создаем папки для сборки логов
New-Item -ItemType Directory -Force -Path logs   | Out-Null78
New-Item -ItemType Directory -Force -Path results | Out-Null
New-Item -ItemType Directory -Force -Path report  | Out-Null

# абсолютный путь текущей директории
$CURRENT_DIR = (Get-Location).Path

# запуск докер контейнера
docker run --name nbank-container `
    -v "${CURRENT_DIR}/logs:/app/logs" `
    -v "${CURRENT_DIR}/results:/app/target/surefire-reports" `
    -v "${CURRENT_DIR}/report:/app/target/site" `
    -e TEST_PROFILE=$TEST_PROFILE `
    -e APIBASEURL=http://192.168.8.52:4111 `
    -e UIBASEURL=http://192.168.8.52:4111 `
    $IMAGE_NAME
