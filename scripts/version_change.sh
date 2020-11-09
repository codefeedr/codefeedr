#!/bin/bash
echo "Updating to version $1 in $2"

ORIGINAL="ThisBuild \/ version :=.*$"
NEW="ThisBuild \/ version := \"$1\""

sed -i "s/${ORIGINAL}/${NEW}/" $2