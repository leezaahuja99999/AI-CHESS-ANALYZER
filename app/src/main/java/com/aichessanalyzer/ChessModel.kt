package com.aichessanalyzer

import com.github.bhlangonijr.chesslib.*
import com.github.bhlangonijr.chesslib.move.Move

class ChessModel {
    private val board = Board()
    private val history = mutableListOf<String>()
    private var cursor = 0

    fun fen() = board.fen

    fun turnIsWhite() = board.sideToMove == Side.WHITE

    fun reset() {
        board.loadFromFen(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        )
        history.clear()
        cursor = 0
    }

    fun move(from: String, to: String): Boolean = try {
        val m = Move(
            Square.valueOf(from.uppercase()),
            Square.valueOf(to.uppercase())
        )

        if (!board.isMoveLegal(m, true)) {
            false
        } else {
            board.doMove(m)

            if (cursor < history.size) {
                history.subList(cursor, history.size).clear()
            }

            history += "$from$to"
            cursor = history.size
            true
        }
    } catch (_: Exception) {
        false
    }

    fun undo(): Boolean {
        if (cursor <= 0) return false
        board.undoMove()
        cursor--
        return true
    }

    fun redo(): Boolean {
        if (cursor >= history.size) return false

        val u = history[cursor]

        val m = Move(
            Square.valueOf(u.substring(0, 2).uppercase()),
            Square.valueOf(u.substring(2, 4).uppercase())
        )

        if (board.isMoveLegal(m, true)) {
            board.doMove(m)
            cursor++
            return true
        }

        return false
    }

    fun pieceAt(file: Int, rank: Int): String {
        val s = Square.values()
            .firstOrNull {
                it.file.ordinal == file && it.rank.ordinal == rank
            } ?: return ""

        return board.getPiece(s).fenSymbol
    }

    fun pgnMoves(): String =
        history.take(cursor).joinToString(" ")

    fun loadFen(fen: String): Boolean = try {
        board.loadFromFen(fen)
        history.clear()
        cursor = 0
        true
    } catch (_: Exception) {
        false
    }
}
