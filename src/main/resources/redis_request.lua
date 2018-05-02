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
local getHit = function ()
    return redis.call("ZRANGEBYSCORE", targetKey .. ":keys", tonumber(ARGV[1]), "+inf", "WITHSCORES", "LIMIT", 0, 1)
end

local hit = getHit()

-- Check
if #hit == 0 then
--    local canRefresh = false
--
--    if canRefresh then
--        -- Do refresh
--
--
--        hit = getHit()
--        if #hit == 0 then
--            return {}
--        end
--    else
        return {}
--    end
end

-- Update key
local diff = -1 * ARGV[1]
redis.call("ZINCRBY", targetKey .. ":keys", diff, hit[1])

return {hit[1], hit[2] + diff}
