package com.zycash.beauth.security.service


import com.fasterxml.jackson.annotation.JsonIgnore
import com.zycash.beauth.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class UserDetailsImpl(
    private val id: String,
    private val username: String,
    @JsonIgnore
    private val password: String,
    val avatar: String
) : UserDetails {

    companion object {
        private const val serialVersionUID = 1L

        fun build(user: User?): UserDetailsImpl {
            return UserDetailsImpl(
                id = user?.id ?: "",
                username = user?.username ?: "",
                password = user?.password ?: "",
                avatar = user?.avatar ?: ""
            )
        }
    }


    fun getId(): String = id

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return emptyList()
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val userDetails = other as UserDetailsImpl
        return id == userDetails.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}