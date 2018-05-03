--
-- Script for getting a new key from the KeyManager stored in Redis.
--
-- It first finds elements, sorted decrementing by score, with a score with at least
-- the number of calls we want. Ater a check, it decreases that number with the
-- number of calls we want and finishes by returning the key and the number of
-- calls left.
--

local targetKey = KEYS[1]

-- Find value
local hit = redis.call("ZRANGEBYSCORE", targetKey .. ":keys", tonumber(ARGV[1]), "+inf", "WITHSCORES", "LIMIT", 0, 1)

-- Find key to refresh
local toRefresh = redis.call("ZRANGEBYSCORE", targetKey .. ":refreshTime", 0, tonumber(ARGV[2]), "WITHSCORES", "LIMIT", 0, 1)
if #toRefresh ~= 0 then
    local key = toRefresh[1]
    local interval = redis.call("HGET", targetKey .. ":interval", key)
    local limit = redis.call("HGET", targetKey .. ":limit", key)
    local lastRefresh = toRefresh[2]

    redis.call("ZADD", targetKey .. ":refreshTime", lastRefresh + interval, key)
    redis.call("ZADD", targetKey .. ":keys", limit, key)

    -- If there was no hit, do use the refreshed key
    if #hit == 0 then
        hit = {key, limit}
    end
end

-- Check if we had a key
if #hit == 0 then
    return {}
end

-- Update key
local diff = -1 * ARGV[1]
redis.call("ZINCRBY", targetKey .. ":keys", diff, hit[1])

return {hit[1], hit[2] + diff}
