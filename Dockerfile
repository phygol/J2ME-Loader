# Dockerfile for building J2ME Loader ARM64 APK
# 使用国内镜像加速

# ============================================
# Stage 1: Build environment with Android SDK
# ============================================
FROM dockerpull.cn/gradle:8.5-jdk17 AS builder

# 设置环境变量
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/build-tools/34.0.0

# 安装必要工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# 下载 Android SDK Command Line Tools (使用国内镜像)
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q -O /tmp/cmdline-tools.zip \
    https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# 接受许可证并安装 SDK 组件
RUN yes | sdkmanager --licenses && \
    sdkmanager --update && \
    sdkmanager \
    "platforms;android-36" \
    "build-tools;34.0.0" \
    "platform-tools" \
    "ndk;26.1.10909125"

# ============================================
# Stage 2: Build the APK
# ============================================
WORKDIR /app

# 复制 Gradle 配置文件（利用缓存）
COPY settings.gradle.kts build.gradle.kts gradle.properties* ./
COPY app/build.gradle.kts app/
COPY app/proguard-rules.pro app/

# 预下载依赖（利用 Docker 层缓存）
RUN gradle :app:dependencies --no-daemon 2>&1 | tail -20

# 复制源码
COPY app/src ./app/src

# 构建 Release APK (仅 ARM64)
RUN gradle :app:assembleRelease --no-daemon -Pandroid.abiFilters=arm64-v8a

# ============================================
# Stage 3: Extract APK
# ============================================
FROM dockerpull.cn/alpine:3.20 AS extractor
WORKDIR /out
COPY --from=builder /app/app/build/outputs/apk/release/ ./
RUN ls -la *.apk

# 最终输出
CMD ["sh", "-c", "cp *.apk /output/ 2>/dev/null || echo 'No APK found'"]