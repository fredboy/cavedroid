#!/usr/bin/env bash

previous_tag=0
i=0
for current_tag in $(git tag --sort=-creatordate)
do

if [ "$previous_tag" != 0 ] &&  [ $i -lt 1 ]; then
    i=$(echo "$i + 1" | bc -q )
    tag_date=$(git log -1 --pretty=format:'%ad' --date=short ${previous_tag})
    printf "## ${previous_tag} (${tag_date})\n\n"
    git log ${current_tag}...${previous_tag} --pretty=format:'*  %s ' --reverse | grep -v Merge
    printf "\n\n"
fi
previous_tag=${current_tag}
done
