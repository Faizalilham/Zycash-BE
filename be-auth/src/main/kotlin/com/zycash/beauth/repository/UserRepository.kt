package com.zycash.beauth.repository


import com.zycash.beauth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {

    @Query(value = "select * from users where username =?1 and is_deleted = false", nativeQuery = true)
    fun findByUsername(username: String?): User?
}