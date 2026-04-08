package data.local.dao

import androidx.room.*
import data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getCurrentUserSync(): User?

    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM user")
    suspend fun getAllUsersSync(): List<User>

    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()

    @Query("UPDATE user SET accessToken = :accessToken, refreshToken = :refreshToken WHERE id = :userId")
    suspend fun updateTokens(userId: String, accessToken: String?, refreshToken: String?)

    @Query("UPDATE user SET storageUsed = :storageUsed WHERE id = :userId")
    suspend fun updateStorageUsed(userId: String, storageUsed: Long)
}