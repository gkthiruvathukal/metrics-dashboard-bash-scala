#! /bin/bash

echo "Ok, let me start..."

args=("$@")

echo ${args[0]} 'is the sha for the cloned repo'
sha=${args[0]}
reponame=${args[1]}
branchname=${args[2]}
username=${args[3]}

cd /scratch/

# base folder
if [ ! -d sshilpika ]; then
    mkdir sshilpika
fi

cd sshilpika

# cloc
if [ ! -d cloc ]; then
     git clone https://github.com/AlDanial/cloc.git
fi

# repo
echo https://github.com/${username}/${reponame}.git
