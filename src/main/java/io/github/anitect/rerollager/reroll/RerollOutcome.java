package io.github.anitect.rerollager.reroll;

import org.jspecify.annotations.NullMarked;

/** Result of a reroll attempt, with the remaining cooldown when relevant. */
@NullMarked
public record RerollOutcome(Status status, long cooldownRemainingMillis) {

    public enum Status { SUCCESS, NOT_ELIGIBLE, LOCKED, ON_COOLDOWN, CANNOT_AFFORD, FAILED }

    public static RerollOutcome of(Status status) {
        return new RerollOutcome(status, 0L);
    }

    public static RerollOutcome onCooldown(long remainingMillis) {
        return new RerollOutcome(Status.ON_COOLDOWN, remainingMillis);
    }
}
