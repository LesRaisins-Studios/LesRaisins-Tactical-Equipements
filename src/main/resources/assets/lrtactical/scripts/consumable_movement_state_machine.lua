-- Opt in with state_machine = "lrtactical:consumable_movement_state_machine".
-- Main actions follow consumable_state_machine; movement uses a separate additive track.
local track_line_top = {value = 0}
local static_track_top = {value = 0}
local blending_track_top = {value = 0}

local function increment(obj)
    obj.value = obj.value + 1
    return obj.value - 1
end

local STATIC_TRACK_LINE = increment(track_line_top)
local BASE_TRACK = increment(static_track_top)
local MAIN_TRACK = increment(static_track_top)
local BLENDING_TRACK_LINE = increment(track_line_top)
local MOVEMENT_TRACK = increment(blending_track_top)
local INPUT_MOVEMENT_IDLE = "movement_idle"

local function run_put_away_animation(context)
    local put_away_time = context:getPutAwayTime()
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    context:runAnimation("put_away", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
    context:setAnimationProgress(track, 1, true)
    context:adjustAnimationProgress(track, -put_away_time, false)
end

local main_track_states = {
    start = {},
    idle = {},
    using = {},
    final = {}
}

local base_track_state = {}

function base_track_state.entry(this, context)
    context:runAnimation("static_idle", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, LOOP, 0)
end

function main_track_states.start.update(this, context)
    context:trigger(INPUT_DRAW)
end

function main_track_states.start.transition(this, context, input)
    if input == INPUT_DRAW then
        context:runAnimation("draw", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
        return this.main_track_states.idle
    end
end

function main_track_states.idle.update(this, context)
    if context:isUsing() then
        context:trigger("start_use")
    end
end

function main_track_states.idle.transition(this, context, input)
    if input == INPUT_PUT_AWAY then
        run_put_away_animation(context)
        return this.main_track_states.final
    elseif input == INPUT_INSPECT then
        context:runAnimation("inspect", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return this.main_track_states.idle
    elseif input == "re_draw" then
        context:runAnimation("draw", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
        return this.main_track_states.idle
    elseif input == "start_use" then
        context:runAnimation("use", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0)
        return this.main_track_states.using
    end
end

function main_track_states.using.update(this, context)
    if not context:isUsing() then
        if context:getStackCount() > 0 then
            context:trigger("re_draw")
        else
            context:trigger("stop_use")
        end
    end
end

function main_track_states.using.transition(this, context, input)
    if input == INPUT_PUT_AWAY then
        run_put_away_animation(context)
        return this.main_track_states.final
    elseif input == "re_draw" then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        context:runAnimation("draw", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
        return this.main_track_states.idle
    elseif input == "stop_use" then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        return this.main_track_states.idle
    end
end

local movement_track_states = {
    idle = {},
    run = {mode = -1},
    walk = {mode = -1}
}

function movement_track_states.idle.update(this, context)
    local track = context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK)
    if context:isStopped(track) or context:isHolding(track) then
        context:runAnimation("idle", track, true, LOOP, 0)
    end
end

function movement_track_states.idle.transition(this, context, input)
    if input == INPUT_RUN then
        if context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) then
            return this.movement_track_states.run
        else
            return this.movement_track_states.walk
        end
    elseif input == INPUT_WALK then
        return this.movement_track_states.walk
    end
end

function movement_track_states.run.entry(this, context)
    this.movement_track_states.run.mode = -1
    context:runAnimation("run_start", context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK), true, PLAY_ONCE_HOLD, 0.2)
end

function movement_track_states.run.exit(this, context)
    context:runAnimation("run_end", context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK), true, PLAY_ONCE_HOLD, 0.3)
end

function movement_track_states.run.update(this, context)
    local track = context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK)
    local state = this.movement_track_states.run
    if context:isHolding(track) then
        context:runAnimation("run", track, true, LOOP, 0.2)
        state.mode = 0
        context:anchorWalkDist()
    end
    if state.mode ~= -1 then
        if not context:isOnGround() then
            if state.mode ~= 1 then
                state.mode = 1
                local animation = context:hasAnimationPrototype("run_hold") and "run_hold" or "idle"
                context:runAnimation(animation, track, true, LOOP, 0.6)
            end
        else
            if state.mode ~= 0 then
                state.mode = 0
                context:runAnimation("run", track, true, LOOP, 0.2)
            end
            context:setAnimationProgress(track, (context:getWalkDist() % 2.0) / 2.0, true)
        end
    end
end

function movement_track_states.run.transition(this, context, input)
    if input == INPUT_MOVEMENT_IDLE then
        return this.movement_track_states.idle
    elseif input == INPUT_WALK or not context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) then
        return this.movement_track_states.walk
    end
end

function movement_track_states.walk.entry(this, context)
    this.movement_track_states.walk.mode = -1
end

function movement_track_states.walk.exit(this, context)
    context:runAnimation("idle", context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK), true, PLAY_ONCE_HOLD, 0.4)
end

function movement_track_states.walk.update(this, context)
    local track = context:getTrack(BLENDING_TRACK_LINE, MOVEMENT_TRACK)
    local state = this.movement_track_states.walk
    if not context:isOnGround() then
        if state.mode ~= 0 then
            state.mode = 0
            context:runAnimation("idle", track, true, LOOP, 0.6)
        end
    elseif context:isInputUp() then
        if state.mode ~= 2 then
            state.mode = 2
            context:runAnimation("walk_forward", track, true, LOOP, 0.4)
            context:anchorWalkDist()
        end
    elseif context:isInputDown() then
        if state.mode ~= 3 then
            state.mode = 3
            context:runAnimation("walk_backward", track, true, LOOP, 0.4)
            context:anchorWalkDist()
        end
    elseif context:isInputLeft() or context:isInputRight() then
        if state.mode ~= 4 then
            state.mode = 4
            context:runAnimation("walk_sideway", track, true, LOOP, 0.4)
            context:anchorWalkDist()
        end
    end
    if state.mode >= 2 and state.mode <= 4 then
        context:setAnimationProgress(track, (context:getWalkDist() % 2.0) / 2.0, true)
    end
end

function movement_track_states.walk.transition(this, context, input)
    if input == INPUT_MOVEMENT_IDLE then
        return this.movement_track_states.idle
    elseif input == INPUT_RUN then
        if context:isStopped(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)) then
            return this.movement_track_states.run
        end
    end
end

local M = {
    track_line_top = track_line_top,
    STATIC_TRACK_LINE = STATIC_TRACK_LINE,
    base_track_state = base_track_state,
    main_track_states = main_track_states,
    movement_track_states = movement_track_states,
}

function M:initialize(context)
    context:ensureTrackLineSize(track_line_top.value)
    context:ensureTracksAmount(STATIC_TRACK_LINE, static_track_top.value)
    context:ensureTracksAmount(BLENDING_TRACK_LINE, blending_track_top.value)
    self.movement_track_states.run.mode = -1
    self.movement_track_states.walk.mode = -1
end

function M:exit(context)
end

function M:states()
    return {
        self.base_track_state,
        self.main_track_states.start,
        self.movement_track_states.idle,
    }
end

return M
