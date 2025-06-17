package com.paramsen.noise.sample.view

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T> Flow<T>.windowed(size: Int): Flow<List<T>> {
    val list = ArrayList<T>(size)

    return transform {
        list.add(it)

        if (list.size == size) {
            val copy = list.toList()
            list.clear()
            emit(copy)
        }
    }
}