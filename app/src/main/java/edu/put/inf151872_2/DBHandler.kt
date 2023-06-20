package edu.put.inf151872_2
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler(
    context: Context, factory: SQLiteDatabase.CursorFactory?
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "games.db"
        private const val TABLE_GAMES = "board_games"
        private const val COLUMN_ID = "id"
        private const val COLUMN_BGG_ID = "bgg_id"
        private const val COLUMN_RELEASE_YEAR = "release_year"
        private const val COLUMN_MIN_PLAYERS = "min_players"
        private const val COLUMN_MAX_PLAYERS = "max_players"
        private const val COLUMN_PLAY_TIME = "play_time"
        private const val COLUMN_BGG_RANK = "bgg_rank"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_IS_EXPANSION = "expansion"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_IMAGE_URL = "image_path"
        private const val COLUMN_DESCRIPTION = "description"

        private var mInstance: DBHandler? = null

        @Synchronized
        fun getInstance(ctx: Context): DBHandler? {
            if (mInstance == null) {
                mInstance = DBHandler(ctx.applicationContext, null)
            }
            return mInstance
        }
    }

    init {
        mInstance = this
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_GAMES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_BGG_ID INTEGER NOT NULL,
                $COLUMN_RELEASE_YEAR INTEGER NOT NULL,
                $COLUMN_MIN_PLAYERS INTEGER NOT NULL,
                $COLUMN_MAX_PLAYERS INTEGER NOT NULL,
                $COLUMN_PLAY_TIME INTEGER NOT NULL,
                $COLUMN_BGG_RANK INTEGER,
                $COLUMN_RATING REAL NOT NULL,
                $COLUMN_IS_EXPANSION INTEGER NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_IMAGE_URL TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        onCreate(db)
    }

    fun addBoardGame(game: BoardGame) {
        val values = ContentValues()
        values.put(COLUMN_BGG_ID, game.bggId)
        values.put(COLUMN_RELEASE_YEAR, game.releaseYear)
        values.put(COLUMN_MIN_PLAYERS, game.minPlayers)
        values.put(COLUMN_MAX_PLAYERS, game.maxPlayers)
        values.put(COLUMN_PLAY_TIME, game.playTime)
        values.put(COLUMN_BGG_RANK, game.bggRank)
        values.put(COLUMN_RATING, game.rating)
        values.put(COLUMN_IS_EXPANSION, game.isExpansion)
        values.put(COLUMN_TITLE, game.title)
        values.put(COLUMN_IMAGE_URL, game.imageUrl)
        values.put(COLUMN_DESCRIPTION, game.description)

        val db = this.writableDatabase
        db.insert(TABLE_GAMES, null, values)
        db.close()
    }

    fun removeAllItems() {
        val db = this.writableDatabase
        db.delete(TABLE_GAMES, null, null)
        db.close()
    }

    fun getElements(isExpansion: Boolean): List<BoardGame> {
        val query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_IS_EXPANSION = $isExpansion ORDER BY $COLUMN_TITLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val boardGames = mutableListOf<BoardGame>()

        val columnIndexBggId = cursor.getColumnIndex(COLUMN_BGG_ID)
        val columnIndexReleaseYear = cursor.getColumnIndex(COLUMN_RELEASE_YEAR)
        val columnIndexMinPlayers = cursor.getColumnIndex(COLUMN_MIN_PLAYERS)
        val columnIndexMaxPlayers = cursor.getColumnIndex(COLUMN_MAX_PLAYERS)
        val columnIndexPlayTime = cursor.getColumnIndex(COLUMN_PLAY_TIME)
        val columnIndexBggRank = cursor.getColumnIndex(COLUMN_BGG_RANK)
        val columnIndexRating = cursor.getColumnIndex(COLUMN_RATING)
        val columnIndexTitle = cursor.getColumnIndex(COLUMN_TITLE)
        val columnIndexImageUrl = cursor.getColumnIndex(COLUMN_IMAGE_URL)
        val columnIndexDescription = cursor.getColumnIndex(COLUMN_DESCRIPTION)

        while (cursor.moveToNext()) {
            val bggId = cursor.getInt(columnIndexBggId)
            val releaseYear = cursor.getInt(columnIndexReleaseYear)
            val minPlayers = cursor.getInt(columnIndexMinPlayers)
            val maxPlayers = cursor.getInt(columnIndexMaxPlayers)
            val playTime = cursor.getInt(columnIndexPlayTime)
            val bggRank = cursor.getInt(columnIndexBggRank)
            val rating = cursor.getFloat(columnIndexRating)
            val title = cursor.getString(columnIndexTitle)
            val imageUrl = cursor.getString(columnIndexImageUrl)
            val description = cursor.getString(columnIndexDescription)

            val boardGame = BoardGame(
                bggId,
                releaseYear,
                minPlayers,
                maxPlayers,
                playTime,
                bggRank,
                rating,
                isExpansion,
                title,
                imageUrl,
                description
            )
            boardGames.add(boardGame)
        }

        cursor.close()
        db.close()
        return boardGames
    }

    fun countElements(isExpansion: Boolean): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_GAMES WHERE $COLUMN_IS_EXPANSION = $isExpansion"

        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        db.close()

        return count
    }

    fun getIds(): List<String> {
        val query = "SELECT $COLUMN_BGG_ID FROM $TABLE_GAMES"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val ids = mutableListOf<String>()

        while (cursor.moveToNext()) {
            val bggId = cursor.getInt(0).toString()
            ids.add(bggId)
        }

        cursor.close()
        db.close()
        return ids
    }

    fun deleteRowsByIds(ids: MutableList<String>) {
        val db = this.writableDatabase

        ids.forEach { id ->
            val whereClause = "$COLUMN_BGG_ID = ?"
            val whereArgs = arrayOf(id)
            db.delete(TABLE_GAMES, whereClause, whereArgs)
        }

        db.close()
    }
}