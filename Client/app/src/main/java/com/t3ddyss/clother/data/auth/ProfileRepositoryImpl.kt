package com.t3ddyss.clother.data.auth

import android.net.Uri
import com.google.gson.Gson
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.domain.auth.ProfileRepository
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.ImagesRepository
import com.t3ddyss.clother.util.networkBoundResource
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.util.extensions.rethrowIfCancellationException
import com.t3ddyss.core.util.log
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

    override fun observeCurrentUserInfo() = networkBoundResource(
        query = { userDao.observeUserWithDetailsById(storage.userId)
            .map { it.toDomain() }
        },
        fetch = {
            remoteAuthService.getUserDetails(
                accessToken = storage.accessToken,
                userId = storage.userId
            ).toDomain()
        },
        saveFetchResult = {
            saveUser(it)
        }
    )

    override fun observeUserInfo(userId: Int) = flow {
        emit(Loading(userDao.getUserById(userId).toDomain()))
        userDao.getUserWithDetailsById(userId)?.toDomain()?.let {
            emit(Loading(it))
        }

        try {
            val user = remoteAuthService.getUserDetails(
                accessToken = storage.accessToken,
                userId = userId
            ).toDomain()
            saveUser(user)
            emit(Success(user))
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ProfileRepositoryImpl.observeUserInfo: $ex")
        }
    }

    override suspend fun updateCurrentUserInfo(name: String, status: String, avatar: Uri?): User {
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

        val user = remoteAuthService.updateUserDetails(
            storage.accessToken,
            detailsBody,
            part
        ).toDomain()
        saveUser(user)

        return user
    }

    private suspend fun saveUser(user: User) {
        userDao.insert(user.toEntity())
        user.details?.toEntity(user.id)?.let {
            userDao.insert(it)
        }
    }
}