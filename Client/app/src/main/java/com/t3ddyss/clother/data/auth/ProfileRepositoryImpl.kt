package com.t3ddyss.clother.data.auth

import android.net.Uri
import arrow.core.Either
import com.google.gson.Gson
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.common.common.Mappers.toApiCallError
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.domain.auth.ProfileRepository
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.auth.models.UserInfoState
import com.t3ddyss.clother.domain.offers.ImagesRepository
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val imagesRepository: ImagesRepository,
    private val remoteAuthService: RemoteAuthService,
    private val gson: Gson,
    private val userDao: UserDao,
    private val storage: Storage
) : ProfileRepository {

    override fun observeCurrentUserInfo(): Flow<UserInfoState> = flow {
        emit(UserInfoState.Cache(getUserWithDetailsOrUser(storage.userId)))

        remoteAuthService.getUserDetails(
            accessToken = storage.accessToken,
            userId = storage.userId
        )
            .tap { user ->
                storeUser(user.toDomain())
                emitAll(
                    userDao.observeUserWithDetailsById(storage.userId)
                        .map { UserInfoState.Fetched(it.toDomain()) }
                )
            }
            .tapLeft { error ->
                emitAll(
                    userDao.observeUserWithDetailsById(storage.userId)
                        .map { UserInfoState.Error(it.toDomain(), error.toApiCallError()) }
                )
            }
    }

    override fun observeUserInfo(userId: Int): Flow<UserInfoState> = flow {
        emit(UserInfoState.Cache(getUserWithDetailsOrUser(userId)))

        remoteAuthService.getUserDetails(
            accessToken = storage.accessToken,
            userId = userId
        )
            .tap { user ->
                storeUser(user.toDomain())
                emitAll(
                    userDao.observeUserWithDetailsById(userId)
                        .map { UserInfoState.Fetched(it.toDomain()) }
                )
            }
            .tapLeft {
                emit(UserInfoState.Error(getUserWithDetailsOrUser(userId), it.toApiCallError()))
            }
    }

    override suspend fun updateCurrentUserInfo(name: String, status: String, avatar: Uri?): Either<ApiCallError, User> {
        val detailsBody = gson.toJson(
            mapOf("name" to name, "status" to status)
        ).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val part = avatar?.let {
            val file = imagesRepository.getCompressedImage(it)
            MultipartBody.Part.createFormData(
                name = "file",
                file.name,
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
        }

        return remoteAuthService.updateUserDetails(
            storage.accessToken,
            detailsBody,
            part
        )
            .map { it.toDomain() }
            .mapLeft { it.toApiCallError() }
            .tap { storeUser(it) }
    }

    private suspend fun getUserWithDetailsOrUser(userId: Int): User {
        return userDao.getUserWithDetailsById(userId)?.toDomain()
            ?: userDao.getUserById(userId).toDomain()
    }

    private suspend fun storeUser(user: User) {
        userDao.insert(user.toEntity())
        user.details?.toEntity(user.id)?.let {
            userDao.insert(it)
        }
    }
}