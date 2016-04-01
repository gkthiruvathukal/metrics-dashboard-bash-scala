#! /bin/bash/

echo "Ok, let me start..."

args=("$@")

echo ${args[0]} 'is the sha for the cloned repo'
sha=${args[0]}
reponame=${args[1]}
branchname=${args[2]}

if [ ! -d commitsMetrics ]; then
    mkdir commitsMetrics
fi

cd commitsMetrics
mkdir ${sha}
cd ${sha}

git init
git remote add parentNode /home/shilpika/scratch/metrics-dashboard-bash-scala/${reponame}
git pull parentNode ${branchname}
git reset --hard ${sha}

/home/thiruvat/code/cloc/cloc --by-file --report-file=../clocByFile.txt .


