package net.dean.jraw.models;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.List;
import java.util.Map;
import net.dean.jraw.RedditClient;
import net.dean.jraw.databind.Enveloped;
import net.dean.jraw.databind.RedditModel;
import net.dean.jraw.databind.UnixTime;
import net.dean.jraw.references.CommentReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;

@AutoValue
@RedditModel
public abstract class Comment implements PublicContribution<CommentReference>, NestedIdentifiable, Serializable {

    @Override
    @NotNull
    public abstract String getAuthor();

    @Nullable
    @Json(name = "author_flair_text")
    public abstract String getAuthorFlairText();

    @Nullable
    @Json(name = "author_flair_richtext")
    public abstract List<Flair.RichTextSpan> getAuthorFlairRichText();

    @Override
    @Json(name = "can_gild") public abstract boolean isGildable();

    /**
     * Get this comments controversiality level. A comment is considered controversial if it has a large number of both
     * upvotes and downvotes. 0 means not controversial, 1 means controversial.
     */
    public abstract int getControversiality();

    @NotNull
    @Override
    @Json(name = "created_utc") @UnixTime public abstract Date getCreated();

    @Override
    @NotNull
    public abstract DistinguishedStatus getDistinguished();

    @Nullable
    @Override
    @UnixTime public abstract Date getEdited();

    @NotNull
    @Override
    @Json(name = "name") public abstract String getFullName();

    /** Comment body is never null */
    @NotNull
    @Override
    public abstract String getBody();

    @NotNull
    @Json(name = "body_html") public abstract String getBodyHtml();

    @Enveloped
    public abstract Listing<NestedIdentifiable> getReplies();

    @NotNull
    @Override
    @Json(name = "parent_id") public abstract String getParentFullName();

    @NotNull
    @Json(name = "link_id") public abstract String getSubmissionFullName();

    /** Gets the title of the parent link, or null if this comment is not being displayed outside of its own thread. */
    @Nullable
    @Json(name = "link_title") public abstract String getSubmissionTitle();

    /**
     * Gets the url of the parent submission (or submission permalink for self-posts), or null if this comment
     * is not being displayed outside of its own thread.
     */
    @Nullable
    @Json(name = "link_url") public abstract String getUrl();

    @NotNull
    @Override
    @Json(name = "subreddit_id") public abstract String getSubredditFullName();

    @Json(name = "subreddit_type") public abstract Subreddit.Access getSubredditType();

    @Override
    @Json(name = "score_hidden") public abstract boolean isScoreHidden();

    /** Whether replies can be made to this comment. */
    @Json(name = "locked") public abstract boolean isLocked();

    @Json(name = "collapsed") public abstract boolean isCollapsed();

    @NotNull
    @Override public String getUniqueId() { return getFullName(); }

    @NotNull
    @Override
    @Json(name = "likes") public abstract VoteDirection getVote();

    /** Path of the permalink to this comment, without 'https://reddit.com'. */
    @NotNull
    @Json(name = "permalink")
    public abstract String getPermalink();

    /**
     * Whether or not the comment author is also the author of the submission.
     */
    @Json(name = "is_submitter")
    public abstract boolean isSubmitter();

    @Override
    @Nullable
    @Json(name = "media_metadata")
    public abstract Map<String, MediaMetadataItem> getMediaMetadata();

    @NotNull
    @Override
    public CommentReference toReference(@NotNull RedditClient reddit) {
        return new CommentReference(reddit, getId());
    }

    public static JsonAdapter<Comment> jsonAdapter(Moshi moshi) {
        return new AutoValue_Comment.MoshiJsonAdapter(moshi);
    }
}
