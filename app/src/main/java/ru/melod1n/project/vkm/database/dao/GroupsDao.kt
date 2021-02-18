package ru.melod1n.project.vkm.database.dao

import androidx.room.*
import ru.melod1n.project.vkm.api.model.VKGroup

@Dao
interface GroupsDao {
    @Query("SELECT * FROM groups")
    fun getAll(): List<VKGroup>

    @Query("SELECT * FROM groups WHERE groupId = :id")
    fun getById(id: Int): VKGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: VKGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<VKGroup>)

    @Update
    fun update(item: VKGroup)

    @Update
    fun update(items: List<VKGroup>)

    @Delete
    fun delete(item: VKGroup)

    @Delete
    fun delete(items: List<VKGroup>)

    @Query("DELETE FROM groups WHERE groupId = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM groups")
    fun clear()
}