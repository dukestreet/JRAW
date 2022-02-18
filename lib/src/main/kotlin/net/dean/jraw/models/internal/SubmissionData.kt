package net.dean.jraw.models.internal

import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.models.Listing
import net.dean.jraw.models.NestedIdentifiable
import net.dean.jraw.models.Submission

/**
 * This class attempts to model the response returned by `/comments/{id}`.
 */
@JsonClass(generateAdapter = true)
data class SubmissionData(
    @Enveloped val submissions: Listing<Submission>,
    @Enveloped val comments: Listing<NestedIdentifiable>,
)
