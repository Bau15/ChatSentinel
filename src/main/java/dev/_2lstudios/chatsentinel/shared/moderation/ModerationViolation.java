package dev._2lstudios.chatsentinel.shared.moderation;

import java.util.Objects;
import java.util.Optional;

import dev._2lstudios.chatsentinel.shared.filter.FilterMatch;

public final class ModerationViolation {
    private final ModerationIdentity identity;
    private final ModerationActionSettings actionSettings;
    private final FilterMatch filterMatch;

    public ModerationViolation(ModerationIdentity identity, ModerationActionSettings actionSettings, FilterMatch filterMatch) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.actionSettings = Objects.requireNonNull(actionSettings, "actionSettings");
        this.filterMatch = filterMatch;
    }

    public ModerationIdentity getIdentity() {
        return identity;
    }

    public ModerationActionSettings getActionSettings() {
        return actionSettings;
    }

    public Optional<FilterMatch> getFilterMatch() {
        return Optional.ofNullable(filterMatch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModerationViolation)) {
            return false;
        }
        ModerationViolation that = (ModerationViolation) o;
        return identity.equals(that.identity)
                && actionSettings.equals(that.actionSettings)
                && Objects.equals(filterMatch, that.filterMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, actionSettings, filterMatch);
    }

    @Override
    public String toString() {
        return "ModerationViolation{" +
                "identity=" + identity +
                ", actionSettings=" + actionSettings +
                ", filterMatch=" + filterMatch +
                '}';
    }
}
