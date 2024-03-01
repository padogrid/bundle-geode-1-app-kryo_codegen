# Copyright (c) 2020 Netcrest Technologies, LLC. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ========================================================================

#
# Enter app specifics in this file.
#

# Cluster level variables:
# ------------------------
# BASE_DIR - padogrid base dir
# ETC_DIR - Cluster etc dir
# LOG_DIR - Cluster log dir

# App level variables:
# --------------------
# APPS_DIR - <padogrid>/apps dir
# APP_DIR - App base dir
# APP_ETC_DIR - App etc dir
# APP_LOG_DIR - App log dir

# Set JAVA_OPT to include your app specifics.
JAVA_OPTS="-Xms512m -Xmx512m"

# GEMFIRE_PROPERTY_FILE defaults to etc/client-gemfire.properties
# GEODE_CLIENT_CONFIG_FILE defaults to etc/client-cache.xml
JAVA_OPTS="$JAVA_OPTS -DgemfirePropertyFile=$GEMFIRE_PROPERTY_FILE \
    -Dgemfire.cache-xml-file=$GEODE_CLIENT_CONFIG_FILE"

# Add log4j which was taken out by the main .addonenv.sh to prevent jar conflicts
# for running clusters. The commands in tools do not have cluster dependencies.
for i in $PADOGRID_HOME/lib/*; do
  if [[ "$i" != *"slf4j"* ]] && [[ "$i" == *"log4j"* ]]; then
     CLASSPATH="$CLASSPATH:$i"
  fi
done

CLASSPATH="$APP_DIR/lib/*:$CLASSPATH"
