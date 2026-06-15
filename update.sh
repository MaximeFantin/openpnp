#!/bin/bash

SETUP_FOLDER="/home/openpnp/Bureau/OpenPnP/"


PATH=$PATH":/home/openpnp/.java/apache-maven-3.9.15/bin/:/home/openpnp/.java/jdk-11.0.2/bin/"
LIGHT_GREEN='\033[1;32m'
LIGHT_RED='\033[1;31m'
BLANK='\033[0m'


cd $SETUP_FOLDER

VER=""
CUR_VER=""

if [[ -f "openpnp/VERSION.txt" ]]
then
    VER=$(curl https://github.com/openpnp/openpnp/blob/main/VERSION.txt | grep 'id="LC1"')
    VER=${VER#*LC1}
    VER=${VER#*>}
    VER=${VER%%<*}

    CUR_VER=$(cat openpnp/VERSION.txt)
fi

UPDATE_ERROR=0
if [[ ("$CUR_VER" != "$VER") || (-z "$CUR_VER") || ($1 == "force") ]]
then
    cd data

    if [[ -d "openpnp" ]];
    then
        rm -rf openpnp
    fi

    git clone https://github.com/openpnp/openpnp
    cp PhotonSlotLight.java openpnp/src/main/java/org/openpnp/vision/pipeline/stages
    sed -i "2iimport org.openpnp.vision.pipeline.stages.PhotonSlotLight;" openpnp/src/main/java/org/openpnp/vision/pipeline/ui/CvPipelineEditor.java

    IFS=":"
    read -ra ADDR <<< $(grep -Fn "stageClasses = new HashSet<>();" openpnp/src/main/java/org/openpnp/vision/pipeline/ui/CvPipelineEditor.java)
    LINE=$((${ADDR[0]} + 1))
    sed -i $LINE"iregisterStageClass(PhotonSlotLight.class);" openpnp/src/main/java/org/openpnp/vision/pipeline/ui/CvPipelineEditor.java

    cd openpnp
    mvn package -Dmaven.test.skip
    if [[ $? == 1 ]]
    then
        UPDATE_ERROR=1
        echo -e $LIGHT_RED"\n\nErreur de compilation"$BLANK
        echo -e $LIGHT_RED"Echèque de la mise à jour"$BLANK
    else
        cp -r . ../../openpnp
        echo -e $LIGHT_GREEN"\n\nMis à jour avec succès"$BLANK    
        ./../../openpnp/openpnp.sh&

        sleep 0.5
        cp -n ../scripts/* ~/.openpnp2/scripts/
    fi
else
    echo -e $LIGHT_GREEN"\n\nDéjà à jour"$BLANK
    ./openpnp/openpnp.sh&
fi

if [[ $UPDATE_ERROR == 0 ]]
then
    sleep 2
    kill -9 $(ps -o ppid= $$)
fi