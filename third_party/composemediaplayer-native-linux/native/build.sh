#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
OUTPUT_DIR="${NATIVE_LIBS_OUTPUT_DIR:-$SCRIPT_DIR/../../resources/composemediaplayer/native}"

echo "=== Building Linux NativeVideoPlayer ==="
echo "Output dir: $OUTPUT_DIR"

# Clean and create build directory
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

cd "$BUILD_DIR"
cmake "$SCRIPT_DIR" -DCMAKE_BUILD_TYPE=Release
cmake --build . --parallel

echo "=== Build completed ==="

# Show output location
echo "Output: $OUTPUT_DIR/linux/libNativeVideoPlayer.so"
