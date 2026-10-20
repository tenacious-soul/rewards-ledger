#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"
exec sh ./mvnw spring-boot:run
