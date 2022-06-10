package com.jeketos.storage

import com.jeketos.data.User

object UserStorage {
    private val users: MutableList<User> = mutableListOf()

    fun addUser(user: User) {
        users.add(user)
    }

    fun getUser(uid: String): User? = users.find { it.uid == uid }

    fun getAllUsers(): List<User> = users.toList()
}