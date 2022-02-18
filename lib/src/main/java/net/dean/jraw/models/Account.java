package net.dean.jraw.models;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import net.dean.jraw.RedditClient;
import net.dean.jraw.databind.RedditModel;
import net.dean.jraw.databind.UnixTime;
import net.dean.jraw.references.Referenceable;
import net.dean.jraw.references.UserReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;

@AutoValue
@RedditModel
public abstract class Account implements Created, Referenceable<UserReference<?>>, Serializable, UniquelyIdentifiable {
    /** The amount of Karma this user has acquired through comment */
    @Json(name = "comment_karma") public abstract int getCommentKarma();

    @NotNull
    @Override
    @Json(name = "created_utc") @UnixTime public abstract Date getCreated();

    /** If the currently logged in user is friends with this account */
    @Json(name = "is_friend") public abstract boolean isFriend();

    /** If this user is a moderator */
    @Json(name = "is_mod") public abstract boolean isModerator();

    /** If this property is true, the user has reddit Gold */
    @Json(name = "is_gold") public abstract boolean isGoldMember();

    /** True if this user has verified ownership of the email address used to create their account */
    @Json(name = "has_subscribed") public abstract boolean getHasSubscribed();

    /** True if this user has verified ownership of the email address used to create their account. May be null. */
    @Nullable
    @Json(name = "has_verified_email") public abstract Boolean getHasVerifiedEmail();

    /** The amount of karma gained from submitting links */
    @Json(name = "link_karma") public abstract int getLinkKarma();

    /** The name chosen for this account by a real person */
    @Json(name = "name") public abstract String getName();

    /** URL to user's avatar */
    @Json(name = "icon_img")
    public abstract String getIcon();

    @Nullable
    @Json(name = "subreddit")
    public abstract Profile getProfile();

    // TODO: a lot more properties for logged-in users (see /api/v1/me)

    @NotNull
    @Override public String getUniqueId() { return getName(); }

    @NotNull
    @Override
    public UserReference toReference(@NotNull RedditClient reddit) {
        return reddit.user(getName());
    }

    public static JsonAdapter<Account> jsonAdapter(Moshi moshi) {
        return new AutoValue_Account.MoshiJsonAdapter(moshi);
    }

    @AutoValue
    public abstract static class Profile {
        @Nullable
        @Json(name = "title")
        public abstract String getDisplayName();

        @Nullable
        @Json(name = "public_description")
        public abstract String getAbout();

        public static JsonAdapter<Profile> jsonAdapter(Moshi moshi) {
            return new AutoValue_Account_Profile.MoshiJsonAdapter(moshi);
        }
    }
}
