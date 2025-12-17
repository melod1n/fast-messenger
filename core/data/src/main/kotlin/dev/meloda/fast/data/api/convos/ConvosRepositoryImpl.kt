package dev.meloda.fast.data.api.convos

import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.VkGroupsMap
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.VkUsersMap
import dev.meloda.fast.database.dao.ConvoDao
import dev.meloda.fast.database.dao.GroupDao
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.database.dao.UserDao
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.model.api.data.asDomain
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkGroupDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.model.api.domain.asEntity
import dev.meloda.fast.model.api.requests.ConvosGetRequest
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.service.convos.ConvosService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConvosRepositoryImpl(
    private val convosService: ConvosService,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val groupDao: GroupDao,
    private val convoDao: ConvoDao
) : ConvosRepository {

    override suspend fun storeConvos(convos: List<VkConvo>) {
        convoDao.insertAll(convos.map(VkConvo::asEntity))
    }

    override suspend fun getConvos(
        count: Int?,
        offset: Int?,
        filter: ConvosFilter
    ): ApiResult<List<VkConvo>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConvosGetRequest(
            count = count,
            offset = offset,
            fields = VkConstants.ALL_FIELDS,
            filter = filter,
            extended = true,
            startMessageId = null
        )

        convosService.getConvos(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
                val contactsList = response.contacts.orEmpty().map(VkContactData::mapToDomain)

                val usersMap = VkUsersMap.forUsers(profilesList)
                val groupsMap = VkGroupsMap.forGroups(groupsList)

                VkMemoryCache.appendUsers(profilesList)
                VkMemoryCache.appendGroups(groupsList)
                VkMemoryCache.appendContacts(contactsList)

                val convos = response.items.map { item ->
                    val lastMessage = item.lastMessage?.asDomain()?.let { message ->
                        message.copy(
                            user = usersMap.messageUser(message),
                            group = groupsMap.messageGroup(message),
                            actionUser = usersMap.messageActionUser(message),
                            actionGroup = groupsMap.messageActionGroup(message),
                            replyMessage = message.replyMessage?.copy(
                                user = usersMap.messageUser(message),
                                group = groupsMap.messageGroup(message),
                                actionUser = usersMap.messageActionUser(message),
                                actionGroup = groupsMap.messageActionGroup(message),
                            )
                        ).also { VkMemoryCache[message.id] = it }
                    }
                    item.convo.asDomain(lastMessage).let { convo ->
                        convo.copy(
                            user = usersMap.convoUser(convo),
                            group = groupsMap.convoGroup(convo)
                        ).also { VkMemoryCache[convo.id] = it }
                    }
                }

                val messages = convos.mapNotNull(VkConvo::lastMessage)

                launch(Dispatchers.IO) {
                    convoDao.insertAll(convos.map(VkConvo::asEntity))
                    messageDao.insertAll(messages.map(VkMessage::asEntity))
                    userDao.insertAll(profilesList.map(VkUser::asEntity))
                    groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                }

                convos
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun getConvosById(
        peerIds: List<Long>,
        extended: Boolean?,
        fields: String?
    ): ApiResult<List<VkConvo>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestParams = mutableMapOf(
            "peer_ids" to peerIds.joinToString(separator = ",")
        ).apply {
            extended?.let { this["extended"] = if (it) "1" else "0" }
            fields?.let { this["fields"] = it }
        }

        convosService.getConvosById(requestParams).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
                val contactsList = response.contacts.orEmpty().map(VkContactData::mapToDomain)

                val usersMap = VkUsersMap.forUsers(profilesList)
                val groupsMap = VkGroupsMap.forGroups(groupsList)

                val convos = response.items.map { item ->
                    item.asDomain().let { convo ->
                        convo.copy(
                            user = usersMap.convoUser(convo),
                            group = groupsMap.convoGroup(convo)
                        ).also { VkMemoryCache[convo.id] = it }
                    }
                }

                launch(Dispatchers.IO) {
                    convoDao.insertAll(convos.map(VkConvo::asEntity))
                    userDao.insertAll(profilesList.map(VkUser::asEntity))
                    groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                }

                VkMemoryCache.appendUsers(profilesList)
                VkMemoryCache.appendGroups(groupsList)
                VkMemoryCache.appendContacts(contactsList)

                convos
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun delete(peerId: Long): ApiResult<Long, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            convosService.delete(mapOf("peer_id" to peerId.toString())).mapApiResult(
                successMapper = { response -> response.requireResponse().lastDeletedId },
                errorMapper = { error -> error?.toDomain() }
            )
        }

    override suspend fun pin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        convosService.pin(mapOf("peer_id" to peerId.toString())).mapApiDefault()
    }

    override suspend fun unpin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        convosService.unpin(mapOf("peer_id" to peerId.toString())).mapApiDefault()
    }

    override suspend fun reorderPinned(
        peerIds: List<Long>
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        convosService
            .reorderPinned(mapOf("peer_ids" to peerIds.joinToString(",")))
            .mapApiDefault()
    }

    override suspend fun archive(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        convosService.archive(mapOf("peer_id" to peerId.toString())).mapApiDefault()
    }

    override suspend fun unarchive(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        convosService.unarchive(mapOf("peer_id" to peerId.toString())).mapApiDefault()
    }
}
