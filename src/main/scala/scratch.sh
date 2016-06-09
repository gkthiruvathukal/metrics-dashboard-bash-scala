#! /bin/bash

echo "Ok, let me start..."

args=("$@")

echo ${args[0]} 'is the sha for the cloned repo'
sha=${args[0]}
reponame=${args[1]}
branchname=${args[2]}

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
if [ ! -d ${reponame} ]; then
     git clone /projects/ExaHDF5/sshilpika/${reponame} /scratch/sshilpika/${reponame}
fi

cd ${reponame}

if [ ! -d results ]; then
    mkdir results
fi

if [ ! -d commitsMetrics ]; then
    mkdir commitsMetrics
fi

cd commitsMetrics

mkdir ${sha} && cd ${sha}

git init
git remote add parentNode /scratch/sshilpika/${reponame}
git pull parentNode ${branchname}
git reset --hard ${sha}

#inside scratch/sshilpika/(repo)/commitsMetrics/(sha)

git log -1 --pretty=format:'%ci' >> /scratch/sshilpika/${reponame}/results/${sha}_date.txt
pwd
echo 'cloc now'
/scratch/sshilpika/cloc/cloc --by-file --report-file=/scratch/sshilpika/${reponame}/results/${sha}_clocByFile.txt .

echo 'cloc done'

