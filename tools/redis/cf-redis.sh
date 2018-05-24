#!/usr/bin/env bash

REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_DB="${REDIS_DB:-0}"
KM_ROOT="codefeedr:keymanager"


function redis_cmd {
    # -h hostname -p port -a password -n db
    redis-cli $1
}

function create_target {
    echo "${KM_ROOT}:$1"
}

function usage {
    echo
    echo USAGE:
    echo "	$0 [-p <password>] [-d <database_number>] [-H <hostname>] [-P <port>] [-r <root>] command..."
    echo ""
    echo " commands:"
    echo "   list <target>"
    echo "   add <target> <key> <limit> <interval>"
    echo "   delete <target> <key>"
    echo ""
}

while getopts P:H:p:d:r:ha opt; do
	case $opt in
		p)
			REDIS_PW=${OPTARG}
			;;
		H)
			REDIS_HOST=${OPTARG}
			;;
		P)
			REDIS_PORT=${OPTARG}
            ;;
        d)
			REDIS_DB=${OPTARG}
			;;
        r)
			KM_ROOT=${OPTARG}
			;;
		h)
			usage
			exit 1
			;;
	esac
done
shift $(expr $OPTIND - 1 )

if [[ $1 == "list" ]]; then
    if [[ -z $2 ]]; then
        echo "No enough parameters for command 'list'"
        usage
        exit 1
    fi

    TARGET=$( create_target $2 )
    echo "Listing $TARGET"

    redis_cmd "ZRANGEBYSCORE $TARGET:keys -inf inf"
elif [[ $1 == "add" ]]; then
    if [[ -z $5 ]]; then
        echo "No enough parameters for command 'add'"
        usage
        exit 1
    fi

    TARGET=$( create_target $2 )
    KEY=$3
    LIMIT=$4
    INTERVAL=$5

    redis_cmd "ZADD $TARGET:keys $LIMIT $KEY"
    redis_cmd "ZADD $TARGET:refreshTime 0 $KEY"
    redis_cmd "HSET $TARGET:limit $KEY $LIMIT"
    redis_cmd "HSET $TARGET:interval $KEY $INTERVAL"

    exit 0
elif [[ $1 ==  "delete" ]]; then
    if [[ -z $3 ]]; then
        echo "No enough parameters for command 'delete'"
        usage
        exit 1
    fi

    TARGET=$( create_target $2 )
    KEY=$3

    echo "Deleting key $KEY for target $TARGET"

    redis_cmd "ZREM $TARGET:keys $KEY"
    redis_cmd "ZREM $TARGET:refreshTime $KEY"
    redis_cmd "HDEL $TARGET:limit $KEY"
    redis_cmd "HDEL $TARGET:interval $KEY"

    exit 0
else
    echo "Command not found"
fi