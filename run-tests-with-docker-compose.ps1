Write-Host ">>> Stop Docker Compose"
docker compose -f infra/docker_compose/docker-compose.yml down

Write-Host ">>> Pull browser images for Selenoid"

$configPath = "./infra/docker_compose/config/browsers.json"
$browsers = Get-Content $configPath | ConvertFrom-Json

$images = @()

foreach ($browser in $browsers.PSObject.Properties.Value) {
    foreach ($version in $browser.versions.PSObject.Properties.Value) {
        $images += $version.image
    }
}

$images = $images | Select-Object -Unique

foreach ($image in $images) {
    Write-Host "Pulling $image ..."
    docker pull $image
}

Write-Host ">>> Start Docker Compose"
docker compose -f infra/docker_compose/docker-compose.yml up -d

Write-Host ">>> Waiting for services to start..."
Start-Sleep -Seconds 15

$IMAGE_NAME = "nbank-tests"

Write-Host ">>> Build test image"
docker build -t nbank-tests .

Write-Host ">>> Prepare folders"
New-Item -ItemType Directory -Force -Path logs   | Out-Null
New-Item -ItemType Directory -Force -Path results | Out-Null
New-Item -ItemType Directory -Force -Path report  | Out-Null

$CURRENT_DIR = (Get-Location).Path

Write-Host ">>> Run tests"

docker run --rm `
    --name nbank-tests-container `
    --network nbank-network `
    -v "${CURRENT_DIR}/logs:/app/logs" `
    -v "${CURRENT_DIR}/results:/app/target/surefire-reports" `
    -v "${CURRENT_DIR}/report:/app/target/site" `
    -e APIBASEURL=http://backend:4111 `
    -e UIBASEURL=http://nginx:3000 `
    -e SELENOID_URL=http://selenoid:4444 `
    -e SELENOID_UI_URL=http://selenoid-ui:8080 `
    $IMAGE_NAME

Write-Host ">>> Tests finished"

