package com.wilkins.safezone.backend.network.User.Profile


import kotlinx.serialization.Serializable

@Serializable
data class UserFollow(
    val id: String,
    val follower_id: String,
    val following_id: String,
    val created_at: String
)

@Serializable
data class FollowStats(
    val user_id: String,
    val followers_count: Int,
    val following_count: Int
)

@Serializable
data class FollowRequest(
    val follower_id: String,
    val following_id: String
)

@Serializable
data class FollowResponse(
    val success: Boolean,
    val message: String,
    val isFollowing: Boolean
)