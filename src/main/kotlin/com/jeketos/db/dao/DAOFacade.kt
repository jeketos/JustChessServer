package com.jeketos.db.dao

import com.jeketos.data.AuthUser
import com.jeketos.data.User

interface DAOFacade {
    suspend fun addAuthUser(user: AuthUser)
    suspend fun getAuthUserByEmail(email: String): AuthUser?
    suspend fun addUser(user: User)
    suspend fun getUser(uid: String): User?
}