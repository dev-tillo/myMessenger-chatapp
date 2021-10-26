package com.example.mymessenger.servvv

data class ResNotificationData(
    val canonical_ids: Int,
    val failure: Int,
    val multicast_id: Long,
    val results: List<Result>,
    val success: Int
)