package net.dean.jraw.test.integration

import com.winterbe.expekt.should
import net.dean.jraw.ApiException
import net.dean.jraw.models.MultiredditPatch
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import net.dean.jraw.references.MultiredditReference
import net.dean.jraw.test.TestConfig.reddit
import net.dean.jraw.test.TestConfig.redditUserless
import net.dean.jraw.test.expectDescendingScore
import net.dean.jraw.test.expectException
import net.dean.jraw.test.randomName
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*

class MultiredditReferenceTest : Spek({
    val undeletedRefs = mutableListOf<MultiredditReference>()

    val me = reddit.me()

    describe("createOrUpdate, about, and delete") {
        it("should create a new multireddit") {
            val name = randomName()
            val ref = me.multi(name)
            val desc = "Created ${Date()}"

            // Create the new multireddit (or overwrite it if in the extremely unlikely chance we've generated a name
            // that the user already has)
            val multi = ref.createOrUpdate(MultiredditPatch.Builder()
                .description(desc)
                .displayName(name)
                .iconName("grooming")
                .keyColor("#FFFFFF")
                .subreddits(listOf("pics", "videos", "funny"))
                .visibility("private")
                .weightingScheme("fresh")
                .build())
            undeletedRefs.add(ref)

            // Make sure we can fetch the data from about()
            ref.about().should.equal(multi)
        }

        it("should throw an ApiException when trying to create a multireddit for another user") {
            expectException(ApiException::class) {
                reddit.user("_vargas_").multi(randomName()).createOrUpdate(MultiredditPatch())
            }
        }
    }

    describe("description and updateDescription") {
        it("should read/update the description") {
            val original = "original description"
            val ref = me.multi(randomName())
            val multi = ref.createOrUpdate(MultiredditPatch(description = original))

            // Ensure cleanup
            undeletedRefs.add(ref)

            multi.description.should.equal(original)
            val newDesc = "new description"
            ref.updateDescription(newDesc)
            ref.description().should.equal(newDesc)
        }
    }

    describe("posts") {
        it("should iterate over the multireddit") {
            val ref = me.multi(randomName()).createOrUpdate(MultiredditPatch.Builder()
                .subreddits("pics", "funny")
                .build()).toReference(reddit)
            // Ensure cleanup
            undeletedRefs.add(ref)

            val posts = ref.posts()
                .sorting(SubredditSort.TOP)
                .timePeriod(TimePeriod.HOUR)
                .limit(10)
                .build()
                .next()

            // Expect a generally descending score
            expectDescendingScore(posts, allowedMistakes = 3)
        }
    }

    describe("copyTo/rename") {
        it("should return a new MultiredditReference") {
            val original = me.createMulti(randomName(), MultiredditPatch()).toReference(reddit)
            undeletedRefs.add(original)

            val copied = original.copyTo(randomName()).toReference(reddit)
            undeletedRefs.add(copied)
            // Make sure we can still reference the original multireddit
            original.about()

            val renamed = copied.rename(randomName()).toReference(reddit)
            undeletedRefs.remove(copied)
            undeletedRefs.add(renamed)
        }

        it("should fail when using application-only creds") {
            // This test doesn't really make sense because even if the request was sent it would fail at the server
            // level because we're creating it using an authenticated client but converting it to a Reference using
            // a userless RedditClient. I don't know why anyone would do this but I guess it's nice to have everything
            // covered
            val original = reddit.me().createMulti(randomName(), MultiredditPatch())
                .toReference(redditUserless)

            undeletedRefs.add(original)
            expectException(IllegalStateException::class) {
                undeletedRefs.add(original.copyTo(randomName()).toReference(reddit))
            }

            expectException(IllegalStateException::class) {
                undeletedRefs.add(original.rename(randomName()).toReference(reddit))
            }
        }

        it("should fail to rename a multireddit not owned by the authenticated user") {
            expectException(ApiException::class) {
                reddit.user("reddit").multi("redditpets").rename(randomName())
            }
        }
    }

    describe("subredditInfo/addSubreddit/removeSubreddit") {
        it("should add/remove subreddits from a multireddit") {
            val multi = reddit.me().createMulti(randomName(), MultiredditPatch()).toReference(reddit)
            undeletedRefs.add(multi)

            multi.addSubreddit("pics")
            multi.about().subreddits.should.contain("pics")
            multi.subredditInfo("pics")
            multi.removeSubreddit("pics")
            multi.about().subreddits.should.not.contain("pics")
        }

        it("should fail if the user doesn't own the multireddit") {
            val ref = reddit.user("reddit").multi("redditpets")
            expectException(ApiException::class) {
                ref.removeSubreddit("AliceAndTheDubber")
            }

            expectException(ApiException::class) {
                ref.addSubreddit("pics")
            }
        }
    }

    afterGroup {
        // Clean up undeleted multireddits
        for (ref in undeletedRefs) {
            try {
                ref.delete()
            } catch (e: Exception) {
                System.err.println("Warning: Unable to delete multireddit with path '${ref.multiPath}'")
                // ignore
            }
        }
    }
})
