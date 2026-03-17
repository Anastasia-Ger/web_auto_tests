Write-Host ">>> Stop Docker Compose"
docker compose down


Write-Host ">>> Docker pull all images for Selenoid"
# Путь к browsers.json (при необходимости измени)
$configPath = "./config/browsers.json"

# Читаем JSON
$browsers = Get-Content $configPath | ConvertFrom-Json

# Собираем все image из JSON
$images = @()

foreach ($browser in $browsers.PSObject.Properties.Value) {
    foreach ($version in $browser.versions.PSObject.Properties.Value) {
        $images += $version.image
    }
}

# Убираем дубликаты (на всякий случай)
$images = $images | Select-Object -Unique

# Пуллим каждый образ
foreach ($image in $images) {
    Write-Host "Pulling $image ..."
    docker pull $image
}

Write-Host ">>> Start Docker Compose"
docker compose up




