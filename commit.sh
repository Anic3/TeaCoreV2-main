#!/bin/bash

# ==== Cấu hình ====
GITHUB_TOKEN="YOUR_TOKEN_HERE"   # Thay YOUR_TOKEN_HERE bằng token của bạn
GITHUB_USER="Tuanvo29110" # Thay YOUR_USERNAME_HERE bằng username GitHub
REPO_NAME="TeaCoreV2"  # Tên repo, ví dụ my-project
BRANCH="main"                     # Nhánh muốn push, ví dụ main hoặc master
# =================

# Lấy thời gian hiện tại
DATE=$(date +"%d/%m/%Y %H:%M")

# Commit message tự động
COMMIT_MESSAGE="Auto Commit $DATE"

# Thêm tất cả thay đổi
git add .

# Commit với message tự động
git commit -m "$COMMIT_MESSAGE"

# Thay URL remote bằng token
REMOTE_URL="https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/${GITHUB_USER}/${REPO_NAME}.git"

# Push lên remote
git push "$REMOTE_URL" "$BRANCH"