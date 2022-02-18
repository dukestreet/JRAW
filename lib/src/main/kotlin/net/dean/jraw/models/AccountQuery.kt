package net.dean.jraw.models

import java.io.Serializable

/**
 * The main function of an AccountQuery is to store the status of an account. If an account exists and is not suspended,
 * then the [Account] object can be used normally.
 */
data class AccountQuery(
    /** The reddit username being queried  */
    val name: String,

    val status: AccountStatus,

    /** The account data. Only non-null when the status is [AccountStatus.EXISTS].  */
    val account: Account? = null
) : Serializable
