---
title: Redis
tags: [internals, architecture]
keywords: internals, architecure, redis, keymanager
permalink: mydoc_redis_keymanager.html
sidebar: mydoc_sidebar
folder: mydoc
---

The Redis key manager implements the full key manager for Redis. It
supports keeping track of the number of calls a key has left and
automatic refreshing of that number over an interval.

### Configuring

Configuring the pipeline to use redis is easy. All that is needed is an
address to the Redis instance.

```scala
val km = new RedisKeyManager("redis://localhost:6379")
```

By default the keys are managed in `codefeedr:keymanager:*`. If you want
this changed, you can supply a new 'root' as the second constructor
argument. For example `km` for putting keys in `km:*`.

### Adding keys

When adding a key to Redis, a refresh policy is required:

- _Key_ is the API key to store
- _Limit_ is the number of calls the key allows within a time interval
- _Interval_ is the number of milliseconds between two resets of the use
count.
- _Time_ is the next time the key counter should reset, in milliseconds
since the Unix epoch (Jan 1st, 1970)

When, for example, a key has 1000 calls every day, one would use:
`limit = 1000, interval = 60 * 60 * 24 * 1000, time = next midnight`
```
ZADD codefeedr:keymanager:[target]:keys [limit] [key]
ZADD codefeedr:keymanager:[target]:refreshTime [time] [key]
HSET codefeedr:keymanager:[target]:limit [key] [limit]
HSET codefeedr:keymanager:[target]:interval [key] [interval]
```

The 'codefeedr:keymanager' can be replaced by another path, but this
also needs to be configured when using the RedisKeyManager

### Removing keys

Removing a key is the reverse of adding: remove it from every collection
the properties are in.

```
targetKey = [root]:[target]
ZREM [targetKey]:keys [key]
ZREM [targetKey]:refreshTime [key]
HDEL [targetKey]:limit [key]
HDEL [targetKey]:interval [key]
```
