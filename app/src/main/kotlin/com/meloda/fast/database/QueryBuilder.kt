package com.meloda.fast.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase


class QueryBuilder private constructor() {

    companion object {
        fun query(): QueryBuilder {
            return QueryBuilder()
        }
    }

    private val builder: StringBuilder = StringBuilder()

    fun select(column: String): QueryBuilder {
        builder.append("SELECT ")
            .append(column)
            .append(" ")
        return this
    }

    fun from(table: String): QueryBuilder {
        builder.append("FROM ")
            .append(table)
            .append(" ")
        return this
    }


    fun where(clause: String): QueryBuilder {
        builder.append("WHERE ")
            .append(clause)
            .append(" ")
        return this
    }

    fun leftJoin(table: String): QueryBuilder {
        builder.append("LEFT JOIN ")
            .append(table)
            .append(" ")
        return this
    }

    fun on(where: String): QueryBuilder {
        builder.append("ON ")
            .append(where)
            .append(" ")
        return this
    }

    fun and(): QueryBuilder {
        builder.append("AND ")
        return this
    }

    fun or(): QueryBuilder {
        builder.append("OR ")
        return this
    }

    fun asCursor(db: SQLiteDatabase): Cursor {
        return db.rawQuery(toString(), null)
    }

    override fun toString(): String {
        return builder.toString().trim()
    }

}