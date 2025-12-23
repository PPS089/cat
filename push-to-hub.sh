# 0) 参数（按需修改）
DOCKERHUB_USER="registry.cn-hangzhou.aliyuncs.com/cjie6620"   
TAG="v1.0.0"

# 1) 登录 Docker Hub（会提示输入用户名和密码/Access Token）
docker login

# 2) 构建并打 Tag：后端（指定 Dockerfile 路径和构建上下文）
docker build -f "pet-project/Dockerfile" -t "${DOCKERHUB_USER}/pet-backend:${TAG}" "pet-project"
docker tag "${DOCKERHUB_USER}/pet-backend:${TAG}" "${DOCKERHUB_USER}/pet-backend:latest"

# 3) 构建并打 Tag：前端
#   - 默认会在 pet-view-refactor 目录下寻找 Dockerfile
#   - 构建上下文也是 pet-view-refactor
docker build -t "${DOCKERHUB_USER}/pet-frontend:${TAG}" "pet-view-refactor"
docker tag "${DOCKERHUB_USER}/pet-frontend:${TAG}" "${DOCKERHUB_USER}/pet-frontend:latest"

# 4) 推送所有标签到 Docker Hub
docker push "${DOCKERHUB_USER}/pet-backend:${TAG}"
docker push "${DOCKERHUB_USER}/pet-backend:latest"
docker push "${DOCKERHUB_USER}/pet-frontend:${TAG}"
docker push "${DOCKERHUB_USER}/pet-frontend:latest"

# 5) 校验（可选，拉取验证是否推送成功）
docker pull "${DOCKERHUB_USER}/pet-backend:${TAG}"
docker pull "${DOCKERHUB_USER}/pet-frontend:${TAG}"
echo "所有操作完成！镜像已推送至："
echo "https://hub.docker.com/r/${DOCKERHUB_USER}/pet-backend"
echo "https://hub.docker.com/r/${DOCKERHUB_USER}/pet-frontend"