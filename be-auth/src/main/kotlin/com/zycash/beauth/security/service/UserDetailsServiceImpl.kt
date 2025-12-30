package com.zycash.beauth.security.service


import com.zycash.beauth.constant.Constant
import com.zycash.beauth.entity.User
import com.zycash.beauth.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserDetailsServiceImpl : UserDetailsService {
    @Autowired
    private val userRepository: UserRepository? = null

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User? = userRepository?.findByUsername(username)
        if (Objects.isNull(user)) {
            throw UsernameNotFoundException(Constant.Message.NOT_FOUND_DATA_MESSAGE)
        }
        return UserDetailsImpl.build(user)
    }
}