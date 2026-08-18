#!/bin/sh
set -e

. "$(dirname -- "$0")/android-env.sh"

emulator -list-avds
