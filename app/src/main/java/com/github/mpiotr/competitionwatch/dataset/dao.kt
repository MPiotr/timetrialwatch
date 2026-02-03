package com.github.mpiotr.competitionwatch.dataset

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


// DAO
@Dao
interface CompetitorDao {
    @Query("SELECT * FROM competitors ORDER BY id")
    fun getAll(): Flow<List<Competitor>>

    @Query("SELECT COUNT(*) FROM competitors")
    fun competitorCount(): Flow<Int>

    @Query("SELECT MAX(bib_bib_number) FROM competitors")
    fun max_bib_number():Flow<Int>


    @Query("SELECT COUNT(*) FROM competitors WHERE bib_bib_number=:number AND bib_bib_color =:color")
    fun countBib(number : Int, color : Int):Flow<Int>

    @Query("SELECT bib_bib_number AS bib_number, bib_bib_color AS bib_color FROM competitors")
    fun registerd_bibs():Flow<List<Bib>>

    @Query("SELECT * FROM competitors WHERE bib_bib_number=:number AND bib_bib_color =:color")
    fun getCompetitor(number : Int, color : Int):Flow<Competitor>

    @Query("SELECT * FROM groups")
    fun groups() : Flow<List<Groups>>

    @Query("SELECT * FROM groups WHERE name=:id")
    fun getGroup(id : String) : Flow<Groups>

    @Query("SELECT email FROM  competitors")
    fun allEmails() : List<String>

    @Update
    suspend fun updateGroup(group: Groups)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(groups: Groups)

    @Query ("SELECT * from info WHERE id = 1")
    fun settings() : Flow<Settings>

    @Update
    suspend fun updateSettings(settings: Settings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(competitor: Competitor)

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //suspend fun insert(settings: CompetitionSettings)

    @Update
    suspend fun update(competitor: Competitor)

    //@Update
    //suspend fun update(settings: CompetitionSettings)

    @Update
    suspend fun updateCompetitors(competitors: List<Competitor>)

    @Transaction
    suspend fun updateAll(competitors: List<Competitor>) {
        updateCompetitors(competitors)
    }

    @Delete
    suspend fun delete(competitor: Competitor)

    @Query("DELETE FROM competitors")
    fun deleteAllCompetitors()

    @Query("DELETE FROM groups")
    fun deleteAllGroups()

    @Query("DELETE FROM info")
    fun deleteAllSettings()
}



// Database
@Database(entities = [Competitor::class, Groups::class, Settings::class], version = 1, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun competitorDao(): CompetitorDao


}