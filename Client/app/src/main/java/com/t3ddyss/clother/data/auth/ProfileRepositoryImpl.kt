package com.t3ddyss.clother.data.auth

import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.domain.auth.ProfileRepository
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.rethrowIfCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remoteAuthService: RemoteAuthService,
    private val userDao: UserDao,
    private val storage: Storage
) : ProfileRepository {
    override fun observeUserInfo(userId: Int): Flow<User> = flow {
        emit(userDao.getUserById(userId).toDomain())
        userDao.getUserWithDetailsById(userId)?.toDomain()?.let {
            emit(it)
        }
        try {
            val user = remoteAuthService.getUserDetails(
                accessToken = storage.accessToken,
                userId = userId
            )
            emit(user.toDomain())

            userDao.insert(user.toEntity())
            user.details?.let {
                userDao.insert(it.toEntity(user.id))
            }
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ProfileRepositoryImpl.observeUserInfo: $ex")
        }
    }
}