#!/bin/bash
#./Weather.sh 'Global_Video' 'v1.2.3.4.3432.1'
if [ $1 == 'Global_Video' ]; then
  sed -i "s/versionName *\"v[0-9.]*\"/versionName \"$2_video\"/" ./app/build.gradle
  sed -i s'/<bool name="is_background_dynamic">false<\/bool>/<bool name="is_background_dynamic">true<\/bool>/' ./app/src/main/res/values/isdm_JrdWeather_defaults.xml
  rm -rf ./app/src/main/res/raw/*
  cp -rf ./app/src/video/res/raw/* ./app/src/main/res/raw
else
  echo "No change."
fi
