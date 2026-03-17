# create variables for image name, Docker Hub user and tag
$IMAGE_NAME = "nbank-tests"
$DOCKER_HUB_USER = "anakipling"
$TAG = "latest"

# read token from env
$DOCKER_TOKEN = $env:DOCKERHUB_TOKEN
if ([string]::IsNullOrWhiteSpace($DOCKER_TOKEN)) {
    throw "DOCKERHUB_TOKEN environment variable is not set."
}
# login
$DOCKER_TOKEN | docker login -u $DOCKER_HUB_USER --password-stdin

# create full name for tagged image
$FULL_IMAGE_NAME = "${DOCKER_HUB_USER}/${IMAGE_NAME}:${TAG}"

# tag image name
docker tag $IMAGE_NAME $FULL_IMAGE_NAME

# push image to docker hub
docker push $FULL_IMAGE_NAME

# final message with instruction how to pull
Write-Host ""
Write-Host "Image successfully pushed to Docker Hub!"
Write-Host "To pull this image, run:"
Write-Host "docker pull $FULL_IMAGE_NAME"