#!/bin/bash
# 上传到稳定版环境中
scp admin@Star:/data/volumes/display-deployment/version.txt remote-version.txt
echo "服务器端部署的版本：$(cat remote-version.txt | tail -n 1)"
echo "$(sbt version | tail -n 1)" > version.txt
echo "本地准备上传的版本：$(cat version.txt | tail -n 1)"
scp version.txt admin@Star:/data/volumes/display-deployment/version.txt
scp Display.jar admin@Star:/data/volumes/display-deployment/Display.jar
echo "上传成功！"
