package com.jeketos.db.dao

import com.jeketos.data.AuthUser
import com.jeketos.data.User
import com.jeketos.db.DatabaseFactory
import com.jeketos.db.model.AuthUsers
import com.jeketos.db.model.Users
import com.jeketos.db.model.insert
import com.jeketos.db.model.select

class DAOFacadeImpl : DAOFacade {

    override suspend fun addAuthUser(user: AuthUser) {
        DatabaseFactory.dbQuery {
            AuthUsers.insert(user)
        }
    }

    override suspend fun getAuthUserByEmail(email: String): AuthUser? = DatabaseFactory.dbQuery {
        AuthUsers.select(email)
    }

    override suspend fun addUser(user: User) {
        DatabaseFactory.dbQuery {
            Users.insert(user)
        }
    }

    override suspend fun getUser(uid: String): User? = DatabaseFactory.dbQuery {
        Users.select(uid)
    }
}